package com.fibcam.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Bento-grid shape system.
 * High radius softens the hard monochromatic palette — design doc §III
 */
val FibCamShapes = Shapes(
    // Small chips, badges
    small  = RoundedCornerShape(8.dp),
    // Overlay picker, control panels
    medium = RoundedCornerShape(16.dp),
    // Main bento cards
    large  = RoundedCornerShape(24.dp),
    // Bottom sheet, full modal panels
    extraLarge = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
)