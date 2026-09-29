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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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

    // Speech Recognizer setup
    var speechRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }
    var recognizedText by remember { mutableStateOf("") }
    var audioLevel by remember { mutableFloatStateOf(0f) }

    val startListeningAction: () -> Unit = {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            val recognizer = speechRecognizer ?: SpeechRecognizer.createSpeechRecognizer(context).also {
                speechRecognizer = it
            }

            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    viewModel.setListening(true)
                    audioLevel = 0.15f
                }
                override fun onBeginningOfSpeech() {
                    audioLevel = 0.3f
                }
                override fun onRmsChanged(rmsdB: Float) {
                    // Normalize SpeechRecognizer rmsdB (-2dB to 10dB+) to 0.05..1.0
                    val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.08f, 1f)
                    audioLevel = normalized
                }
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    viewModel.setListening(false)
                    audioLevel = 0f
                }
                override fun onError(error: Int) {
                    viewModel.setListening(false)
                    audioLevel = 0f
                }
                override fun onResults(results: Bundle?) {
                    viewModel.setListening(false)
                    audioLevel = 0f
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull() ?: ""
                    if (text.isNotBlank()) {
                        recognizedText = text
                        viewModel.sendMessage(text)
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    matches?.firstOrNull()?.let { recognizedText = it }
                }
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            recognizer.startListening(intent)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startListeningAction()
        }
    }

    val toggleVoiceListening: () -> Unit = {
        if (isListening) {
            speechRecognizer?.stopListening()
            viewModel.setListening(false)
            audioLevel = 0f
        } else {
            val permissionCheck = androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            )
            if (permissionCheck == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                startListeningAction()
            } else {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer?.destroy()
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
                        audioLevel = audioLevel,
                        lastMessage = messages.lastOrNull(),
                        recognizedText = recognizedText,
                        onSwitchAgentClick = { showAgentSelectorSheet = true },
                        onMicClick = toggleVoiceListening,
                        onReplayTts = { text -> viewModel.speak(text) }
                    )
                }
                1 -> {
                    // INTERACTION TAB (FULL CHAT & VOICE HISTORY)
                    InteractionScreen(
                        messages = messages,
                        selectedAgent = selectedAgent,
                        isLoading = isLoading,
                        isListening = isListening,
                        audioLevel = audioLevel,
                        onSendMessage = { viewModel.sendMessage(it) },
                        onReplayTts = { viewModel.speak(it) },
                        onMicClick = toggleVoiceListening
                    )
                }
                2 -> {
                    // SETTINGS / AGENT MANAGER TAB
                    val openAiApiKey by viewModel.openAiApiKey.collectAsState()
                    SettingsScreen(
                        agents = agents,
                        selectedAgent = selectedAgent,
                        onSelectAgent = { viewModel.selectAgent(it) },
                        onOpenVoiceCreate = { showVoiceCreateSheet = true },
                        onDeleteAgent = { viewModel.deleteAgent(it) },
                        onClearCurrentHistory = { viewModel.clearCurrentAgentMessages() },
                        openAiApiKey = openAiApiKey,
                        onSaveOpenAiApiKey = { viewModel.saveOpenAiApiKey(it) }
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

        // Voice Status Text
        val statusText = when {
            isListening -> "OUVINDO SUA VOZ..."
            isLoading -> "PROCESSANDO RESPOSTA NO GEMINI..."
            isSpeaking -> "FALANDO EM VOZ ALTA..."
            else -> "TOQUE PARA CONVERSAR"
        }

        val statusColor = when {
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
                                imageVector = Icons.Default.VolumeUp,
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
    audioLevel: Float = 0f,
    onSendMessage: (String) -> Unit,
    onReplayTts: (String) -> Unit,
    onMicClick: () -> Unit
) {
    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

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
                                        imageVector = Icons.Default.VolumeUp,
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
                        text = "Gemini está gerando a resposta...",
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
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
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

            IconButton(
                onClick = onMicClick,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (isListening) Color(0xFFFF2A85) else Color(0xFF1E293B))
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Falar",
                    tint = if (isListening) Color.White else Color(0xFF00E5FF)
                )
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
    onSaveOpenAiApiKey: (String) -> Unit
) {
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var tempOpenAiKey by remember(openAiApiKey) { mutableStateOf(openAiApiKey) }

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

        // OpenAI API Key Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0C101A))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            Text(
                text = "CONFIGURAR OPENAI API KEY (GPT-4o-mini)",
                color = Color(0xFF00E5FF),
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = tempOpenAiKey,
                onValueChange = { tempOpenAiKey = it },
                placeholder = { Text("sk-...", color = Color(0xFF64748B), fontSize = 12.sp) },
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

            Button(
                onClick = { onSaveOpenAiApiKey(tempOpenAiKey) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("SALVAR OPENAI API KEY", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
