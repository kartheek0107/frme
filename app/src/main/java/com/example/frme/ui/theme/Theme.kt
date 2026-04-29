package com.fibcam.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── "Stealth Wealth" palette — strictly monochromatic ────────────────────────

object FibCamColors {
    val Black         = Color(0xFF000000)
    val White         = Color(0xFFFFFFFF)
    val PaperWhite    = Color(0xFFF8F8F8)
    val SurfaceGrey   = Color(0xFFF2F2F2)
    val MidGrey       = Color(0xFF888888)
    val DarkGrey      = Color(0xFF1C1C1E)
    val CardDark      = Color(0xFF111111)

    // Single accent — the "red line" concept from the design doc
    val AccentGold    = Color(0xFFFFD700)
    val AccentRed     = Color(0xFFFF3B30)

    // Overlay glass tint
    val GlassDark     = Color(0xCC000000)   // 80% black
    val GlassLight    = Color(0x33FFFFFF)   // 20% white
}

// ── Typography — Inter / system geometric sans ────────────────────────────────
// (Inter must be added to res/font/ — falls back to system sans gracefully)

val FibCamTypography = Typography(
    // Level 1: Hero numerals (shutter speed, ISO hero display)
    displayLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize   = 72.sp,
        letterSpacing = (-2).sp
    ),
    displayMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize   = 48.sp,
        letterSpacing = (-1.5).sp
    ),
    // Level 2: Section labels
    headlineLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize   = 28.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize   = 22.sp,
        letterSpacing = (-0.3).sp
    ),
    // Level 3: Metadata — tiny caps
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize   = 10.sp,
        letterSpacing = 1.2.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize   = 12.sp,
        letterSpacing = 0.8.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 14.sp,
        letterSpacing = 0.1.sp
    )
)

// ── Dark color scheme (camera is always dark-mode) ───────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary          = FibCamColors.White,
    onPrimary        = FibCamColors.Black,
    secondary        = FibCamColors.MidGrey,
    onSecondary      = FibCamColors.White,
    background       = FibCamColors.Black,
    onBackground     = FibCamColors.White,
    surface          = FibCamColors.DarkGrey,
    onSurface        = FibCamColors.White,
    surfaceVariant   = FibCamColors.CardDark,
    onSurfaceVariant = FibCamColors.MidGrey,
    error            = FibCamColors.AccentRed,
    outline          = FibCamColors.DarkGrey
)

// Light scheme for settings screens
private val LightColorScheme = lightColorScheme(
    primary          = FibCamColors.Black,
    onPrimary        = FibCamColors.White,
    secondary        = FibCamColors.MidGrey,
    onSecondary      = FibCamColors.White,
    background       = FibCamColors.PaperWhite,
    onBackground     = FibCamColors.Black,
    surface          = FibCamColors.SurfaceGrey,
    onSurface        = FibCamColors.Black,
    surfaceVariant   = FibCamColors.White,
    onSurfaceVariant = FibCamColors.DarkGrey,
    error            = FibCamColors.AccentRed,
    outline          = FibCamColors.SurfaceGrey
)

@Composable
fun FibCamTheme(
    darkTheme: Boolean = true, // Camera UI is always dark
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = FibCamTypography,
        shapes      = FibCamShapes,
        content     = content
    )
}