package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Modos de sensibilidade da Detecção de Atividade de Voz (VAD).
 * Permite calibrar a imunidade contra sons externos (como caixas de som de computador ligadas).
 */
enum class VadMode(
    val title: String,
    val description: String,
    val silenceThresholdMs: Long,
    val minRmsDelta: Float
) {
    NOISY_PC(
        title = "Perto de PC / Som Ligado",
        description = "Alta imunidade a áudio de computadores, caixas de som e ventiladores. Para de gravar rápido (~1.1s de silêncio).",
        silenceThresholdMs = 1100L,
        minRmsDelta = 3.2f
    ),
    BALANCED(
        title = "Equilibrado (Recomendado)",
        description = "Ideal para o dia a dia. Detecta o fim da fala após ~1.4s de silêncio natural.",
        silenceThresholdMs = 1400L,
        minRmsDelta = 2.4f
    ),
    QUIET_ROOM(
        title = "Ambiente Silencioso",
        description = "Permite pausas mais longas (~1.8s) para pensar antes de concluir a gravação.",
        silenceThresholdMs = 1800L,
        minRmsDelta = 1.6f
    )
}

/**
 * Estado atual da detecção de atividade de voz.
 */
enum class VadState {
    IDLE,
    CALIBRATING_NOISE_FLOOR,
    LISTENING,
    USER_SPEAKING,
    SILENCE_AFTER_SPEECH,
    FINALIZING
}

/**
 * Gerenciador profissional de reconhecimento de fala com Detecção de Atividade de Voz (VAD).
 *
 * Resolve problemas comuns em ambientes com som ambiente (ex: usuário em frente ao PC com som ligado):
 * 1. Calibra o piso de ruído ambiental (noise floor).
 * 2. Diferencia a fala direta no celular do som contínuo de computadores/alto-falantes.
 * 3. Utiliza monitoramento de tempo de silêncio pós-fala e observador de resultados parciais para
 *    parar automaticamente a gravação assim que o usuário termina de falar.
 * 4. Aplica trava de segurança contra eco (evita que a IA escute a si mesma).
 */
