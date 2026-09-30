package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AgentEntity
import com.example.data.MessageEntity
import com.example.ui.components.VoiceWaveform
import com.example.viewmodel.MainViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceChatScreen(
    viewModel: MainViewModel,
    agents: List<AgentEntity>,
    selectedAgent: AgentEntity?,
    messages: List<MessageEntity>,
    isListening: Boolean,
    isSpeaking: Boolean,
    isLoading: Boolean,
    onOpenAgentSelector: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val audioLevel by viewModel.audioLevel.collectAsState()
    val vadState by viewModel.vadState.collectAsState()
    var showMicrophoneDisclosureDialog by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "voiceChatMicPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = when {
            isLoading -> 1.25f
            isSpeaking -> 1.18f
            isListening -> 1.15f
            else -> 1f
        },
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isLoading) 550 else if (isSpeaking) 750 else 650,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = if (isLoading || isSpeaking || isListening) 0.55f else 0f,
        targetValue = 0.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isLoading) 550 else if (isSpeaking) 750 else 650,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startListeningWithVad()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        // Top Agent Header Card
        if (selectedAgent != null) {
            Surface(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { onOpenAgentSelector() },
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = selectedAgent.emoji, fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = selectedAgent.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = selectedAgent.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = selectedAgent.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Trocar Agente",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .clickable { onOpenAgentSelector() },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Nenhum agente selecionado. Toque para escolher.", color = MaterialTheme.colorScheme.primary)
            }
        }

        // Messages List or Empty State
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (messages.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "🎙️", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Converse com ${selectedAgent?.name ?: "o Agente"} por voz",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Toque no microfone abaixo ou digite sua mensagem",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(messages) { msg ->
                        val isUser = msg.sender == "user"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Surface(
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isUser) 16.dp else 4.dp,
                                    bottomEnd = if (isUser) 4.dp else 16.dp
                                ),
                                color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.widthIn(max = 280.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = msg.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Voice Waveform Activity Indicator
        VoiceWaveform(
            isListening = isListening,
            isSpeaking = isSpeaking,
            audioLevel = audioLevel,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Bottom Controls: Mic Button & Text Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mic Button with Pulse effect for listening, AI processing and speaking
                val micColors = when {
                    isListening -> listOf(Color.Red, MaterialTheme.colorScheme.primary)
                    isLoading -> listOf(Color(0xFFFFEA00), Color(0xFFFF6D00))
                    isSpeaking -> listOf(Color(0xFF00E5FF), MaterialTheme.colorScheme.primary)
                    else -> listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(56.dp)
                ) {
                    if (isLoading || isSpeaking || isListening) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .scale(pulseScale * 1.15f)
                                .clip(CircleShape)
                                .background(
                                    color = if (isLoading) Color(0xFFFFEA00).copy(alpha = pulseAlpha)
                                    else if (isSpeaking) Color(0xFF00E5FF).copy(alpha = pulseAlpha)
                                    else Color.Red.copy(alpha = pulseAlpha)
                                )
                        )
                    }

                    IconButton(
                        onClick = {
                            if (isListening) {
                                viewModel.stopListeningWithVad()
                            } else {
                                val permissionCheck = androidx.core.content.ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                )
                                if (permissionCheck == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                    viewModel.startListeningWithVad()
                                } else {
                                    showMicrophoneDisclosureDialog = true
                                }
                            }
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .scale(pulseScale)
                            .background(
                                brush = Brush.radialGradient(colors = micColors),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = when {
                                isListening -> Icons.Default.MicOff
                                isLoading -> Icons.Default.Mic
                                isSpeaking -> Icons.Default.Mic
                                else -> Icons.Default.Mic
                            },
                            contentDescription = when {
                                isListening -> "Parar de Ouvir"
                                isLoading -> "IA Processando..."
                                isSpeaking -> "IA Falando..."
                                else -> "Falar"
                            },
                            tint = if (isLoading) Color.Black else Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("Digite ou fale com o agente...") },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (textInput.isNotBlank()) {
                                viewModel.sendMessage(textInput)
                                textInput = ""
                            }
                        }
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            viewModel.sendMessage(textInput)
                            textInput = ""
                        }
                    },
                    enabled = !isLoading && textInput.isNotBlank(),
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = if (textInput.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Enviar",
                        tint = if (textInput.isNotBlank()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showMicrophoneDisclosureDialog) {
        AlertDialog(
            onDismissRequest = { showMicrophoneDisclosureDialog = false },
            title = {
                Text(
                    text = "Acesso ao Microfone",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = "Para que você possa conversar com o agente de inteligência artificial por voz, o aplicativo precisa de permissão para utilizar o microfone.\n\n" +
                           "• O áudio é convertido em texto em tempo real (VAD) para envio à IA.\n" +
                           "• Nenhuma gravação de voz é salva permanentemente ou comercializada.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showMicrophoneDisclosureDialog = false
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                ) {
                    Text("CONCORDAR E CONTINUAR", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMicrophoneDisclosureDialog = false }) {
                    Text("AGORA NÃO", fontSize = 12.sp)
                }
            }
        )
    }
}

