package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
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
import androidx.compose.material.icons.automirrored.filled.*
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AgentEntity
import com.example.ui.components.*
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val agents by viewModel.agents.collectAsStateWithLifecycle()
    val selectedAgent by viewModel.selectedAgent.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val isListening by viewModel.isListening.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    var currentTab by remember { mutableIntStateOf(0) } // 0: Home, 1: Interaction, 2: Settings
    var showAgentSelectorSheet by remember { mutableStateOf(false) }
    var showVoiceCreateSheet by remember { mutableStateOf(false) }
    var showMicrophoneDisclosureDialog by remember { mutableStateOf(false) }

    val audioLevel by viewModel.audioLevel.collectAsStateWithLifecycle()
    val vadState by viewModel.vadState.collectAsStateWithLifecycle()
    val vadMode by viewModel.vadMode.collectAsStateWithLifecycle()
    val recognizedText by viewModel.lastRecognizedText.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startListeningWithVad()
        }
    }

    val toggleVoiceListening: () -> Unit = {
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
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF07090E),
        bottomBar = {
            CyberBottomNavigation(
                selectedTab = currentTab,
                onTabSelected = { currentTab = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Cosmic Particle Nebula Canvas
            CyberParticleNebula(
                isListening = isListening,
                isSpeaking = isSpeaking,
                modifier = Modifier.fillMaxSize()
            )

            // Screen Content by Tab
            when (currentTab) {
                0 -> {
                    // HOME SCREEN (EXACT MATCH TO REFERENCE PHOTO)
                    HomeHudScreen(
                        selectedAgent = selectedAgent,
                        isListening = isListening,
                        isSpeaking = isSpeaking,
                        isLoading = isLoading,
                        vadState = vadState,
                        audioLevel = audioLevel,
                        lastMessage = messages.lastOrNull(),
                        recognizedText = recognizedText,
                        onSwitchAgentClick = { showAgentSelectorSheet = true },
                        onMicClick = toggleVoiceListening,
                        onReplayTts = { text -> viewModel.speak(text) }
                    )
                }
                1 -> {
                    // CREATE AGENT TAB (SECOND PAGE)
                    CreateAgentScreen(
                        viewModel = viewModel,
                        isLoading = isLoading
                    )
                }
                2 -> {
                    // SETTINGS / AGENT MANAGER TAB
                    val openAiApiKey by viewModel.openAiApiKey.collectAsState()
                    val speechRate by viewModel.speechRate.collectAsStateWithLifecycle()
                    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
                    SettingsScreen(
                        agents = agents,
                        selectedAgent = selectedAgent,
                        onSelectAgent = { viewModel.selectAgent(it) },
                        onOpenVoiceCreate = { showVoiceCreateSheet = true },
                        onDeleteAgent = { viewModel.deleteAgent(it) },
                        onClearCurrentHistory = { viewModel.clearCurrentAgentMessages() },
                        openAiApiKey = openAiApiKey,
                        onSaveOpenAiApiKey = { viewModel.saveOpenAiApiKey(it) },
                        speechRate = speechRate,
                        onSetSpeechRate = { viewModel.setSpeechRate(it) },
                        currentLanguage = currentLanguage,
                        onSetLanguage = { viewModel.setLanguage(it) },
                        onTestTts = {
                            viewModel.speak("Olá! Esta é a voz sintetizada em português para o seu assistente de inteligência artificial.")
                        },
                        vadMode = vadMode,
                        onSetVadMode = { viewModel.setVadMode(it) },
                        onTestApiKey = { key, callback -> viewModel.testApiKeyConnection(key, callback) }
                    )
                }
            }
        }
    }

    // Modal Sheet: Select Agent
    if (showAgentSelectorSheet) {
        AgentLibraryBottomSheet(
            viewModel = viewModel,
            agents = agents,
            selectedAgent = selectedAgent,
            onDismiss = { showAgentSelectorSheet = false },
            onOpenVoiceCreate = {
                showAgentSelectorSheet = false
                showVoiceCreateSheet = true
            }
        )
    }

    // Modal Sheet: Create Agent by Voice
    if (showVoiceCreateSheet) {
        VoiceAgentCreationSheet(
            viewModel = viewModel,
            isLoading = isLoading,
            onDismiss = { showVoiceCreateSheet = false }
        )
    }

    // Prominent Microphone Disclosure Dialog (MANDATORY GOOGLE PLAY DATA SAFETY REQUIREMENT)
    if (showMicrophoneDisclosureDialog) {
        AlertDialog(
            onDismissRequest = { showMicrophoneDisclosureDialog = false },
            containerColor = Color(0xFF0F172A),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Acesso ao Microfone",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Para que você possa conversar com os agentes de inteligência artificial por voz, o aplicativo precisa de permissão para utilizar o microfone do seu dispositivo.\n\n" +
                               "• O áudio é convertido em texto em tempo real (VAD) para envio à IA.\n" +
                               "• Nenhuma gravação de voz é salva permanentemente ou comercializada com terceiros.",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showMicrophoneDisclosureDialog = false
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                ) {
                    Text("CONCORDAR E CONTINUAR", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMicrophoneDisclosureDialog = false }) {
                    Text("AGORA NÃO", color = Color(0xFF94A3B8), fontSize = 11.5.sp)
                }
            }
        )
    }
}

