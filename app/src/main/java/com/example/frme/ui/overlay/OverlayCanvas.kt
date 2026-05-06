package com.fibcam.ui.overlay

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.fibcam.model.OverlayType

/**
 * Canvas composable that renders composition guides over the camera preview.
 * Zero-allocation hot path — all drawing delegated to OverlayRenderer.
 */
@Composable
fun OverlayCanvas(
    overlays: Set<OverlayType>,
    color: Color,
    alpha: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        overlays.forEach { overlay ->
            drawOverlay(overlay, color, alpha)
        }
    }
}