package com.example.quantpricetracker.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Autoscaled price sparkline. Each instance is scaled to its own min/max
 * so the visual shape is readable regardless of absolute price magnitude.
 *
 * In ambient mode: monochrome, thinner stroke, no end-dot (burn-in safety).
 */
@Composable
fun Sparkline(
    points: List<Double>,
    modifier: Modifier = Modifier,
    color: Color = IsowatchTokens.AccentBlue,
    ambient: Boolean = false
) {
    if (points.size < 2) return

    val strokeColor = if (ambient) IsowatchTokens.AmbientTextDim else color
    val strokeWidthDp = if (ambient) 1.0f else 1.6f

    Canvas(modifier = modifier) {
        val min = points.min()
        val max = points.max()
        val range = (max - min).takeIf { it > 0 } ?: 1.0

        val stepX = size.width / (points.size - 1)
        val path = Path()

        points.forEachIndexed { i, v ->
            val x = i * stepX
            val yNorm = ((v - min) / range).toFloat()
            val y = size.height - (yNorm * size.height)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = strokeColor,
            style = Stroke(width = strokeWidthDp.dp.toPx(), cap = StrokeCap.Round)
        )

        if (!ambient) {
            val lastX = (points.size - 1) * stepX
            val lastYNorm = ((points.last() - min) / range).toFloat()
            val lastY = size.height - (lastYNorm * size.height)
            drawCircle(color = strokeColor, radius = 2.2f.dp.toPx(), center = Offset(lastX, lastY))
        }
    }
}
