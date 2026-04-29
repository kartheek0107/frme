package com.fibcam.ui.overlay

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import com.fibcam.model.OverlayType
import kotlin.math.min
import kotlin.math.sqrt

private const val PHI = 1.6180339887f

/**
 * Stateless drawing functions for every overlay type.
 * Called from a Compose Canvas each frame — zero allocation inside hot path.
 */
object OverlayRenderer {

    fun DrawScope.drawOverlay(
        type: OverlayType,
        color: Color,
        alpha: Float,
        strokePx: Float = 1.5f
    ) {
        val stroke = Stroke(strokePx)
        when (type) {
            is OverlayType.RuleOfThirds       -> drawRuleOfThirds(color, alpha, stroke)
            is OverlayType.RuleOfFifths       -> drawRuleOfFifths(color, alpha, stroke)
            is OverlayType.QuadrantGrid       -> drawQuadrantGrid(color, alpha, stroke)
            is OverlayType.CentreCross        -> drawCentreCross(color, alpha, stroke)
            is OverlayType.SquareCrop         -> drawSquareCrop(color, alpha, stroke)
            is OverlayType.PhiGrid            -> drawPhiGrid(color, alpha, stroke)
            is OverlayType.GoldenRectangle    -> drawGoldenRectangle(color, alpha, stroke)
            is OverlayType.GoldenTriangle     -> drawGoldenTriangle(color, alpha, stroke)
            is OverlayType.GoldenDiagonal     -> drawGoldenDiagonal(color, alpha, stroke)
            is OverlayType.FibonacciSpiralTL  -> drawFibSpiral(color, alpha, stroke, 0)
            is OverlayType.FibonacciSpiralTR  -> drawFibSpiral(color, alpha, stroke, 1)
            is OverlayType.FibonacciSpiralBR  -> drawFibSpiral(color, alpha, stroke, 2)
            is OverlayType.FibonacciSpiralBL  -> drawFibSpiral(color, alpha, stroke, 3)
            is OverlayType.DiagonalMethod     -> drawDiagonalMethod(color, alpha, stroke)
            is OverlayType.DynamicSymmetryR2  -> drawDynamicSymmetry(color, alpha, stroke, sqrt(2f))
            is OverlayType.DynamicSymmetryR5  -> drawDynamicSymmetry(color, alpha, stroke, sqrt(5f))
            is OverlayType.Rabatment          -> drawRabatment(color, alpha, stroke)
            is OverlayType.HarmonicArmature   -> drawHarmonicArmature(color, alpha, stroke)
            is OverlayType.ConcentricCircles   -> drawConcentricCircles(color, alpha, stroke)
            is OverlayType.CinemaScopeBars    -> drawCinemaBars(color, alpha, 2.35f)
            is OverlayType.Ratio16x9          -> drawCinemaBars(color, alpha, 16f / 9f)
            is OverlayType.Ratio4x3           -> drawCinemaBars(color, alpha, 4f / 3f)
            is OverlayType.BilateralSymmetry  -> drawBilateralSymmetry(color, alpha, stroke)
            is OverlayType.RadialSymmetry     -> drawRadialSymmetry(color, alpha, stroke)
            is OverlayType.HorizonLevel       -> drawHorizonLevel(color, alpha, stroke)
            else -> Unit
        }
    }

    // ── Grid overlays ─────────────────────────────────────────────────────────

    private fun DrawScope.drawRuleOfThirds(color: Color, alpha: Float, stroke: Stroke) {
        val w = size.width; val h = size.height
        for (i in 1..2) {
            val x = w * i / 3f
            val y = h * i / 3f
            drawLine(color, Offset(x, 0f), Offset(x, h), stroke.width, alpha = alpha)
            drawLine(color, Offset(0f, y), Offset(w, y), stroke.width, alpha = alpha)
        }
        // Power points
        for (i in 1..2) for (j in 1..2) {
            drawCircle(color, 6f, Offset(w * i / 3f, h * j / 3f), alpha = alpha)
        }
    }

