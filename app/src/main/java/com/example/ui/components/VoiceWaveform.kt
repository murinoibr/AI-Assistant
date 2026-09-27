package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.VividMagenta
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun VoiceWaveform(
    isListening: Boolean,
    isSpeaking: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "futuristic_waveform")

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val waveColors = when {
        isListening -> listOf(VividMagenta, NeonCyan)
        isSpeaking -> listOf(NeonCyan, NeonBlue, ElectricViolet)
        else -> listOf(NeonCyan.copy(alpha = 0.3f), NeonBlue.copy(alpha = 0.3f))
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        val width = size.width
        val height = size.height
        val barCount = 32
        val totalSpacing = width * 0.4f
        val barWidth = (width - totalSpacing) / barCount
        val spacing = totalSpacing / (barCount - 1)

        val centerY = height / 2f

        for (i in 0 until barCount) {
            val progress = i.toFloat() / barCount
            val multiplier = when {
                isListening -> {
                    val s1 = sin(progress * 12.0 + phase)
                    val s2 = cos(progress * 8.0 - phase * 0.7)
                    ((s1 + s2) * 0.35 + 0.5).coerceIn(0.12, 0.95).toFloat()
                }
                isSpeaking -> {
                    val s1 = sin(progress * 14.0 - phase * 1.5)
                    val s2 = sin(progress * 6.0 + phase)
                    ((s1 * s2) * 0.45 + 0.55).coerceIn(0.15, 1.0).toFloat()
                }
                else -> {
                    val s = sin(progress * 4.0 + phase * 0.3)
                    (s * 0.08 + 0.12).coerceIn(0.06, 0.2).toFloat()
                }
            }

            val barHeight = height * multiplier
            val x = i * (barWidth + spacing)
            val y = centerY - (barHeight / 2f)

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = waveColors,
                    startY = y,
                    endY = y + barHeight
                ),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}
