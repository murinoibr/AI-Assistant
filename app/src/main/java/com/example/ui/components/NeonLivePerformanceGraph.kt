package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.sin

/**
 * Animated rainbow wave curve recreating the "LIVE PERFORMANCE" graph
 * from the reference HUD screenshot.
 */
@Composable
fun NeonLivePerformanceGraph(
    isLive: Boolean = true,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "graphWave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "graphPhase"
    )

    val rainbowBrush = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFFF2A85), // Magenta / Pink on the left
            Color(0xFF8B5CF6), // Purple
            Color(0xFF00E5FF), // Cyan in middle
            Color(0xFF00E676)  // Neon Green on the right
        )
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(26.dp)
    ) {
        val w = size.width
        val h = size.height

        val path = Path()
        // Start point
        path.moveTo(0f, h * 0.7f)

        // Draw smooth wavy bezier curve matching the screenshot
        val cp1x = w * 0.22f
        val cp1y = h * (0.85f + 0.15f * sin(phase))

        val p1x = w * 0.42f
        val p1y = h * (0.45f - 0.2f * sin(phase + 1.2f))

        val cp2x = w * 0.65f
        val cp2y = h * (0.15f + 0.15f * sin(phase + 2.4f))

        val p2x = w * 0.82f
        val p2y = h * (0.35f + 0.1f * sin(phase + 3.6f))

        val endX = w
        val endY = h * 0.2f

        path.cubicTo(cp1x, cp1y, p1x, p1y, cp2x, cp2y)
        path.cubicTo(cp2x, cp2y, p2x, p2y, endX, endY)

        // Draw glowing background stroke
        drawPath(
            path = path,
            brush = rainbowBrush,
            style = Stroke(
                width = 5.5f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            ),
            alpha = 0.4f
        )

        // Draw crisp foreground stroke
        drawPath(
            path = path,
            brush = rainbowBrush,
            style = Stroke(
                width = 3f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}
