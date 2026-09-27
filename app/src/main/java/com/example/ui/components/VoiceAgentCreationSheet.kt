package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceAgentCreationSheet(
    viewModel: MainViewModel,
    isLoading: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isListening by remember { mutableStateOf(false) }
    var spokenVoicePrompt by remember { mutableStateOf("") }
    var showManualForm by remember { mutableStateOf(false) }

    // Manual form state
    var manualName by remember { mutableStateOf("") }
    var manualDesc by remember { mutableStateOf("") }
    var manualPrompt by remember { mutableStateOf("") }
    var manualEmoji by remember { mutableStateOf("⚡") }

    val speechRecognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else {
            null
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted && speechRecognizer != null) {
            isListening = true
            startVoiceCreationSpeech(context, speechRecognizer) { result ->
                isListening = false
                spokenVoicePrompt = result
                if (result.isNotBlank()) {
                    viewModel.createAgentViaVoiceDescription(result) {
                        onDismiss()
                    }
                }
            }
        }
    }

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
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            SheetHeader(onDismiss = onDismiss)

            Spacer(modifier = Modifier.height(28.dp))

            // Main Voice Button
            VoiceRecordingButton(
                isListening = isListening,
                onToggleListening = {
                    if (isListening) {
                        speechRecognizer?.stopListening()
                        isListening = false
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        if (speechRecognizer != null) {
                            isListening = true
                            startVoiceCreationSpeech(context, speechRecognizer) { result ->
                                isListening = false
                                spokenVoicePrompt = result
                                if (result.isNotBlank()) {
                                    viewModel.createAgentViaVoiceDescription(result) {
                                        onDismiss()
                                    }
                                }
                            }
                        } else {
                            // Fallback simulation
                            viewModel.createAgentViaVoiceDescription("Assistente para dicas de investimentos e finanças") {
                                onDismiss()
                            }
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isListening) "🎙️ Ouvindo sua ideia em português..."
                else if (isLoading) "✨ Gemini gerando persona e instruções..."
                else "Toque para falar o agente desejado",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isListening) VividMagenta else NeonCyan
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Examples pill
            ExamplesPill()

            if (spokenVoicePrompt.isNotBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                SpokenPromptDisplay(spokenVoicePrompt = spokenVoicePrompt)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Toggle Manual Mode
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showManualForm = !showManualForm }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = NeonBlue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (showManualForm) "Ocultar formulário manual" else "Ou configurar manualmente por texto",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeonBlue
                )
            }

            AnimatedVisibility(visible = showManualForm) {
                ManualAgentForm(
                    manualName = manualName,
                    onManualNameChange = { manualName = it },
                    manualDesc = manualDesc,
                    onManualDescChange = { manualDesc = it },
                    manualPrompt = manualPrompt,
                    onManualPromptChange = { manualPrompt = it },
                    manualEmoji = manualEmoji,
                    onManualEmojiChange = { manualEmoji = it },
                    isLoading = isLoading,
                    onSubmit = {
                        if (manualName.isNotBlank() && manualPrompt.isNotBlank()) {
                            viewModel.createNewAgent(
                                manualName,
                                manualDesc.ifBlank { "Assistente IA" },
                                manualPrompt,
                                manualEmoji.ifBlank { "🤖" },
                                "Manual"
                            )
                            onDismiss()
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

private fun startVoiceCreationSpeech(context: Context, speechRecognizer: SpeechRecognizer, onResult: (String) -> Unit) {
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
    }

    speechRecognizer.setRecognitionListener(object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onError(error: Int) { onResult("") }
        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                onResult(matches[0])
            } else {
                onResult("")
            }
        }
        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    })

    try {
        speechRecognizer.startListening(intent)
    } catch (e: Exception) {
        onResult("")
    }
}

@Composable
fun SheetHeader(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Criar Agente por Voz",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPureWhite
            )
            Text(
                text = "Diga como quer que seu novo assistente seja",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
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

@Composable
fun VoiceRecordingButton(
    isListening: Boolean,
    onToggleListening: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(110.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = if (isListening) listOf(VividMagenta, ElectricViolet)
                    else listOf(NeonCyan, NeonBlue)
                ),
                shape = CircleShape
            )
            .clickable {
                onToggleListening()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
            contentDescription = "Falar ideia do agente",
            tint = Color.White,
            modifier = Modifier.size(52.dp)
        )
    }
}

@Composable
fun ExamplesPill() {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SurfaceCardDark,
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Exemplos do que você pode falar:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "• \"Crie um chef especializado em receitas saudáveis com poucos ingredientes\"",
                style = MaterialTheme.typography.bodySmall,
                color = TextPureWhite
            )
            Text(
                text = "• \"Crie um mentor de negócios focado em startups\"",
                style = MaterialTheme.typography.bodySmall,
                color = TextPureWhite
            )
        }
    }
}

@Composable
fun SpokenPromptDisplay(spokenVoicePrompt: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SurfaceGlassHighlight,
        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Você disse: \"$spokenVoicePrompt\"",
            style = MaterialTheme.typography.bodySmall,
            color = TextPureWhite,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
fun ManualAgentForm(
    manualName: String,
    onManualNameChange: (String) -> Unit,
    manualDesc: String,
    onManualDescChange: (String) -> Unit,
    manualPrompt: String,
    onManualPromptChange: (String) -> Unit,
    manualEmoji: String,
    onManualEmojiChange: (String) -> Unit,
    isLoading: Boolean,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
    ) {
        OutlinedTextField(
            value = manualName,
            onValueChange = onManualNameChange,
            label = { Text("Nome do Agente (ex: Leonardo)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = BorderSubtle,
                focusedTextColor = TextPureWhite,
                unfocusedTextColor = TextPureWhite
            )
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = manualDesc,
            onValueChange = onManualDescChange,
            label = { Text("Breve Descrição") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = BorderSubtle,
                focusedTextColor = TextPureWhite,
                unfocusedTextColor = TextPureWhite
            )
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = manualPrompt,
            onValueChange = onManualPromptChange,
            label = { Text("Instrução do Sistema (Personalidade)") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = BorderSubtle,
                focusedTextColor = TextPureWhite,
                unfocusedTextColor = TextPureWhite
            )
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = manualEmoji,
            onValueChange = onManualEmojiChange,
            label = { Text("Emoji Avatar (ex: 🚀)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = BorderSubtle,
                focusedTextColor = TextPureWhite,
                unfocusedTextColor = TextPureWhite
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
            enabled = manualName.isNotBlank() && manualPrompt.isNotBlank() && !isLoading
        ) {
            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Criar e Iniciar Conversa",
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}