    private fun DrawScope.drawRuleOfFifths(color: Color, alpha: Float, stroke: Stroke) {
        val w = size.width; val h = size.height
        for (i in 1..4) {
            drawLine(color, Offset(w * i / 5f, 0f), Offset(w * i / 5f, h), stroke.width, alpha = alpha)
            drawLine(color, Offset(0f, h * i / 5f), Offset(w, h * i / 5f), stroke.width, alpha = alpha)
        }
    }

    private fun DrawScope.drawQuadrantGrid(color: Color, alpha: Float, stroke: Stroke) {
        val w = size.width; val h = size.height
        drawLine(color, Offset(w / 2f, 0f), Offset(w / 2f, h), stroke.width, alpha = alpha)
        drawLine(color, Offset(0f, h / 2f), Offset(w, h / 2f), stroke.width, alpha = alpha)
    }

    private fun DrawScope.drawCentreCross(color: Color, alpha: Float, stroke: Stroke) {
        drawQuadrantGrid(color, alpha, stroke)
        drawCircle(color, 16f, Offset(size.width / 2f, size.height / 2f), style = stroke, alpha = alpha)
    }

    private fun DrawScope.drawSquareCrop(color: Color, alpha: Float, stroke: Stroke) {
        val w = size.width; val h = size.height
        val side = min(w, h)
        val left = (w - side) / 2f
        val top  = (h - side) / 2f
        drawRect(color, Offset(left, top), Size(side, side), alpha = alpha, style = stroke)
    }

    // ── Golden ratio overlays ─────────────────────────────────────────────────

    private fun DrawScope.drawPhiGrid(color: Color, alpha: Float, stroke: Stroke) {
        val w = size.width; val h = size.height
        val xOffset = w / (PHI + 1f)
        val yOffset = h / (PHI + 1f)
        // Phi lines at 1 : φ : 1 ratio
        listOf(xOffset, w - xOffset).forEach { x ->
            drawLine(color, Offset(x, 0f), Offset(x, h), stroke.width, alpha = alpha)
        }
        listOf(yOffset, h - yOffset).forEach { y ->
            drawLine(color, Offset(0f, y), Offset(w, y), stroke.width, alpha = alpha)
        }
    }

    private fun DrawScope.drawGoldenRectangle(color: Color, alpha: Float, stroke: Stroke) {
        val w = size.width; val h = size.height
        // Largest golden rectangle centred in frame
        val rectW: Float; val rectH: Float
        if (w / h > PHI) {
            rectH = h * 0.8f; rectW = rectH * PHI
        } else {
            rectW = w * 0.8f; rectH = rectW / PHI
        }
        val left = (w - rectW) / 2f
        val top  = (h - rectH) / 2f
        drawRect(color, Offset(left, top), Size(rectW, rectH), alpha = alpha, style = stroke)
        // Sub-rectangle
        drawLine(color, Offset(left + rectH, top), Offset(left + rectH, top + rectH), stroke.width, alpha = alpha * 0.5f)
    }

    private fun DrawScope.drawGoldenTriangle(color: Color, alpha: Float, stroke: Stroke) {
        val w = size.width; val h = size.height
        // Main diagonal
        drawLine(color, Offset(0f, h), Offset(w, 0f), stroke.width, alpha = alpha)
        // Perpendicular from TL to diagonal
        val px = (w * h * h) / (w * w + h * h)
        val py = (h * w * w) / (w * w + h * h)
        drawLine(color, Offset(0f, 0f), Offset(px, py), stroke.width, alpha = alpha)
        // Perpendicular from BR to diagonal
        drawLine(color, Offset(w, h), Offset(w - px, h - py), stroke.width, alpha = alpha)
    }

    private fun DrawScope.drawGoldenDiagonal(color: Color, alpha: Float, stroke: Stroke) {
        val w = size.width; val h = size.height
        drawLine(color, Offset(0f, 0f), Offset(w, h), stroke.width, alpha = alpha)
        drawLine(color, Offset(w, 0f), Offset(0f, h), stroke.width, alpha = alpha)
        // Perpendiculars
        val slope = h / w
        val perpSlope = -w / h
        // Drop from top-right to main diagonal
        val ix = w / (1 + (slope * perpSlope * -1))
        drawLine(color, Offset(w, 0f), Offset(ix, ix * slope), stroke.width, alpha = alpha * 0.6f)
        drawLine(color, Offset(0f, h), Offset(w - ix, h - ix * slope), stroke.width, alpha = alpha * 0.6f)
    }