/**
 * The flagship Home HUD screen recreating the exact layout from the reference photo.
 */
@Composable
private fun HomeHudScreen(
    selectedAgent: AgentEntity?,
    isListening: Boolean,
    isSpeaking: Boolean,
    isLoading: Boolean,
    vadState: com.example.voice.VadState = com.example.voice.VadState.IDLE,
    audioLevel: Float = 0f,
    lastMessage: com.example.data.MessageEntity?,
    recognizedText: String,
    onSwitchAgentClick: () -> Unit,
    onMicClick: () -> Unit,
    onReplayTts: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TOP BADGE: "AI ASSISTANT"
        BuildAiStudioBadge(
            title = "AI ASSISTANT",
            modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
        )

        // HUD CARD: "CONVERSATIONAL AGENT: PROJECT ALPHA"
        ConversationalAgentHudCard(
            currentAgent = selectedAgent,
            onSwitchAgentClick = onSwitchAgentClick,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.weight(0.12f))

        // CENTERPIECE: Concentric Rainbow Ring Mic Button with real-time audioLevel
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            CyberMicButton(
                isListening = isListening,
                isSpeaking = isSpeaking,
                isLoading = isLoading,
                audioLevel = audioLevel,
                onClick = onMicClick
            )
        }

        // Voice Status Text with intelligent VAD awareness
        val statusText = when {
            isListening && vadState == com.example.voice.VadState.USER_SPEAKING -> "FALA DETECTADA • RECONHECENDO EM PORTUGUÊS..."
            isListening && (vadState == com.example.voice.VadState.SILENCE_AFTER_SPEECH || vadState == com.example.voice.VadState.FINALIZING) -> "COMANDO CONCLUÍDO • PROCESSANDO..."
            isListening && vadState == com.example.voice.VadState.CALIBRATING_NOISE_FLOOR -> "CALIBRANDO MICROFONE..."
            isListening -> "OUVINDO... (FALE SEU COMANDO EM PORTUGUÊS)"
            isLoading -> "IA PROCESSANDO COMANDO DE VOZ..."
            isSpeaking -> "FALANDO EM VOZ ALTA (PT-BR)..."
            else -> "TOQUE PARA FALAR EM PORTUGUÊS"
        }

        val statusColor = when {
            isListening && (vadState == com.example.voice.VadState.SILENCE_AFTER_SPEECH || vadState == com.example.voice.VadState.FINALIZING) -> Color(0xFF00E5FF)
            isListening && vadState == com.example.voice.VadState.USER_SPEAKING -> Color(0xFF10B981)
            isListening -> Color(0xFFFF2A85)
            isLoading -> Color(0xFFFFEA00)
            isSpeaking -> Color(0xFF00E5FF)
            else -> Color(0xFF94A3B8)
        }

        Text(
            text = statusText,
            color = statusColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )

        if (isListening && recognizedText.isNotBlank()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1E293B).copy(alpha = 0.85f),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = "\"$recognizedText\"",
                    color = Color(0xFF00E5FF),
                    fontSize = 11.5.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Dynamic Waveform that reacts in real-time to voice input level
        if (isListening || isSpeaking) {
            VoiceWaveform(
                isListening = isListening,
                isSpeaking = isSpeaking,
                audioLevel = audioLevel,
                modifier = Modifier
                    .fillMaxWidth(0.82f)
                    .height(44.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(44.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Floating Response Card / Live Transcription
        if (lastMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0D121E).copy(alpha = 0.85f))
                    .border(
                        1.dp,
                        Brush.horizontalGradient(
                            listOf(Color(0xFF00E5FF).copy(alpha = 0.3f), Color(0xFFFF2A85).copy(alpha = 0.3f))
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                if (lastMessage.sender == "user") Color(0xFF00E5FF).copy(alpha = 0.2f)
                                else Color(0xFFFF2A85).copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (lastMessage.sender == "user") "👤" else (selectedAgent?.emoji ?: "⚡"),
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = lastMessage.text,
                        color = Color(0xFFE2E8F0),
                        fontSize = 12.5.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (lastMessage.sender == "agent") {
                        IconButton(
                            onClick = { onReplayTts(lastMessage.text) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Ouvir",
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        } else {
            // Placeholder hint
            Text(
                text = "Diga \"Olá\" ou pergunte qualquer coisa para iniciar a conversa.",
                color = Color(0xFF64748B),
                fontSize = 11.5.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.weight(0.18f))
    }
}

/**
 * Detailed Interaction Screen for browsing conversations, audio replies, and typing.
 */
@Composable
private fun InteractionScreen(
    messages: List<com.example.data.MessageEntity>,
    selectedAgent: AgentEntity?,
    isLoading: Boolean,
    isListening: Boolean,
    isSpeaking: Boolean = false,
    audioLevel: Float = 0f,
    onSendMessage: (String) -> Unit,
    onReplayTts: (String) -> Unit,
    onMicClick: () -> Unit
) {
    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val infiniteTransition = rememberInfiniteTransition(label = "interactionMicPulse")
    val micPulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = when {
            isLoading -> 1.18f
            isSpeaking -> 1.12f
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
        label = "micScale"
    )

    val micPulseAlpha by infiniteTransition.animateFloat(
        initialValue = if (isLoading || isSpeaking || isListening) 0.5f else 0f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isLoading) 550 else if (isSpeaking) 750 else 650,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "micAlpha"
    )

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "INTERAÇÕES: ${selectedAgent?.name ?: "PROJECT ALPHA"}",
                color = Color(0xFF00E5FF),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        // Messages list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                val isUser = msg.sender == "user"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isUser) 16.dp else 4.dp,
                                    bottomEnd = if (isUser) 4.dp else 16.dp
                                )
                            )
                            .background(if (isUser) Color(0xFF1E293B) else Color(0xFF0F172A))
                            .border(
                                1.dp,
                                if (isUser) Color(0xFF334155) else Color(0xFF00E5FF).copy(alpha = 0.4f),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isUser) "Você" else (selectedAgent?.name ?: "Agente IA"),
                                    color = if (isUser) Color(0xFF94A3B8) else Color(0xFF00E5FF),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )

                                if (!isUser) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Ouvir",
                                        tint = Color(0xFF00E5FF),
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clickable { onReplayTts(msg.text) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = msg.text,
                                color = Color.White,
                                fontSize = 13.5.sp,
                                lineHeight = 19.sp
                            )
                        }
                    }
                }
            }

            if (isLoading) {
                item {
                    Text(
                        text = "Agente de IA está gerando a resposta...",
                        color = Color(0xFF38BDF8),
                        fontSize = 12.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }

        // Real-time Voice Waveform while recording in Interaction tab
        if (isListening) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                VoiceWaveform(
                    isListening = true,
                    isSpeaking = false,
                    audioLevel = audioLevel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                )
            }
        }

        // Input Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text("Digite sua mensagem...", color = Color(0xFF64748B), fontSize = 13.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00E5FF),
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (textInput.isNotBlank()) {
                            onSendMessage(textInput)
                            textInput = ""
                        }
                    }
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (textInput.isNotBlank()) {
                        onSendMessage(textInput)
                        textInput = ""
                    }
                },
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00E5FF))
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar", tint = Color.Black)
            }

            Spacer(modifier = Modifier.width(6.dp))

            val micBgColor = when {
                isListening -> Color(0xFFFF2A85)
                isLoading -> Color(0xFFFFEA00)
                isSpeaking -> Color(0xFF00E5FF)
                else -> Color(0xFF1E293B)
            }
            val micIconColor = when {
                isListening -> Color.White
                isLoading -> Color.Black
                isSpeaking -> Color.Black
                else -> Color(0xFF00E5FF)
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(50.dp)
            ) {
                // Pulsing outer ripple shockwave
                if (isLoading || isSpeaking || isListening) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .scale(micPulseScale * 1.15f)
                            .clip(CircleShape)
                            .background(micBgColor.copy(alpha = micPulseAlpha))
                    )
                }

                IconButton(
                    onClick = onMicClick,
                    modifier = Modifier
                        .size(46.dp)
                        .scale(micPulseScale)
                        .clip(CircleShape)
                        .background(micBgColor)
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = when {
                            isListening -> "Parar de Ouvir"
                            isLoading -> "IA Processando..."
                            isSpeaking -> "IA Falando..."
                            else -> "Falar"
                        },
                        tint = micIconColor
                    )
                }
            }
        }
    }
}

