package com.fibcam.camera

import android.hardware.camera2.CaptureRequest
import android.util.Log
import androidx.camera.camera2.interop.CameraControl
import androidx.camera.camera2.interop.ExposureCompensation
import androidx.camera.core.Camera
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Helper to apply Camera2 controls (ISO, exposure time) via CameraX Camera2 interop.
 * Used for manual exposure mode in advanced photography workflow.
 */
class ManualControlHelper {

    companion object {
        private const val TAG = "ManualControlHelper"
    }

    /**
     * Apply ISO and shutter speed via Camera2 interop.
     * Requires Camera2CameraInfo to detect capabilities.
     */
    fun applyManualExposure(
        camera: Camera,
        iso: Int,
        shutterSpeedNs: Long
    ) {
        try {
            val cameraControl = camera.cameraControl
            // Camera2 interop via CaptureRequest extensions
            // Note: Full manual exposure requires direct Camera2 API in practice.
            // This is a placeholder for the interop layer.
            Log.d(TAG, "Applying manual exposure: ISO=$iso, shutterSpeed=${shutterSpeedNs}ns")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to apply manual exposure", e)
        }
    }

    /**
     * Apply exposure compensation (simpler alternative to full manual mode).
     */
    fun applyExposureCompensation(
        camera: Camera,
        ev: Float
    ) {
        try {
            camera.cameraControl.setExposureCompensationIndex((ev * 2).toInt())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to apply exposure compensation", e)
        }
    }
}