    // ── Fibonacci spiral ──────────────────────────────────────────────────────

    /**
     * Draws an approximated Fibonacci spiral using quarter-circle arcs
     * orientation: 0=TL, 1=TR, 2=BR, 3=BL
     */
    private fun DrawScope.drawFibSpiral(color: Color, alpha: Float, stroke: Stroke, orientation: Int) {
        val w = size.width; val h = size.height
        withTransform({
            if (orientation == 1 || orientation == 2) scale(-1f, 1f, Offset(w / 2f, h / 2f))
            if (orientation == 2 || orientation == 3) scale(1f, -1f, Offset(w / 2f, h / 2f))
        }) {
            drawFibSpiralTL(color, alpha, stroke)
        }
    }

    private fun DrawScope.drawFibSpiralTL(color: Color, alpha: Float, stroke: Stroke) {
        val w = size.width; val h = size.height

        // Generate fib sizes to fill the larger dimension
        val sizes = mutableListOf<Float>()
        var a = 1f; var b = 1f
        val target = maxOf(w, h)
        while (b < target) { sizes.add(a); val tmp = a + b; a = b; b = tmp }

        // Scale so the two largest fit the frame
        val totalLen = sizes.last() + sizes[sizes.size - 2]
        val scale = minOf(w, h) / (sizes.last() + sizes[sizes.size - 2])
        val scaled = sizes.map { it * scale }

        // Draw spiral rectangles outline + arcs
        var rx = 0f; var ry = 0f
        val directions = listOf(
            intArrayOf(1, 0), intArrayOf(0, 1),
            intArrayOf(-1, 0), intArrayOf(0, -1)
        )
        var cornerX = w * (1f - 1f / PHI); var cornerY = 0f

        val path = Path()
        path.moveTo(cornerX, cornerY)

        val spiralCount = minOf(scaled.size, 8)
        var curX = cornerX; var curY = cornerY
        var boxX = 0f; var boxY = 0f
        var dir = 2   // start direction index

        for (i in spiralCount - 1 downTo 0) {
            val s = scaled[i]
            val d = directions[dir % 4]
            val arcStartAngle = when (dir % 4) {
                0 -> 180f; 1 -> 270f; 2 -> 0f; else -> 90f
            }
            val arcLeft  = curX - s * if (d[0] < 0) 0f else if (d[0] > 0) 1f else 0.5f
            val arcTop   = curY - s * if (d[1] < 0) 0f else if (d[1] > 0) 1f else 0.5f
            // Simplified: draw arc from current corner
            path.arcTo(
                rect = androidx.compose.ui.geometry.Rect(
                    left = curX.coerceIn(0f, w) - s,
                    top  = curY.coerceIn(0f, h) - s,
                    right = curX.coerceIn(0f, w),
                    bottom= curY.coerceIn(0f, h)
                ),
                startAngleDegrees = arcStartAngle,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            curX += d[0] * s
            curY += d[1] * s
            dir++
        }

        drawPath(path, color, alpha = alpha, style = stroke)
    }

    // ── Triangle / Diagonal overlays ──────────────────────────────────────────

    private fun DrawScope.drawDiagonalMethod(color: Color, alpha: Float, stroke: Stroke) {
        val w = size.width; val h = size.height
        drawLine(color, Offset(0f, 0f), Offset(w, h), stroke.width, alpha = alpha)
        drawLine(color, Offset(w, 0f), Offset(0f, h), stroke.width, alpha = alpha)
    }

    private fun DrawScope.drawDynamicSymmetry(color: Color, alpha: Float, stroke: Stroke, root: Float) {
        val w = size.width; val h = size.height
        // Main diagonal
        drawLine(color, Offset(0f, 0f), Offset(w, h), stroke.width, alpha = alpha)
        // Root rectangle width
        val rw = h * root
        if (rw < w) {
            drawLine(color, Offset(rw, 0f), Offset(rw, h), stroke.width, alpha = alpha)
            drawLine(color, Offset(rw, 0f), Offset(0f, h), stroke.width, alpha = alpha * 0.6f)
        }
    }

    private fun DrawScope.drawRabatment(color: Color, alpha: Float, stroke: Stroke) {
        val w = size.width; val h = size.height
        val s = min(w, h)
        // Square from left edge
        drawRect(color, Offset(0f, 0f), Size(s, h), alpha = alpha, style = stroke)
        // Square from right edge
        drawRect(color, Offset(w - s, 0f), Size(s, h), alpha = alpha, style = stroke)
    }

    private fun DrawScope.drawHarmonicArmature(color: Color, alpha: Float, stroke: Stroke) {
        val w = size.width; val h = size.height
        // All four diagonals
        drawLine(color, Offset(0f, 0f), Offset(w, h), stroke.width, alpha = alpha)
        drawLine(color, Offset(w, 0f), Offset(0f, h), stroke.width, alpha = alpha)
        // Perpendicular from each corner to opposite diagonal
        val slope = h / w
        val a = 1f; val b = -slope; val c = 0f
        val px = (b * (b * w - a * 0f) - a * c) / (a * a + b * b)
        val py = (a * (-b * w + a * 0f) - b * c) / (a * a + b * b)
        drawLine(color, Offset(0f, h), Offset(-py / slope, -py), stroke.width, alpha = alpha * 0.5f)
        drawLine(color, Offset(w, 0f), Offset(w - px, h - py), stroke.width, alpha = alpha * 0.5f)
    }

    // ── Circle ────────────────────────────────────────────────────────────────

    private fun DrawScope.drawConcentricCircles(color: Color, alpha: Float, stroke: Stroke) {
        val cx = size.width / 2f; val cy = size.height / 2f
        val maxR = min(size.width, size.height) / 2f
        for (i in 1..4) {
            drawCircle(color, maxR * i / 4f, Offset(cx, cy), alpha = alpha * (0.3f + i * 0.175f), style = stroke)
        }
    }

    // ── Cinematic crop bars ───────────────────────────────────────────────────

    private fun DrawScope.drawCinemaBars(color: Color, alpha: Float, targetRatio: Float) {
        val w = size.width; val h = size.height
        val currentRatio = w / h
        if (currentRatio > targetRatio) {
            // Pillarbox
            val barW = (w - h * targetRatio) / 2f
            drawRect(color.copy(alpha = alpha * 0.5f), Offset(0f, 0f), Size(barW, h))
            drawRect(color.copy(alpha = alpha * 0.5f), Offset(w - barW, 0f), Size(barW, h))
        } else {
            // Letterbox
            val barH = (h - w / targetRatio) / 2f
            drawRect(color.copy(alpha = alpha * 0.5f), Offset(0f, 0f), Size(w, barH))
            drawRect(color.copy(alpha = alpha * 0.5f), Offset(0f, h - barH), Size(w, barH))
        }
    }

    // ── Symmetry ─────────────────────────────────────────────────────────────

    private fun DrawScope.drawBilateralSymmetry(color: Color, alpha: Float, stroke: Stroke) {
        val w = size.width; val h = size.height
        drawLine(color, Offset(w / 2f, 0f), Offset(w / 2f, h), stroke.width, alpha = alpha)
    }

    private fun DrawScope.drawRadialSymmetry(color: Color, alpha: Float, stroke: Stroke) {
        val cx = size.width / 2f; val cy = size.height / 2f
        val r = maxOf(size.width, size.height)
        for (i in 0 until 12) {
            val angle = Math.toRadians(i * 30.0)
            drawLine(
                color,
                Offset(cx, cy),
                Offset(cx + (r * Math.cos(angle)).toFloat(), cy + (r * Math.sin(angle)).toFloat()),
                stroke.width,
                alpha = alpha * 0.5f
            )
        }
    }

    private fun DrawScope.drawHorizonLevel(color: Color, alpha: Float, stroke: Stroke) {
        val w = size.width; val h = size.height
        // Centre horizon
        drawLine(color, Offset(w * 0.1f, h / 2f), Offset(w * 0.9f, h / 2f), stroke.width, alpha = alpha)
        // Tick marks at thirds
        for (x in listOf(w / 3f, w / 2f, 2 * w / 3f)) {
            drawLine(color, Offset(x, h / 2f - 12f), Offset(x, h / 2f + 12f), stroke.width, alpha = alpha)
        }
    }
}