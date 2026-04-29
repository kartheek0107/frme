package com.fibcam.model

import androidx.camera.core.CameraSelector

/** Everything the UI needs to know about the current camera state */
data class CameraUiState(

    // ── Lens ────────────────────────────────────────────────────────────────
    val lensFacing: Int = CameraSelector.LENS_FACING_BACK,
    val availableLenses: List<LensInfo> = emptyList(),
    val currentZoom: Float = 1f,
    val minZoom: Float = 1f,
    val maxZoom: Float = 10f,

    // ── Capture mode ────────────────────────────────────────────────────────
    val captureMode: CaptureMode = CaptureMode.PHOTO,
    val isRawEnabled: Boolean = false,
    val isRawSupported: Boolean = false,

    // ── Manual controls ──────────────────────────────────────────────────────
    val isManualMode: Boolean = false,
    val iso: Int = 100,
    val isoRange: IntRange = 100..3200,
    val shutterSpeed: Long = 16_666_666L,   // nanoseconds — default 1/60s
    val shutterRange: LongRange = 1_000_000L..1_000_000_000L,
    val whiteBalance: WhiteBalanceMode = WhiteBalanceMode.AUTO,
    val focusDistance: Float = 0f,
    val isAutoFocus: Boolean = true,

    // ── Overlays ─────────────────────────────────────────────────────────────
    val activeOverlays: Set<OverlayType> = OverlayType.DEFAULTS,
    val overlayColor: OverlayColor = OverlayColor.GOLD,
    val overlayAlpha: Float = 0.75f,

    // ── Flash & timer ────────────────────────────────────────────────────────
    val flashMode: FlashMode = FlashMode.AUTO,
    val timerSeconds: Int = 0,              // 0 = off, 3, 5, 10

    // ── Grid / level ────────────────────────────────────────────────────────
    val showLevelIndicator: Boolean = false,
    val deviceTiltDegrees: Float = 0f,

    // ── UI state ─────────────────────────────────────────────────────────────
    val isCapturing: Boolean = false,
    val lastCaptureUri: String? = null,
    val error: String? = null
)

data class LensInfo(
    val facing: Int,
    val label: String,
    val zoomRatio: Float   // e.g. 0.6x, 1x, 3x
)

enum class CaptureMode(val label: String) {
    PHOTO("Photo"),
    RAW("RAW"),
    VIDEO("Video"),
    BURST("Burst")
}

enum class FlashMode(val label: String) {
    OFF("Off"),
    AUTO("Auto"),
    ON("On"),
    TORCH("Torch")
}

enum class WhiteBalanceMode(val label: String) {
    AUTO("Auto"),
    DAYLIGHT("Daylight"),
    CLOUDY("Cloudy"),
    TUNGSTEN("Tungsten"),
    FLUORESCENT("Fluorescent"),
    SHADE("Shade")
}

enum class OverlayColor(val label: String, val hexArgb: Long) {
    GOLD("Gold",  0xFFFFD700),
    WHITE("White", 0xFFFFFFFF),
    BLACK("Black", 0xFF000000),
    RED("Red",    0xFFFF3B30),
    CYAN("Cyan",  0xFF00FFFF)
}