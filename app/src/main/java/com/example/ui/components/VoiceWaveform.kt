package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin

/**
 * Dynamic visual waveform animation that reacts in real-time to the user's
 * voice input level (RMSdB) while recording or AI voice output while speaking.
 */
@Composable
fun VoiceWaveform(
    isListening: Boolean,
    isSpeaking: Boolean,
    audioLevel: Float = 0f, // 0.0f to 1.0f real-time voice input level
    modifier: Modifier = Modifier
) {
    // Smooth, organic spring physics for audio level reaction
    val animatedAudioLevel by animateFloatAsState(
        targetValue = when {
            isListening -> audioLevel.coerceIn(0.08f, 1f)
            isSpeaking -> 0.65f
            else -> 0.05f
        },
        animationSpec = spring(
            dampingRatio = 0.62f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "animatedAudioLevel"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "waveform_motion")

    // Continuous wave phase animation
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isListening) 900 else if (isSpeaking) 1200 else 2800,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveformPhase"
    )

    val waveColors = when {
        isListening -> listOf(
            Color(0xFF00E5FF), // Cyan
            Color(0xFFFF2A85), // Vivid Magenta
            Color(0xFFFFEA00)  // Neon Yellow peak
        )
        isSpeaking -> listOf(
            Color(0xFF8B5CF6), // Purple
            Color(0xFF00E5FF), // Cyan
            Color(0xFF00E676)  // Emerald Green
        )
        else -> listOf(
            Color(0xFF00E5FF).copy(alpha = 0.4f),
            Color(0xFF38BDF8).copy(alpha = 0.3f)
        )
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        val width = size.width
        val height = size.height
        val barCount = 36
        val barSpacingFactor = 0.35f
        val totalSpacing = width * barSpacingFactor
        val barWidth = (width - totalSpacing) / barCount
        val spacing = totalSpacing / (barCount - 1)
        val centerY = height / 2f

        for (i in 0 until barCount) {
            val progress = i.toFloat() / (barCount - 1) // 0f to 1f

            // Gaussian bell curve centered in the middle (human voice formant spectrum)
            val bell = exp(-((progress - 0.5) / 0.28).pow(2.0)).toFloat()

            // Dynamic sine variations combined with real-time audio input level
            val waveOscillation = if (isListening) {
                val s1 = sin(progress * 14.0 + phase).toFloat()
                val s2 = cos(progress * 9.0 - phase * 1.3).toFloat()
                val flutter = sin(phase * 4.0 + i).toFloat() * 0.15f
                ((s1 * 0.45f + s2 * 0.4f + flutter + 1f) * 0.5f).coerceIn(0.1f, 1f)
            } else if (isSpeaking) {
                val s1 = sin(progress * 12.0 - phase * 1.4).toFloat()
                val s2 = sin(progress * 7.0 + phase).toFloat()
                ((s1 * s2 * 0.45f + 0.65f)).coerceIn(0.15f, 1f)
            } else {
                val s = sin(progress * 4.0 + phase * 0.4).toFloat()
                (s * 0.1f + 0.18f).coerceIn(0.08f, 0.25f)
            }

            // Real-time scaled multiplier: baseline + dynamic loudness reaction
            val reactiveHeightMultiplier = if (isListening) {
                // Minimum idle bar height + amplified voice input level response
                (0.12f + (animatedAudioLevel * 0.88f) * (0.35f + 0.65f * bell) * waveOscillation)
                    .coerceIn(0.10f, 1.0f)
            } else if (isSpeaking) {
                (0.18f + 0.75f * bell * waveOscillation).coerceIn(0.12f, 0.95f)
            } else {
                (0.10f + 0.10f * waveOscillation).coerceIn(0.06f, 0.22f)
            }

            val barHeight = (height * reactiveHeightMultiplier).coerceAtLeast(barWidth)
            val x = i * (barWidth + spacing)
            val y = centerY - (barHeight / 2f)

            // Draw glowing background shadow when user is actively speaking loud
            if (isListening && animatedAudioLevel > 0.25f) {
                drawRoundRect(
                    color = Color(0xFF00E5FF).copy(alpha = (animatedAudioLevel * 0.35f).coerceIn(0f, 0.4f)),
                    topLeft = Offset(x - 1.5.dp.toPx(), y - 2.dp.toPx()),
                    size = Size(barWidth + 3.dp.toPx(), barHeight + 4.dp.toPx()),
                    cornerRadius = CornerRadius(barWidth, barWidth)
                )
            }

            // Draw main neon gradient bar
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
