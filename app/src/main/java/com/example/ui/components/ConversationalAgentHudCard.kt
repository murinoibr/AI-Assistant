package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AgentEntity

/**
 * HUD Card matching "CONVERSATIONAL AGENT: PROJECT ALPHA" with
 * INTEGRATED MODULES and LIVE PERFORMANCE panels from the reference screenshot.
 */
@Composable
fun ConversationalAgentHudCard(
    currentAgent: AgentEntity?,
    onSwitchAgentClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val agentName = currentAgent?.name?.uppercase() ?: "PROJECT ALPHA"

    val rainbowBorder = Brush.linearGradient(
        colors = listOf(
            Color(0xFF00E5FF).copy(alpha = 0.85f),
            Color(0xFF8B5CF6).copy(alpha = 0.7f),
            Color(0xFFFF2A85).copy(alpha = 0.85f),
            Color(0xFFFF7A00).copy(alpha = 0.7f),
            Color(0xFF00E676).copy(alpha = 0.8f)
        )
    )

    // Outer HUD Container
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF0D121F).copy(alpha = 0.92f))
            .border(width = 1.5.dp, brush = rainbowBorder, shape = RoundedCornerShape(22.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row: CONVERSATIONAL AGENT:
            AgentHeaderRow(
                agentName = agentName,
                currentAgent = currentAgent,
                onSwitchAgentClick = onSwitchAgentClick
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Two Sub-cards row: INTEGRATED MODULES and LIVE PERFORMANCE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // LEFT CARD: INTEGRATED MODULES
                IntegratedModulesCard(modifier = Modifier.weight(1f))

                // RIGHT CARD: LIVE PERFORMANCE
                LivePerformanceCard(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun AgentHeaderRow(
    agentName: String,
    currentAgent: AgentEntity?,
    onSwitchAgentClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Button) { onSwitchAgentClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "CONVERSATIONAL AGENT:",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = agentName,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.SwapVert,
                    contentDescription = "Trocar Agente",
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Emoji / Category Chip
        if (currentAgent != null) {
            AgentCategoryChip(currentAgent)
        }
    }
}

@Composable
private fun AgentCategoryChip(
    currentAgent: AgentEntity,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF1E293B).copy(alpha = 0.7f))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "${currentAgent.emoji} ${currentAgent.category}",
            color = Color(0xFFCBD5E1),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun IntegratedModulesCard(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF080B12))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp, horizontal = 10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mic Icon with glowing gradient circle
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFF2A85).copy(alpha = 0.25f),
                                Color(0xFF00E5FF).copy(alpha = 0.25f)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        Color(0xFFFF2A85).copy(alpha = 0.6f),
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = Color(0xFFFF2A85),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "INTEGRATED MODULES",
                color = Color(0xFF94A3B8),
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Green / Teal Module: Vertex AI / Gemini
            Text(
                text = "Vertex AI",
                color = Color(0xFF2DD4BF), // Teal / Green
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            // Orange Module: Dialogflow / TTS
            Text(
                text = "Dialogflow",
                color = Color(0xFFFB923C), // Amber / Orange
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun LivePerformanceCard(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF080B12))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
            .padding(vertical = 10.dp, horizontal = 10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top row: Bar chart icon + Animated Rainbow Graph
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null,
                    tint = Color(0xFFFF2A85),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                NeonLivePerformanceGraph(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "LIVE PERFORMANCE",
                color = Color(0xFF94A3B8),
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Stats: 95% Training | 4.8/5 Rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "95%",
                        color = Color(0xFF38BDF8),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Training",
                        color = Color(0xFF64748B),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(20.dp)
                        .background(Color(0xFF1E293B))
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "4.8/5",
                        color = Color(0xFF4ADE80),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Rating",
                        color = Color(0xFF64748B),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
