package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AgentEntity
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentLibraryBottomSheet(
    viewModel: MainViewModel,
    agents: List<AgentEntity>,
    selectedAgent: AgentEntity?,
    onDismiss: () -> Unit,
    onOpenVoiceCreate: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BackgroundObsidian,
        scrimColor = Color.Black.copy(alpha = 0.7f),
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = TextMuted.copy(alpha = 0.4f))
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Agentes Disponíveis",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPureWhite
                    )
                    Text(
                        text = "Toque em um agente para conversar com ele",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }

                Row {
                    IconButton(
                        onClick = {
                            onDismiss()
                            onOpenVoiceCreate()
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(NeonCyan.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Novo Agente", tint = NeonCyan)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .background(SurfaceGlass, CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar", tint = TextMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(agents) { agent ->
                    val isSelected = selectedAgent?.id == agent.id
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.selectAgent(agent)
                                onDismiss()
                            },
                        shape = RoundedCornerShape(18.dp),
                        color = if (isSelected) SurfaceCardDark else SurfaceSpace,
                        border = if (isSelected) BorderStroke(1.5.dp, NeonCyan) else BorderStroke(1.dp, BorderSubtle),
                        tonalElevation = if (isSelected) 4.dp else 1.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(SurfaceGlassHighlight, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = agent.emoji, fontSize = 26.sp)
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = agent.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPureWhite
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = NeonCyan.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = agent.category,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = NeonCyan,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = agent.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted,
                                    maxLines = 2
                                )
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selecionado",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                            } else if (agent.isCustom) {
                                IconButton(
                                    onClick = { viewModel.deleteAgent(agent.id) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Excluir",
                                        tint = TextMuted.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
