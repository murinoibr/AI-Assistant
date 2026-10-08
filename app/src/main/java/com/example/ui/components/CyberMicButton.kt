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
                durationMillis = when {
                    isListening -> 2000
                    isLoading -> 1000 // Fast energetic rotation while AI processes
                    isSpeaking -> 1600 // Smooth rhythmic rotation while AI speaks
                    else -> 8000
                },
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

    // Dedicated Pulsing Shockwave Scale for AI Processing and Speaking
    val auraScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = when {
            isListening -> 1.28f
            isLoading -> 1.35f // Deep energetic pulse while processing
            isSpeaking -> 1.24f // Rhythmic breathing pulse while AI talks
            else -> 1.05f
        },
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when {
                    isListening -> 750
                    isLoading -> 600 // Fast heartbeat-style pulse
                    isSpeaking -> 850 // Smooth breathing pulse
                    else -> 2400
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auraScale"
    )

    // Secondary ripple shockwave for processing/speaking
    val secondaryRippleScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = when {
            isLoading -> 1.50f
            isSpeaking -> 1.38f
            isListening -> 1.30f
            else -> 1.08f
        },
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when {
                    isLoading -> 800
                    isSpeaking -> 1100
                    else -> 1500
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "secondaryRippleScale"
    )

    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = when {
            isLoading -> 0.65f
            isSpeaking -> 0.55f
            isListening -> 0.50f
            else -> 0.12f
        },
        targetValue = when {
            isLoading -> 0.10f
            isSpeaking -> 0.08f
            isListening -> 0.05f
            else -> 0.02f
        },
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when {
                    isLoading -> 600
                    isSpeaking -> 850
                    isListening -> 750
                    else -> 2400
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auraAlpha"
    )

    // Core button pulsing scale for tactile visual feedback during AI thought or speech
    val corePulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = when {
            isLoading -> 1.08f // Heartbeat expansion during processing
            isSpeaking -> 1.05f // Harmonic vocal resonance expansion
            else -> 1f
        },
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isLoading) 500 else 750,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "corePulseScale"
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

    // Dynamic aura colors depending on the AI state
    val primaryAuraColor = when {
        isLoading -> Color(0xFFFFEA00) // Electric Yellow/Gold while AI thinking/processing
        isSpeaking -> Color(0xFF00E5FF) // Radiant Cyan while AI speaking
        isListening -> Color(0xFFFF2A85) // Hot Magenta while listening to user
        else -> Color(0xFF00E5FF)
    }

    val secondaryAuraColor = when {
        isLoading -> Color(0xFFFF6D00) // Orange flare
        isSpeaking -> Color(0xFF8B5CF6) // Purple flare
        isListening -> Color(0xFF00E5FF) // Cyan flare
        else -> Color(0xFFFF2A85)
    }

    Box(
        modifier = modifier
            .size(142.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer pulsing shockwave rings that expand with voice input level or AI processing/speaking
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = size.width * 0.40f

            // Dynamic voice flare multiplier
            val voiceExpansion = 1f + (animatedVoiceLevel * 0.35f)
            val totalScale = auraScale * voiceExpansion
            val combinedAlpha = (auraAlpha + animatedVoiceLevel * 0.4f).coerceIn(0f, 0.9f)

            // Outer primary shockwave ring
            drawCircle(
                color = primaryAuraColor.copy(alpha = combinedAlpha * 0.55f),
                radius = baseRadius * totalScale * 1.15f,
                center = center
            )
            // Secondary ripple ring (visible during processing and speaking)
            if (isLoading || isSpeaking || isListening) {
                drawCircle(
                    color = secondaryAuraColor.copy(alpha = (combinedAlpha * 0.45f).coerceIn(0f, 0.6f)),
                    radius = baseRadius * secondaryRippleScale,
                    center = center
                )
            }
            // Inner aura ring
            drawCircle(
                color = primaryAuraColor.copy(alpha = combinedAlpha * 0.45f),
                radius = baseRadius * totalScale,
                center = center
            )
        }

        // The rainbow outer ring with glow and rotation
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
                alpha = (if (isLoading || isSpeaking) 0.85f else (0.4f + animatedVoiceLevel * 0.5f)).coerceIn(0f, 0.95f)
            )

            // Draw crisp rainbow ring
            drawCircle(
                brush = Brush.sweepGradient(rainbowColors, center = center),
                radius = radius,
                style = Stroke(width = 3.5.dp.toPx() + extraStroke * 0.5f)
            )
        }

        // Inner dark button core with pulsing scaling while AI is processing or speaking
        val coreScale = (corePulseScale + (animatedVoiceLevel * 0.08f))

        Box(
            modifier = Modifier
                .size(86.dp)
                .scale(coreScale)
                .shadow(
                    elevation = when {
                        isLoading -> 26.dp
                        isSpeaking -> 22.dp
                        isListening -> (16 + (animatedVoiceLevel * 14)).dp
                        else -> 12.dp
                    },
                    shape = CircleShape,
                    spotColor = primaryAuraColor
                )
                .clip(CircleShape)
                .background(Color(0xFF0C101A))
                .border(
                    width = when {
                        isLoading -> 2.5.dp
                        isSpeaking -> 2.dp
                        else -> (1.5 + (animatedVoiceLevel * 1.5)).dp
                    },
                    brush = Brush.linearGradient(
                        colors = listOf(
                            primaryAuraColor.copy(alpha = 0.9f),
                            secondaryAuraColor.copy(alpha = 0.9f)
                        )
                    ),
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    role = androidx.compose.ui.semantics.Role.Button,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            // Microphone Icon with contextual icon and tint
            val iconTint = when {
                isListening -> Color(0xFFFF2A85)
                isLoading -> Color(0xFFFFEA00)
                isSpeaking -> Color(0xFF00E5FF)
                else -> Color(0xFF00E5FF)
            }

            Icon(
                imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                contentDescription = when {
                    isListening -> "Parar de Ouvir"
                    isLoading -> "IA Processando..."
                    isSpeaking -> "IA Falando..."
                    else -> "Falar"
                },
                modifier = Modifier
                    .size(42.dp)
                    .scale(if (isListening) (1.1f + animatedVoiceLevel * 0.12f) else 1f),
                tint = iconTint
            )
        }
    }
}
