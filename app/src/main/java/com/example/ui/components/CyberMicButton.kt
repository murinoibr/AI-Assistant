package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Concentric Rainbow Ring Mic Button from the reference design.
 * Features 360-degree rainbow neon sweep border, dark inner core, and gradient mic icon,
 * with real-time reaction to the user's voice input level (RMSdB).
 */
@Composable
fun CyberMicButton(
    isListening: Boolean,
    isSpeaking: Boolean,
    isLoading: Boolean,
    audioLevel: Float = 0f,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "micAnimation")

    // Rotation for rainbow ring when active
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isListening) 2200 else if (isLoading) 1200 else 8000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rainbowRotation"
    )

    // Dynamic voice response with spring physics
    val animatedVoiceLevel by animateFloatAsState(
        targetValue = if (isListening) audioLevel.coerceIn(0f, 1f) else 0f,
        animationSpec = spring(
            dampingRatio = 0.58f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "animatedVoiceLevel"
    )

    // Pulse scale for shockwave aura
    val auraScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening || isSpeaking) 1.25f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isListening) 750 else if (isSpeaking) 600 else 2400,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auraScale"
    )

    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = if (isListening || isSpeaking) 0.5f else 0.15f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isListening) 750 else if (isSpeaking) 600 else 2400,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auraAlpha"
    )

    val rainbowColors = listOf(
        Color(0xFF00E5FF), // Cyan
        Color(0xFF00E676), // Green
        Color(0xFFFFEA00), // Yellow
        Color(0xFFFF6D00), // Orange
        Color(0xFFFF2A85), // Magenta
        Color(0xFF8B5CF6), // Purple
        Color(0xFF00E5FF)  // Back to Cyan
    )

    Box(
        modifier = modifier
            .size(136.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer pulsing shockwave rings that expand with voice input level
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = size.width * 0.44f

            // Dynamic voice flare multiplier
            val voiceExpansion = 1f + (animatedVoiceLevel * 0.35f)
            val totalScale = auraScale * voiceExpansion
            val combinedAlpha = (auraAlpha + animatedVoiceLevel * 0.4f).coerceIn(0f, 0.8f)

            // Outer subtle glow halo
            drawCircle(
                color = Color(0xFF00E5FF).copy(alpha = combinedAlpha * 0.5f),
                radius = baseRadius * totalScale * 1.18f,
                center = center
            )
            drawCircle(
                color = Color(0xFFFF2A85).copy(alpha = combinedAlpha * 0.45f),
                radius = baseRadius * totalScale,
                center = center
            )
        }

        // The rainbow outer ring with glow
        Canvas(
            modifier = Modifier
                .size(118.dp)
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = (size.width / 2f) - 4.dp.toPx()

            // Dynamic ring thickness with audio level
            val extraStroke = (animatedVoiceLevel * 3.dp.toPx())

            // Draw glowing outer blur ring
            drawCircle(
                brush = Brush.sweepGradient(rainbowColors, center = center),
                radius = radius,
                style = Stroke(width = 6.dp.toPx() + extraStroke),
                alpha = (0.4f + animatedVoiceLevel * 0.5f).coerceIn(0f, 0.9f)
            )

            // Draw crisp rainbow ring
            drawCircle(
                brush = Brush.sweepGradient(rainbowColors, center = center),
                radius = radius,
                style = Stroke(width = 3.5.dp.toPx() + extraStroke * 0.5f)
            )
        }

        // Inner dark button core with subtle scaling on loud audio
        Box(
            modifier = Modifier
                .size(86.dp)
                .scale(1f + (animatedVoiceLevel * 0.08f))
                .shadow(
                    elevation = (16 + (animatedVoiceLevel * 14)).dp,
                    shape = CircleShape,
                    spotColor = if (isListening) Color(0xFFFF2A85) else Color(0xFF00E5FF)
                )
                .clip(CircleShape)
                .background(Color(0xFF0C101A))
                .border(
                    width = (1.5 + (animatedVoiceLevel * 1.5)).dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF00E5FF).copy(alpha = 0.85f),
                            Color(0xFFFF2A85).copy(alpha = 0.85f)
                        )
                    ),
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            // Microphone Icon with gradient styling
            Icon(
                imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                contentDescription = if (isListening) "Parar de Ouvir" else "Falar",
                modifier = Modifier
                    .size(42.dp)
                    .scale(if (isListening) (1.1f + animatedVoiceLevel * 0.12f) else 1f),
                tint = if (isListening) Color(0xFFFF2A85) else Color(0xFF00E5FF)
            )
        }
    }
}
