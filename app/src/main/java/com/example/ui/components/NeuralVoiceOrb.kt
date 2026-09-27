package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun NeuralVoiceOrb(
    isListening: Boolean,
    isSpeaking: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_pulse")

    // Breathing scale for idle state
    val idleBreathing by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idle_breathing"
    )

    // Fast rotation for active/thinking states
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isLoading) 1200 else 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Ripple wave for listening/speaking
    val rippleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple"
    )

    // Core scale
    val activeScale = when {
        isListening -> 1.12f
        isSpeaking -> 1.08f
        isLoading -> 1.02f
        else -> idleBreathing
    }

    val animatedScale by animateFloatAsState(
        targetValue = activeScale,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow),
        label = "scale"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(animatedScale)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = NeonCyan),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerOffset = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = this.size.minDimension / 2f

            // Outer reactive glow aura
            val auraColors = when {
                isListening -> listOf(
                    VividMagenta.copy(alpha = 0.45f),
                    NeonCyan.copy(alpha = 0.25f),
                    Color.Transparent
                )
                isSpeaking -> listOf(
                    NeonCyan.copy(alpha = 0.45f),
                    ElectricViolet.copy(alpha = 0.25f),
                    Color.Transparent
                )
                isLoading -> listOf(
                    GoldenAmber.copy(alpha = 0.35f),
                    ElectricViolet.copy(alpha = 0.25f),
                    Color.Transparent
                )
                else -> listOf(
                    NeonBlue.copy(alpha = 0.25f),
                    ElectricViolet.copy(alpha = 0.15f),
                    Color.Transparent
                )
            }

            drawCircle(
                brush = Brush.radialGradient(
                    colors = auraColors,
                    center = centerOffset,
                    radius = radius
                ),
                radius = radius,
                center = centerOffset
            )

            // Expanding ripple rings when listening or speaking
            if (isListening || isSpeaking) {
                val rippleRadius = radius * 0.5f + (radius * 0.45f * rippleProgress)
                val rippleAlpha = (1f - rippleProgress).coerceIn(0f, 1f) * 0.6f
                drawCircle(
                    color = if (isListening) VividMagenta.copy(alpha = rippleAlpha) else NeonCyan.copy(alpha = rippleAlpha),
                    radius = rippleRadius,
                    center = centerOffset,
                    style = Stroke(width = 3.dp.toPx())
                )
            }

            // Energy core with layered sphere
            val coreRadius = radius * 0.62f
            val coreAngleRad = Math.toRadians(rotation.toDouble())
            val coreOffsetX = (cos(coreAngleRad) * 12).toFloat()
            val coreOffsetY = (sin(coreAngleRad) * 12).toFloat()

            val coreColors = when {
                isListening -> listOf(VividMagenta, ElectricViolet, NeonCyan)
                isSpeaking -> listOf(NeonCyan, NeonBlue, ElectricViolet)
                isLoading -> listOf(GoldenAmber, VividMagenta, ElectricViolet)
                else -> listOf(NeonCyan, NeonBlue, ElectricViolet)
            }

            drawCircle(
                brush = Brush.radialGradient(
                    colors = coreColors,
                    center = Offset(centerOffset.x + coreOffsetX, centerOffset.y + coreOffsetY),
                    radius = coreRadius
                ),
                radius = coreRadius,
                center = centerOffset
            )

            // Frosted glass highlight ring
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.7f),
                        Color.Transparent,
                        NeonCyan.copy(alpha = 0.8f),
                        Color.Transparent,
                        Color.White.copy(alpha = 0.7f)
                    ),
                    center = centerOffset
                ),
                radius = coreRadius,
                center = centerOffset,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // Center Icon Indicator
        val icon = when {
            isListening -> Icons.Default.Mic
            isSpeaking -> Icons.Default.GraphicEq
            isLoading -> Icons.Default.MicOff
            else -> Icons.Default.Mic
        }

        Icon(
            imageVector = icon,
            contentDescription = if (isListening) "Ouvindo" else "Toque para falar",
            tint = Color.White,
            modifier = Modifier.size(size * 0.26f)
        )
    }
}