class VoiceActivityManager(
    private val context: Context,
    private val onStopSpeakingRequest: () -> Unit = {}
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null
    private var supervisorJob = SupervisorJob()
    private var coroutineScope = CoroutineScope(Dispatchers.Main + supervisorJob)

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _audioLevel = MutableStateFlow(0f)
    val audioLevel: StateFlow<Float> = _audioLevel.asStateFlow()

    private val _vadState = MutableStateFlow(VadState.IDLE)
    val vadState: StateFlow<VadState> = _vadState.asStateFlow()

    private val _currentMode = MutableStateFlow(VadMode.BALANCED)
    val currentMode: StateFlow<VadMode> = _currentMode.asStateFlow()

    private val _lastRecognizedText = MutableStateFlow("")
    val lastRecognizedText: StateFlow<String> = _lastRecognizedText.asStateFlow()

    // Variáveis de controle de VAD
    private var hasUserSpoken = false
    private var lastVoiceActivityTimestamp = 0L
    private var listeningStartTime = 0L
    private var baselineNoiseFloor = -2.0f
    private var noiseFloorSamples = 0
    private var watchdogJob: Job? = null
    private var pendingOnResultCallback: ((String) -> Unit)? = null
    private var currentPartialText = ""

    companion object {
        private const val INITIAL_NOISE_CALIBRATION_MS = 350L
        private const val INITIAL_NO_SPEECH_TIMEOUT_MS = 6500L
        private const val MAX_TOTAL_RECORDING_MS = 25000L
    }

    fun setMode(mode: VadMode) {
        _currentMode.value = mode
    }

    /**
     * Inicia a escuta com VAD inteligente.
     * @param onResult Callback invocado com a fala final reconhecida quando o usuário terminar de falar.
     */
    fun startListening(onResult: (String) -> Unit) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onResult("")
            return
        }

        // Corta qualquer reprodução de TTS ativa para não captar o próprio áudio
        onStopSpeakingRequest()

        stopListeningInternal()

        pendingOnResultCallback = onResult
        hasUserSpoken = false
        currentPartialText = ""
        _lastRecognizedText.value = ""
        baselineNoiseFloor = -2.0f
        noiseFloorSamples = 0
        listeningStartTime = System.currentTimeMillis()
        lastVoiceActivityTimestamp = listeningStartTime

        _vadState.value = VadState.CALIBRATING_NOISE_FLOOR
        _isListening.value = true
        _audioLevel.value = 0.12f

        mainHandler.post {
            try {
                if (speechRecognizer == null) {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
                }

                speechRecognizer?.setRecognitionListener(createRecognitionListener())

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "pt-BR")
                    putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, "pt-BR")
                    putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("pt-BR", "pt-PT"))
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Fale seu comando em português...")
                    // Configuração de silêncio nativo como camada de suporte
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, _currentMode.value.silenceThresholdMs)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, _currentMode.value.silenceThresholdMs)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 500L)
                }

                speechRecognizer?.startListening(intent)
                startVadWatchdog()
            } catch (e: Exception) {
                cleanupAndNotify("")
            }
        }
    }

    /**
     * Para a escuta manualmente se o usuário tocar no botão.
     */
    fun stopListening() {
        mainHandler.post {
            try {
                _vadState.value = VadState.FINALIZING
                speechRecognizer?.stopListening()
            } catch (e: Exception) {
                cleanupAndNotify(currentPartialText)
            }
        }
    }

    /**
     * Cancela imediatamente sem emitir resultados pendentes.
     */
    fun cancel() {
        mainHandler.post {
            watchdogJob?.cancel()
            watchdogJob = null
            try {
                speechRecognizer?.cancel()
            } catch (ignored: Exception) {}
            _isListening.value = false
            _audioLevel.value = 0f
            _vadState.value = VadState.IDLE
            pendingOnResultCallback = null
        }
    }

    private fun startVadWatchdog() {
        watchdogJob?.cancel()
        watchdogJob = coroutineScope.launch {
            while (_isListening.value) {
                delay(80L)
                val now = System.currentTimeMillis()
                val mode = _currentMode.value

                // 1. Timeout máximo absoluto para não ficar gravando indefinidamente se houver barulho contínuo no ambiente
                if (now - listeningStartTime > MAX_TOTAL_RECORDING_MS) {
                    finalizeSpeech("Tempo máximo de fala atingido.")
                    break
                }

                if (hasUserSpoken) {
                    // O usuário já começou a falar. Monitora se ele parou (silêncio).
                    val elapsedSinceLastVoice = now - lastVoiceActivityTimestamp
                    if (elapsedSinceLastVoice >= mode.silenceThresholdMs) {
                        _vadState.value = VadState.SILENCE_AFTER_SPEECH
                        finalizeSpeech("Fim de fala detectado por VAD.")
                        break
                    }
                } else {
                    // O usuário ainda não falou nada. Verifica timeout inicial (evita travar por som de PC de fundo)
                    val elapsedSinceStart = now - listeningStartTime
                    if (elapsedSinceStart > INITIAL_NO_SPEECH_TIMEOUT_MS) {
                        cleanupAndNotify("")
                        break
                    }
                }
            }
        }
    }

    private fun finalizeSpeech(reason: String) {
        mainHandler.post {
            if (_isListening.value) {
                _vadState.value = VadState.FINALIZING
                try {
                    // Solicita que o reconhecedor finalize e processe os buffers capturados
                    speechRecognizer?.stopListening()
                } catch (e: Exception) {
                    cleanupAndNotify(currentPartialText)
                }
            }
        }
    }

    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _vadState.value = VadState.LISTENING
                listeningStartTime = System.currentTimeMillis()
                lastVoiceActivityTimestamp = listeningStartTime
            }

            override fun onBeginningOfSpeech() {
                hasUserSpoken = true
                lastVoiceActivityTimestamp = System.currentTimeMillis()
                _vadState.value = VadState.USER_SPEAKING
            }

            override fun onRmsChanged(rmsdB: Float) {
                val now = System.currentTimeMillis()
                val mode = _currentMode.value

                // Fase 1: Calibração adaptativa do piso de ruído do ambiente (primeiros 350ms)
                if (now - listeningStartTime < INITIAL_NOISE_CALIBRATION_MS) {
                    noiseFloorSamples++
                    baselineNoiseFloor = if (noiseFloorSamples == 1) rmsdB else (baselineNoiseFloor * 0.7f + rmsdB * 0.3f)
                    return
                }

                // Normaliza o nível de áudio para a UI (0.05 .. 1.0)
                val normalizedLevel = ((rmsdB - baselineNoiseFloor + 2f) / 10f).coerceIn(0.08f, 1.0f)
                _audioLevel.value = normalizedLevel

                // Detecção de voz ativa baseada em energia vs ruído de fundo
                val threshold = baselineNoiseFloor + mode.minRmsDelta
                if (rmsdB > threshold) {
                    hasUserSpoken = true
                    lastVoiceActivityTimestamp = now
                    _vadState.value = VadState.USER_SPEAKING
                } else {
                    // Atualiza suavemente o piso de ruído caso o som de fundo do computador mude gradualmente
                    if (!hasUserSpoken && rmsdB < baselineNoiseFloor + 1.0f) {
                        baselineNoiseFloor = baselineNoiseFloor * 0.95f + rmsdB * 0.05f
                    }
                }
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                _vadState.value = VadState.FINALIZING
                _audioLevel.value = 0.05f
            }

            override fun onError(error: Int) {
                // Em caso de erro, se já tivermos texto parcial capturado, aproveitamos o texto do usuário
                if (currentPartialText.isNotBlank()) {
                    cleanupAndNotify(currentPartialText)
                } else {
                    cleanupAndNotify("")
                }
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val recognized = matches?.firstOrNull() ?: currentPartialText
                cleanupAndNotify(recognized)
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val partial = matches?.firstOrNull() ?: return
                if (partial.isNotBlank()) {
                    currentPartialText = partial
                    _lastRecognizedText.value = partial
                    hasUserSpoken = true
                    lastVoiceActivityTimestamp = System.currentTimeMillis()
                    _vadState.value = VadState.USER_SPEAKING
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    private fun cleanupAndNotify(text: String) {
        watchdogJob?.cancel()
        watchdogJob = null
        _isListening.value = false
        _audioLevel.value = 0f
        _vadState.value = VadState.IDLE

        val callback = pendingOnResultCallback
        pendingOnResultCallback = null

        val finalText = text.trim()
        if (finalText.isNotBlank()) {
            _lastRecognizedText.value = finalText
            callback?.invoke(finalText)
        } else {
            callback?.invoke("")
        }
    }

    private fun stopListeningInternal() {
        watchdogJob?.cancel()
        watchdogJob = null
        try {
            speechRecognizer?.cancel()
        } catch (ignored: Exception) {}
        _isListening.value = false
        _audioLevel.value = 0f
    }

    fun destroy() {
        coroutineScope.cancel()
        mainHandler.post {
            try {
                speechRecognizer?.destroy()
            } catch (ignored: Exception) {}
            speechRecognizer = null
        }
    }
}
