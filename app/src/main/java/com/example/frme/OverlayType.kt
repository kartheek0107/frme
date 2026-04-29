package com.fibcam.model

/**
 * Every composition overlay template FibCam supports.
 * Each type carries display metadata for the UI picker.
 */
sealed class OverlayType(
    val id: String,
    val label: String,
    val category: OverlayCategory
) {
    // ── Golden Ratio Family ──────────────────────────────────────────────────
    object FibonacciSpiralTL  : OverlayType("fib_spiral_tl",  "Fibonacci ↖",    OverlayCategory.GOLDEN_RATIO)
    object FibonacciSpiralTR  : OverlayType("fib_spiral_tr",  "Fibonacci ↗",    OverlayCategory.GOLDEN_RATIO)
    object FibonacciSpiralBL  : OverlayType("fib_spiral_bl",  "Fibonacci ↙",    OverlayCategory.GOLDEN_RATIO)
    object FibonacciSpiralBR  : OverlayType("fib_spiral_br",  "Fibonacci ↘",    OverlayCategory.GOLDEN_RATIO)
    object PhiGrid            : OverlayType("phi_grid",        "Phi Grid",       OverlayCategory.GOLDEN_RATIO)
    object GoldenRectangle    : OverlayType("golden_rect",     "Golden Rect",    OverlayCategory.GOLDEN_RATIO)
    object GoldenTriangle     : OverlayType("golden_tri",      "Golden Triangle",OverlayCategory.GOLDEN_RATIO)
    object GoldenDiagonal     : OverlayType("golden_diag",     "Golden Diagonal",OverlayCategory.GOLDEN_RATIO)

    // ── Grid Family ──────────────────────────────────────────────────────────
    object RuleOfThirds       : OverlayType("thirds",          "Rule of Thirds", OverlayCategory.GRID)
    object RuleOfFifths       : OverlayType("fifths",          "Rule of Fifths", OverlayCategory.GRID)
    object QuadrantGrid       : OverlayType("quadrant",        "Quadrant",       OverlayCategory.GRID)
    object CentreCross        : OverlayType("centre_cross",    "Centre Cross",   OverlayCategory.GRID)
    object SquareCrop         : OverlayType("square_crop",     "1:1 Square",     OverlayCategory.GRID)

    // ── Triangle & Diagonal ──────────────────────────────────────────────────
    object DiagonalMethod     : OverlayType("diagonal",        "Diagonal",       OverlayCategory.TRIANGLE)
    object DynamicSymmetryR2  : OverlayType("root2",           "Root 2 (√2)",    OverlayCategory.TRIANGLE)
    object DynamicSymmetryR5  : OverlayType("root5",           "Root 5 (√5)",    OverlayCategory.TRIANGLE)
    object Rabatment          : OverlayType("rabatment",       "Rabatment",      OverlayCategory.TRIANGLE)
    object HarmonicArmature   : OverlayType("harmonic",        "Harmonic",       OverlayCategory.TRIANGLE)

    // ── Circle & Spiral ──────────────────────────────────────────────────────
    object ConcentricCircles  : OverlayType("concentric",      "Concentric",     OverlayCategory.CIRCLE)

    // ── Cinematic ────────────────────────────────────────────────────────────
    object CinemaScopeBars    : OverlayType("cinemascope",     "2.35:1 Scope",   OverlayCategory.CINEMATIC)
    object Ratio16x9          : OverlayType("16x9",            "16:9 Video",     OverlayCategory.CINEMATIC)
    object Ratio4x3           : OverlayType("4x3",             "4:3 Classic",    OverlayCategory.CINEMATIC)

    // ── Symmetry ─────────────────────────────────────────────────────────────
    object BilateralSymmetry  : OverlayType("bilateral",       "Bilateral",      OverlayCategory.SYMMETRY)
    object RadialSymmetry     : OverlayType("radial",          "Radial",         OverlayCategory.SYMMETRY)
    object HorizonLevel       : OverlayType("horizon",         "Horizon",        OverlayCategory.SYMMETRY)

    companion object {
        val ALL: List<OverlayType> = listOf(
            FibonacciSpiralTL, FibonacciSpiralTR, FibonacciSpiralBL, FibonacciSpiralBR,
            PhiGrid, GoldenRectangle, GoldenTriangle, GoldenDiagonal,
            RuleOfThirds, RuleOfFifths, QuadrantGrid, CentreCross, SquareCrop,
            DiagonalMethod, DynamicSymmetryR2, DynamicSymmetryR5, Rabatment, HarmonicArmature,
            ConcentricCircles,
            CinemaScopeBars, Ratio16x9, Ratio4x3,
            BilateralSymmetry, RadialSymmetry, HorizonLevel
        )

        /** Defaults shown on first launch */
        val DEFAULTS: Set<OverlayType> = setOf(RuleOfThirds)
    }
}

enum class OverlayCategory(val label: String) {
    GOLDEN_RATIO("Golden Ratio"),
    GRID("Grid"),
    TRIANGLE("Triangle & Diagonal"),
    CIRCLE("Circle"),
    CINEMATIC("Cinematic"),
    SYMMETRY("Symmetry")
}