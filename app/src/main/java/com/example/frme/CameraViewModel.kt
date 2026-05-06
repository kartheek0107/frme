package com.fibcam.viewmodel

import android.content.ContentValues
import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fibcam.data.AppPreferences
import com.fibcam.model.*
import com.fibcam.sensor.AccelerometerHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appPreferences: AppPreferences,
    private val accelerometerHelper: AccelerometerHelper
) : ViewModel() {

    companion object {
        private const val TAG = "CameraViewModel"
    }

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null

    init {
        // Load saved preferences
        loadPreferences()
        // Start accelerometer for level indicator
        accelerometerHelper.startListening()
        // Collect tilt updates
        viewModelScope.launch {
            accelerometerHelper.tiltDegrees.collect { tilt ->
                _uiState.update { it.copy(deviceTiltDegrees = tilt) }
            }
        }
    }

    private fun loadPreferences() {
        val overlayIds = appPreferences.getActiveOverlayIds()
        val overlayColor = OverlayColor.valueOf(appPreferences.getOverlayColor())
        val overlayAlpha = appPreferences.getOverlayAlpha()
        val lastZoom = appPreferences.getLastZoom()

        val activeOverlays = OverlayType.ALL.filter { it.id in overlayIds }.toSet()

        _uiState.update {
            it.copy(
                activeOverlays = activeOverlays.ifEmpty { OverlayType.DEFAULTS },
                overlayColor = overlayColor,
                overlayAlpha = overlayAlpha,
                currentZoom = lastZoom,
                showLevelIndicator = appPreferences.getShowLevelIndicator(),
                isManualMode = appPreferences.getIsManualMode(),
                iso = appPreferences.getLastIso()
            )
        }
    }

    // ── Camera lifecycle ─────────────────────────────────────────────────────

    fun bindCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView
    ) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            try {
                cameraProvider = providerFuture.get()
                bindUseCases(lifecycleOwner, previewView)
                detectAvailableLenses()
                detectRawSupport()
            } catch (e: Exception) {
                Log.e(TAG, "Camera initialization failed", e)
                _uiState.update { it.copy(error = "Camera init failed: ${e.message}") }
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun bindUseCases(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView
    ) {
        val state = _uiState.value
        val provider = cameraProvider ?: return

        // Build Preview
        val preview = Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .build()
            .also { it.setSurfaceProvider(previewView.surfaceProvider) }

        // Build ImageCapture
        val captureBuilder = ImageCapture.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setFlashMode(state.flashMode.toCameraX())

        imageCapture = captureBuilder.build()

        // Lens selector
        val selector = if (state.lensFacing == CameraSelector.LENS_FACING_BACK)
            CameraSelector.DEFAULT_BACK_CAMERA
        else
            CameraSelector.DEFAULT_FRONT_CAMERA

        try {
            provider.unbindAll()
            camera = provider.bindToLifecycle(
                lifecycleOwner, selector, preview, imageCapture
            )
            applyZoom(state.currentZoom)
        } catch (e: Exception) {
            Log.e(TAG, "bindUseCases failed", e)
            _uiState.update { it.copy(error = "Camera bind failed: ${e.message}") }
        }
    }

    private fun detectAvailableLenses() {
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val lenses = mutableListOf<LensInfo>()

        try {
            for (id in manager.cameraIdList) {
                val chars = manager.getCameraCharacteristics(id)
                val facing = chars.get(CameraCharacteristics.LENS_FACING) ?: continue
                val focalLengths = chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
                val label = when {
                    focalLengths != null && focalLengths[0] < 3f -> "Ultra"
                    focalLengths != null && focalLengths[0] > 7f -> "Tele"
                    else -> "Wide"
                }
                lenses.add(
                    LensInfo(
                        facing = facing,
                        label = label,
                        zoomRatio = when (label) {
                            "Ultra" -> 0.6f
                            "Tele" -> 3.0f
                            else -> 1.0f
                        }
                    )
                )
            }
            _uiState.update { it.copy(availableLenses = lenses) }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to detect lenses", e)
        }
    }

    private fun detectRawSupport() {
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            for (id in manager.cameraIdList) {
                val chars = manager.getCameraCharacteristics(id)
                val caps = chars.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES) ?: continue
                if (CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW in caps) {
                    _uiState.update { it.copy(isRawSupported = true) }
                    return
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to detect RAW support", e)
        }
    }

    // ── Capture ──────────────────────────────────────────────────────────────

    fun capturePhoto() {
        val capture = imageCapture ?: return
        _uiState.update { it.copy(isCapturing = true) }

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "fibcam_${System.currentTimeMillis()}")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/FibCam")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(context.contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            .build()

        capture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val uri = output.savedUri?.toString()
                    _uiState.update {
                        it.copy(
                            isCapturing = false,
                            lastCaptureUri = uri
                        )
                    }
                    Log.i(TAG, "Photo saved: $uri")
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Capture failed", exception)
                    _uiState.update {
                        it.copy(
                            isCapturing = false,
                            error = exception.message ?: "Capture failed"
                        )
                    }
                }
            }
        )
    }

    // ── Controls ─────────────────────────────────────────────────────────────

    fun setZoom(zoom: Float) {
        applyZoom(zoom)
        _uiState.update { it.copy(currentZoom = zoom) }
        appPreferences.setLastZoom(zoom)
    }

    private fun applyZoom(zoom: Float) {
        try {
            camera?.cameraControl?.setZoomRatio(zoom)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to set zoom", e)
        }
    }

    fun flipLens() {
        _uiState.update {
            it.copy(
                lensFacing = if (it.lensFacing == CameraSelector.LENS_FACING_BACK)
                    CameraSelector.LENS_FACING_FRONT
                else
                    CameraSelector.LENS_FACING_BACK
            )
        }
    }

    fun setFlashMode(mode: FlashMode) {
        try {
            imageCapture?.flashMode = mode.toCameraX()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to set flash mode", e)
        }
        _uiState.update { it.copy(flashMode = mode) }
        appPreferences.setLastFlashMode(mode.name)
    }

    fun setCaptureMode(mode: CaptureMode) {
        _uiState.update { it.copy(captureMode = mode) }
    }

    fun toggleRaw() {
        _uiState.update { it.copy(isRawEnabled = !it.isRawEnabled) }
    }

    fun setManualMode(enabled: Boolean) {
        _uiState.update { it.copy(isManualMode = enabled) }
        appPreferences.setIsManualMode(enabled)
    }

    fun setIso(iso: Int) {
        _uiState.update { it.copy(iso = iso) }
        appPreferences.setLastIso(iso)
    }

    fun setShutterSpeed(ns: Long) {
        _uiState.update { it.copy(shutterSpeed = ns) }
    }

    fun setWhiteBalance(mode: WhiteBalanceMode) {
        _uiState.update { it.copy(whiteBalance = mode) }
    }

    // ── Overlay controls ─────────────────────────────────────────────────────

    fun toggleOverlay(overlay: OverlayType) {
        _uiState.update {
            val current = it.activeOverlays.toMutableSet()
            if (overlay in current) current.remove(overlay) else current.add(overlay)
            val updated = it.copy(activeOverlays = current)
            appPreferences.setActiveOverlayIds(current.map { o -> o.id }.toSet())
            updated
        }
    }

    fun setOverlayColor(color: OverlayColor) {
        _uiState.update { it.copy(overlayColor = color) }
        appPreferences.setOverlayColor(color.name)
    }

    fun setOverlayAlpha(alpha: Float) {
        _uiState.update { it.copy(overlayAlpha = alpha) }
        appPreferences.setOverlayAlpha(alpha)
    }

    fun clearOverlays() {
        _uiState.update { it.copy(activeOverlays = emptySet()) }
        appPreferences.setActiveOverlayIds(emptySet())
    }

    fun setLevelIndicator(show: Boolean) {
        _uiState.update { it.copy(showLevelIndicator = show) }
        appPreferences.setShowLevelIndicator(show)
    }

    fun updateTilt(degrees: Float) {
        _uiState.update { it.copy(deviceTiltDegrees = degrees) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        accelerometerHelper.stopListening()
    }
}

// ── Extension: FlashMode → CameraX constant ──────────────────────────────────
private fun FlashMode.toCameraX(): Int = when (this) {
    FlashMode.OFF -> ImageCapture.FLASH_MODE_OFF
    FlashMode.AUTO -> ImageCapture.FLASH_MODE_AUTO
    FlashMode.ON -> ImageCapture.FLASH_MODE_ON
    FlashMode.TORCH -> ImageCapture.FLASH_MODE_ON
}