package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Bottom Navigation bar from the reference image.
 * Features 3 tabs (Home, Interaction, Settings) with a top rainbow indicator line
 * over the active tab.
 */
@Composable
fun CyberBottomNavigation(
    selectedTab: Int, // 0: Home, 1: Interaction, 2: Settings
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val rainbowBrush = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF00E5FF),
            Color(0xFF00E676),
            Color(0xFFFFEA00),
            Color(0xFFFF2A85),
            Color(0xFF8B5CF6)
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF07090E))
            .padding(bottom = 12.dp, top = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CyberNavItem(
                icon = Icons.Default.Home,
                label = "Home",
                isSelected = selectedTab == 0,
                rainbowBrush = rainbowBrush,
                onClick = { onTabSelected(0) }
            )

            CyberNavItem(
                icon = Icons.Default.ChatBubbleOutline,
                label = "Interaction",
                isSelected = selectedTab == 1,
                rainbowBrush = rainbowBrush,
                onClick = { onTabSelected(1) }
            )

            CyberNavItem(
                icon = Icons.Default.Settings,
                label = "Settings",
                isSelected = selectedTab == 2,
                rainbowBrush = rainbowBrush,
                onClick = { onTabSelected(2) }
            )
        }
    }
}

@Composable
private fun CyberNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    rainbowBrush: Brush,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Rainbow indicator bar right above the icon when selected (exact match with image)
        Box(
            modifier = Modifier
                .height(3.dp)
                .width(36.dp)
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(2.dp))
                        .background(rainbowBrush)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Icon
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = if (isSelected) Color(0xFF00E5FF) else Color(0xFF64748B)
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Text label
        Text(
            text = label,
            color = if (isSelected) Color.White else Color(0xFF64748B),
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
