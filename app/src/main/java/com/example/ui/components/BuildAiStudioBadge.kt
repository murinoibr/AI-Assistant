package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Top Badge matching the "BUILD AI STUDIO" futuristic pill with neon rainbow border
 * from the reference design.
 */
@Composable
fun BuildAiStudioBadge(
    title: String = "AI ASSISTANT",
    modifier: Modifier = Modifier
) {
    val neonBorderGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF00E5FF), // Cyan on left
            Color(0xFF3B82F6), // Blue
            Color(0xFF8B5CF6), // Purple
            Color(0xFFFF2A85), // Magenta on right
            Color(0xFFFF7A00)  // Orange bottom right
        )
    )

    Box(
        modifier = modifier
            .padding(top = 10.dp, bottom = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer glowing shadow pill
        Box(
            modifier = Modifier
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = Color(0xFF00E5FF),
                    ambientColor = Color(0xFFFF2A85)
                )
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0A0D16).copy(alpha = 0.95f))
                .border(
                    width = 2.dp,
                    brush = neonBorderGradient,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 26.dp, vertical = 9.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                color = Color(0xFFE2E8F0),
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.5.sp,
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}
