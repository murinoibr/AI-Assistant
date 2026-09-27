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
 * Features 360-degree rainbow neon sweep border, dark inner core, and gradient mic icon.
 */
@Composable
fun CyberMicButton(
    isListening: Boolean,
    isSpeaking: Boolean,
    isLoading: Boolean,
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
                durationMillis = if (isListening) 2500 else if (isLoading) 1200 else 8000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rainbowRotation"
    )

    // Pulse scale for shockwave aura
    val auraScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening || isSpeaking) 1.28f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isListening) 800 else if (isSpeaking) 600 else 2400,
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
                durationMillis = if (isListening) 800 else if (isSpeaking) 600 else 2400,
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
        // Outer pulsing shockwave rings
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = size.width * 0.46f

            // Outer subtle glow halo
            drawCircle(
                color = Color(0xFF00E5FF).copy(alpha = auraAlpha * 0.5f),
                radius = baseRadius * auraScale * 1.15f,
                center = center
            )
            drawCircle(
                color = Color(0xFFFF2A85).copy(alpha = auraAlpha * 0.35f),
                radius = baseRadius * auraScale,
                center = center
            )
        }

        // The rainbow outer ring
        Canvas(
            modifier = Modifier
                .size(118.dp)
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = (size.width / 2f) - 4.dp.toPx()

            // Draw glowing outer blur ring
            drawCircle(
                brush = Brush.sweepGradient(rainbowColors, center = center),
                radius = radius,
                style = Stroke(width = 6.dp.toPx()),
                alpha = 0.4f
            )

            // Draw crisp rainbow ring
            drawCircle(
                brush = Brush.sweepGradient(rainbowColors, center = center),
                radius = radius,
                style = Stroke(width = 3.5.dp.toPx())
            )
        }

        // Inner dark button core
        Box(
            modifier = Modifier
                .size(86.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    spotColor = Color(0xFF00E5FF)
                )
                .clip(CircleShape)
                .background(Color(0xFF0C101A))
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF00E5FF).copy(alpha = 0.8f),
                            Color(0xFFFF2A85).copy(alpha = 0.8f)
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
                    .scale(if (isListening) 1.1f else 1f),
                tint = if (isListening) Color(0xFFFF2A85) else Color(0xFF00E5FF)
            )
        }
    }
}
