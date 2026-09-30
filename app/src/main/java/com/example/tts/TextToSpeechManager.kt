package com.example.tts

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * TextToSpeechManager provides a unified, production-grade wrapper around Android's native
 * TextToSpeech API. It features:
 * - Natural Brazilian Portuguese pronunciation by default (pt-BR)
 * - Multi-language support (Portuguese, English, Spanish, French, German, Italian, etc.)
 * - Adjustable speech playback rate (0.5x to 2.5x) and pitch
 * - Text normalization to strip markdown syntax and formatting for natural-sounding AI speech
 * - Reactive StateFlow tracking speaking state, initialization status, current speed, and language
 * - Lifecycle-safe stopping and shutdown routines
 */
class TextToSpeechManager(
    context: Context,
    private val onInitComplete: ((Boolean) -> Unit)? = null
) : TextToSpeech.OnInitListener {

    companion object {
        private const val TAG = "TextToSpeechManager"

        val LOCALE_PORTUGUESE_BR: Locale = Locale.forLanguageTag("pt-BR")
        val LOCALE_PORTUGUESE_PT: Locale = Locale.forLanguageTag("pt-PT")
        val LOCALE_ENGLISH_US: Locale = Locale.US
        val LOCALE_ENGLISH_UK: Locale = Locale.UK
        val LOCALE_SPANISH: Locale = Locale.forLanguageTag("es-ES")
        val LOCALE_FRENCH: Locale = Locale.FRENCH
        val LOCALE_GERMAN: Locale = Locale.GERMAN
        val LOCALE_ITALIAN: Locale = Locale.ITALIAN

        val DEFAULT_SUPPORTED_LOCALES: List<Locale> = listOf(
            LOCALE_PORTUGUESE_BR,
            LOCALE_ENGLISH_US,
            LOCALE_SPANISH,
            LOCALE_FRENCH,
            LOCALE_GERMAN,
            LOCALE_ITALIAN
        )
    }

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _speechRate = MutableStateFlow(1.0f)
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    private val _pitch = MutableStateFlow(1.0f)
    val pitch: StateFlow<Float> = _pitch.asStateFlow()

    private val _currentLocale = MutableStateFlow(LOCALE_PORTUGUESE_BR)
    val currentLocale: StateFlow<Locale> = _currentLocale.asStateFlow()

    private var currentOnStart: (() -> Unit)? = null
    private var currentOnDone: (() -> Unit)? = null
    private var currentOnError: ((String?) -> Unit)? = null

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            setupUtteranceListener()
            val success = setLanguage(LOCALE_PORTUGUESE_BR)
            if (!success) {
                // Fallback to generic Portuguese
                setLanguage(Locale.forLanguageTag("pt"))
            }
            applySpeechRate(_speechRate.value)
            applyPitch(_pitch.value)
            _isInitialized.value = true
            Log.d(TAG, "TextToSpeech initialized successfully with language: ${_currentLocale.value}")
            onInitComplete?.invoke(true)
        } else {
            Log.e(TAG, "TextToSpeech initialization failed with status: $status")
            _isInitialized.value = false
            onInitComplete?.invoke(false)
        }
    }

    private fun setupUtteranceListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
                currentOnStart?.invoke()
            }

            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
                currentOnDone?.invoke()
                clearCallbacks()
            }

            @Deprecated("Deprecated in Java", ReplaceWith("onError(utteranceId, -1)"))
            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
                currentOnError?.invoke(utteranceId)
                clearCallbacks()
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                _isSpeaking.value = false
                Log.w(TAG, "TTS Utterance error: $errorCode for id: $utteranceId")
                currentOnError?.invoke("TTS Error Code: $errorCode")
                clearCallbacks()
            }
        })
    }

    private fun clearCallbacks() {
        currentOnStart = null
        currentOnDone = null
        currentOnError = null
    }

    /**
     * Set language for speech synthesis.
     * @return true if the language is available and supported, false otherwise.
     */
    fun setLanguage(locale: Locale): Boolean {
        val engine = tts ?: return false
        val availability = engine.isLanguageAvailable(locale)
        return if (availability >= TextToSpeech.LANG_AVAILABLE) {
            val result = engine.setLanguage(locale)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                _currentLocale.value = locale
                true
            } else {
                Log.w(TAG, "Language $locale missing data or not supported")
                false
            }
        } else {
            Log.w(TAG, "Language $locale not available in engine")
            false
        }
    }

    /**
     * Adjust speech rate (speed).
     * @param rate playback speed multiplier, typically between 0.5f (slow) and 2.5f (fast).
     */
    fun setSpeechRate(rate: Float) {
        val clamped = rate.coerceIn(0.5f, 2.5f)
        _speechRate.value = clamped
        applySpeechRate(clamped)
    }

    private fun applySpeechRate(rate: Float) {
        tts?.setSpeechRate(rate)
    }

    /**
     * Adjust voice pitch.
     * @param pitch tone frequency multiplier, typically between 0.5f (deep) and 2.0f (high).
     */
    fun setPitch(pitch: Float) {
        val clamped = pitch.coerceIn(0.5f, 2.0f)
        _pitch.value = clamped
        applyPitch(clamped)
    }

    private fun applyPitch(pitch: Float) {
        tts?.setPitch(pitch)
    }

    /**
     * Speaks the given AI response aloud naturally.
     * Strips Markdown syntax (bold, headings, bullet marks, code blocks) to ensure natural sounding speech.
     */
    fun speak(
        text: String,
        queueMode: Int = TextToSpeech.QUEUE_FLUSH,
        onStart: (() -> Unit)? = null,
        onDone: (() -> Unit)? = null,
        onError: ((String?) -> Unit)? = null
    ) {
        if (!_isInitialized.value) {
            Log.w(TAG, "Cannot speak: TextToSpeech is not yet initialized")
            onError?.invoke("TTS not initialized")
            return
        }

        val cleanText = normalizeTextForSpeech(text)
        if (cleanText.isBlank()) {
            return
        }

        currentOnStart = onStart
        currentOnDone = onDone
        currentOnError = onError

        val utteranceId = "tts_${System.currentTimeMillis()}"
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }

        val result = tts?.speak(cleanText, queueMode, params, utteranceId)
        if (result == TextToSpeech.ERROR) {
            _isSpeaking.value = false
            clearCallbacks()
            onError?.invoke("Failed to start speaking")
        }
    }

    /**
     * Immediately stops ongoing speech output.
     */
    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
        clearCallbacks()
    }

    /**
     * Releases the native TextToSpeech engine. Should be called when the ViewModel or Activity is destroyed.
     */
    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
        _isInitialized.value = false
    }

    /**
     * Preprocesses text generated by AI models so that punctuation and formatting
     * sound natural and pleasant to human ears.
     */
    private fun normalizeTextForSpeech(input: String): String {
        return input
            // Remove code blocks
            .replace(Regex("```[\\s\\S]*?```"), " Bloco de código omitido. ")
            // Remove inline code
            .replace(Regex("`([^`]+)`"), "$1")
            // Remove markdown headers
            .replace(Regex("#{1,6}\\s*"), "")
            // Remove bold/italics
            .replace(Regex("\\*\\*([^*]+)\\*\\*"), "$1")
            .replace(Regex("\\*([^*]+)\\*"), "$1")
            .replace(Regex("__([^_]+)__"), "$1")
            .replace(Regex("_([^_]+)_"), "$1")
            // Remove markdown links [text](url) -> text
            .replace(Regex("\\[([^\\]]+)\\]\\([^)]+\\)"), "$1")
            // Replace list bullet points with natural pauses
            .replace(Regex("(?m)^\\s*[-*+]\\s+"), "")
            .replace(Regex("(?m)^\\s*\\d+\\.\\s+"), "")
            // Remove emoji / non-speech symbols that confuse TTS engines
            .replace(Regex("[⚡💻🤖🚀🌍✨🎙️💡🔥👍👋]"), "")
            // Normalize multiple whitespaces and newlines
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
