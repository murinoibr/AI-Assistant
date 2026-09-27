package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.sin

/**
 * Cybernetic Particle Nebula canvas recreating the stardust / particle wave
 * seen in the reference HUD design.
 */
@Composable
fun CyberParticleNebula(
    isListening: Boolean,
    isSpeaking: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "particleWave")
    
    // Wave animation phase
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    // Pulse animation based on active voice
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isListening || isSpeaking) 700 else 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Fixed random seeds for particles so they don't jump every frame
    val particles = remember {
        List(110) { index ->
            val u = (index * 0.73f) % 1f
            val v = (index * 0.37f + 0.15f) % 1f
            val size = 1.2f + (index % 4) * 0.9f
            val alphaBase = 0.25f + (index % 5) * 0.15f
            val speed = 0.5f + (index % 3) * 0.5f
            val colorType = index % 4 // 0: Cyan, 1: Teal, 2: Magenta, 3: Violet
            ParticleData(u, v, size, alphaBase, speed, colorType)
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Subtle ambient background gradient
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF0F172A).copy(alpha = 0.45f * pulse),
                    Color(0xFF07090E).copy(alpha = 0.95f),
                    Color(0xFF040508)
                ),
                center = Offset(width * 0.5f, height * 0.65f),
                radius = width * 0.8f
            )
        )

        // Draw particle wave mesh in the lower 60% of the screen
        particles.forEach { p ->
            // Sine wave displacement
            val waveY = sin(phase * p.speed + p.u * 4 * PI).toFloat() * 22f * pulse
            val waveX = sin(phase * 0.7f + p.v * 3 * PI).toFloat() * 14f

            val px = (p.u * width + waveX).coerceIn(0f, width)
            // Concentrate particles in bottom half (0.38f to 0.95f height)
            val py = ((0.38f + p.v * 0.58f) * height + waveY).coerceIn(0f, height)

            val particleColor = when (p.colorType) {
                0 -> Color(0xFF00F2FE) // Cyan
                1 -> Color(0xFF00E676) // Teal / Neon Green
                2 -> Color(0xFFFF2A85) // Magenta / Pink
                else -> Color(0xFF8A2387) // Violet
            }

            val finalAlpha = (p.alphaBase * (if (isListening || isSpeaking) 1.2f else 0.85f)).coerceIn(0f, 1f)

            // Draw soft glow
            drawCircle(
                color = particleColor.copy(alpha = finalAlpha * 0.35f),
                radius = p.size * 2.8f * pulse,
                center = Offset(px, py)
            )
            // Draw core
            drawCircle(
                color = particleColor.copy(alpha = finalAlpha),
                radius = p.size,
                center = Offset(px, py)
            )
        }
    }
}

private data class ParticleData(
    val u: Float,
    val v: Float,
    val size: Float,
    val alphaBase: Float,
    val speed: Float,
    val colorType: Int
)