/**
 * Settings & Agent Customization Screen with Google Play Console Compliance & Privacy Policy
 */
@Composable
private fun SettingsScreen(
    agents: List<AgentEntity>,
    selectedAgent: AgentEntity?,
    onSelectAgent: (AgentEntity) -> Unit,
    onOpenVoiceCreate: () -> Unit,
    onDeleteAgent: (Long) -> Unit,
    onClearCurrentHistory: () -> Unit,
    openAiApiKey: String,
    onSaveOpenAiApiKey: (String) -> Unit,
    speechRate: Float = 1.0f,
    onSetSpeechRate: (Float) -> Unit = {},
    currentLanguage: java.util.Locale = java.util.Locale.forLanguageTag("pt-BR"),
    onSetLanguage: (java.util.Locale) -> Unit = {},
    onTestTts: () -> Unit = {},
    vadMode: com.example.voice.VadMode = com.example.voice.VadMode.BALANCED,
    onSetVadMode: (com.example.voice.VadMode) -> Unit = {},
    onTestApiKey: (String, (Boolean, String) -> Unit) -> Unit = { _, _ -> }
) {
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var tempOpenAiKey by remember(openAiApiKey) { mutableStateOf(openAiApiKey) }
    var testStatusMessage by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var isTestingKey by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "CONFIGURAÇÕES E AGENTES",
            color = Color(0xFF00E5FF),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Create Agent Button
        Button(
            onClick = onOpenVoiceCreate,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00E5FF)
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text("CRIAR NOVO AGENTE POR VOZ", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "AGENTES DISPONÍVEIS:",
            color = Color(0xFF94A3B8),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(agents) { agent ->
                val isSelected = agent.id == selectedAgent?.id
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) Color(0xFF141F32) else Color(0xFF0C101A))
                        .border(
                            1.dp,
                            if (isSelected) Color(0xFF00E5FF) else Color(0xFF1E293B),
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { onSelectAgent(agent) }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = agent.emoji, fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = agent.name,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = agent.description,
                                color = Color(0xFF94A3B8),
                                fontSize = 11.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (isSelected) {
                            Text(
                                text = "ATIVO",
                                color = Color(0xFF00E5FF),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        } else if (agent.isCustom) {
                            IconButton(onClick = { onDeleteAgent(agent.id) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Excluir",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Text-to-Speech Settings Section (Natural Speech & Speed)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0C101A))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = null,
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SÍNTESE DE VOZ (TEXT-TO-SPEECH)",
                    color = Color(0xFF00E5FF),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "VELOCIDADE DE FALA: ${String.format(java.util.Locale.US, "%.2fx", speechRate)}",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(0.75f to "0.75x", 1.0f to "1.0x", 1.25f to "1.25x", 1.5f to "1.5x").forEach { (rate, label) ->
                    val isCurrent = kotlin.math.abs(speechRate - rate) < 0.05f
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isCurrent) Color(0xFF00E5FF) else Color(0xFF141F32),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSetSpeechRate(rate) }
                    ) {
                        Text(
                            text = label,
                            color = if (isCurrent) Color.Black else Color.White,
                            fontSize = 11.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "IDIOMA DA VOZ:",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    java.util.Locale.forLanguageTag("pt-BR") to "🇧🇷 PT",
                    java.util.Locale.US to "🇺🇸 EN",
                    java.util.Locale.forLanguageTag("es-ES") to "🇪🇸 ES",
                    java.util.Locale.FRENCH to "🇫🇷 FR"
                ).forEach { (loc, label) ->
                    val isCurrent = (currentLanguage.language == loc.language)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isCurrent) Color(0xFF00E5FF) else Color(0xFF141F32),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSetLanguage(loc) }
                    ) {
                        Text(
                            text = label,
                            color = if (isCurrent) Color.Black else Color.White,
                            fontSize = 11.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onTestTts,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF141F32)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = null,
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "TESTAR ÁUDIO NATURAL",
                    color = Color(0xFF00E5FF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // VAD & Noise Rejection Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0C101A))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.GraphicEq,
                    contentDescription = null,
                    tint = Color(0xFFFF2A85),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "DETECÇÃO DE VOZ (VAD) & FILTRO DE RUÍDO",
                    color = Color(0xFFFF2A85),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Para de gravar automaticamente ao parar de falar. Ajuste a imunidade para som de computador/caixas de som ligadas:",
                color = Color(0xFF64748B),
                fontSize = 10.5.sp,
                lineHeight = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            com.example.voice.VadMode.values().forEach { mode ->
                val isSelected = (vadMode == mode)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) Color(0xFFFF2A85).copy(alpha = 0.15f) else Color(0xFF141F32),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF2A85)) else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clickable { onSetVadMode(mode) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onSetVadMode(mode) },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFF2A85))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = mode.title,
                                color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                            Text(
                                text = mode.description,
                                color = Color(0xFF94A3B8),
                                fontSize = 9.5.sp,
                                lineHeight = 12.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // AI API Key & Neural Engine Configuration Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0C101A))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ROTEAMENTO IA & CHAVE DE API",
                    color = Color(0xFF00E5FF),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                val modeBadge = when {
                    tempOpenAiKey.trim().startsWith("oc_sk_") -> "OmniRoute Ativo ⚡"
                    tempOpenAiKey.trim().startsWith("sk-") -> "OpenAI 🟢"
                    tempOpenAiKey.trim().startsWith("AIza") -> "Gemini 🟢"
                    tempOpenAiKey.trim().isBlank() -> "OmniRoute Padrão ⚡"
                    else -> "Personalizada 🟢"
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF141F32),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f))
                ) {
                    Text(
                        text = modeBadge,
                        color = Color(0xFF00E5FF),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "O aplicativo já vem pré-configurado via OmniRoute com chave universal integrada. Você não precisa digitar nada para conversar com os agentes inteligentes!",
                color = Color(0xFF94A3B8),
                fontSize = 10.5.sp,
                lineHeight = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = tempOpenAiKey,
                onValueChange = {
                    tempOpenAiKey = it
                    testStatusMessage = null
                },
                placeholder = { Text("oc_sk_... (OmniRoute integrado) ou customizada", color = Color(0xFF64748B), fontSize = 11.5.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF00E5FF),
                    unfocusedBorderColor = Color(0xFF1E293B),
                    focusedContainerColor = Color(0xFF141F32),
                    unfocusedContainerColor = Color(0xFF141F32)
                ),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        onSaveOpenAiApiKey(tempOpenAiKey)
                        testStatusMessage = Pair(true, "Chave salva com sucesso!")
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("SALVAR CHAVE", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                }

                Button(
                    onClick = {
                        isTestingKey = true
                        testStatusMessage = null
                        onTestApiKey(tempOpenAiKey) { success, msg ->
                            isTestingKey = false
                            testStatusMessage = Pair(success, msg)
                        }
                    },
                    enabled = !isTestingKey,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF141F32)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        if (isTestingKey) "TESTANDO..." else "TESTAR CONEXÃO",
                        color = Color(0xFF00E5FF),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            testStatusMessage?.let { (success, message) ->
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (success) Color(0xFF064E3B).copy(alpha = 0.5f) else Color(0xFF7F1D1D).copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (success) Color(0xFF10B981) else Color(0xFFEF4444)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = message,
                        color = if (success) Color(0xFF6EE7B7) else Color(0xFFFCA5A5),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Privacy Policy & Google Play Data Safety Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0C101A))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            Text(
                text = "SEGURANÇA E CONFORMIDADE (GOOGLE PLAY)",
                color = Color(0xFF00E5FF),
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showPrivacyDialog = true }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Política de Privacidade e Dados",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(18.dp)
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 6.dp),
                color = Color(0xFF1E293B)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showClearConfirmDialog = true }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Limpar Histórico de Mensagens",
                        color = Color(0xFFFCA5A5),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "AI Assistant v1.0.0 (Build 1) • Target SDK 36 (Android 15+) • Google Play Ready",
                color = Color(0xFF64748B),
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // Privacy Policy Dialog (MANDATORY FOR GOOGLE PLAY STORE AUDIO PERMISSIONS)
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            containerColor = Color(0xFF0F172A),
            title = {
                Text(
                    text = "Política de Privacidade",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "1. Uso do Microfone (RECORD_AUDIO):\n" +
                                "O aplicativo solicita acesso ao microfone exclusivamente para converter sua fala em texto em tempo real.\n\n" +
                                "2. Tratamento de Dados de Voz:\n" +
                                "O áudio é processado de maneira efêmera e não é armazenado em servidores externos nem comercializado com terceiros.\n\n" +
                                "3. Armazenamento Local:\n" +
                                "As conversas e os agentes criados ficam salvos exclusivamente no banco de dados local do seu dispositivo (Room Database) e podem ser apagados por você a qualquer momento.\n\n" +
                                "4. Inteligência Artificial:\n" +
                                "As respostas são geradas via API do Google Gemini através de conexões HTTPS criptografadas.",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                ) {
                    Text("ENTENDI", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Clear History Confirmation Dialog
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            containerColor = Color(0xFF0F172A),
            title = {
                Text("Limpar Mensagens", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Deseja realmente apagar o histórico de mensagens do agente atual? Essa ação não pode ser desfeita.",
                    color = Color(0xFFCBD5E1),
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearCurrentHistory()
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("LIMPAR", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("CANCELAR", color = Color(0xFF94A3B8))
                }
            }
        )
    }
}
