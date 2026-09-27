package com.example.viewmodel

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.network.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {
    private val db = AppDatabase.getDatabase(application)
    private val repository = AgentRepository(db.agentDao(), db.messageDao())

    private var tts: TextToSpeech? = null
    var isTtsInitialized = false
        private set

    val agents: StateFlow<List<AgentEntity>> = repository.allAgents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedAgent = MutableStateFlow<AgentEntity?>(null)
    val selectedAgent: StateFlow<AgentEntity?> = _selectedAgent

    private val _messages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val messages: StateFlow<List<MessageEntity>> = _messages

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab

    init {
        tts = TextToSpeech(application, this)
        viewModelScope.launch {
            repository.allAgents.collect { list ->
                if (list.isEmpty()) {
                    repository.insertAgent(
                        AgentEntity(
                            name = "OpenCode",
                            description = "Especialista em engenharia de software e programação",
                            systemPrompt = "Você é o OpenCode, um assistente de inteligência artificial especialista em desenvolvimento de software, arquitetura de sistemas, programação em Kotlin/Compose e depuração de código. Seja técnico, preciso e forneça código limpo.",
                            emoji = "💻",
                            category = "Desenvolvimento",
                            isCustom = false
                        )
                    )
                    repository.insertAgent(
                        AgentEntity(
                            name = "PROJECT ALPHA",
                            description = "Agente conversacional de alta performance e baixa latência",
                            systemPrompt = "Você é o PROJECT ALPHA, um agente conversacional avançado de inteligência artificial em português. Você responde com inteligência, precisão, naturalidade e clareza.",
                            emoji = "⚡",
                            category = "AI Core",
                            isCustom = false
                        )
                    )
                    repository.insertAgent(
                        AgentEntity(
                            name = "Sofia",
                            description = "Assistente geral amigável e inteligente",
                            systemPrompt = "Você é a Sofia, uma assistente virtual inteligente, amigável e prestativa em português. Responda de forma natural, calorosa e concisa.",
                            emoji = "🤖",
                            category = "Geral",
                            isCustom = false
                        )
                    )
                    repository.insertAgent(
                        AgentEntity(
                            name = "Lucas",
                            description = "Coach de produtividade e foco de alta performance",
                            systemPrompt = "Você é o Lucas, um coach de produtividade focado em ajudar o usuário a alcançar metas, organizar tarefas, superar procrastinação e manter alta energia.",
                            emoji = "🚀",
                            category = "Produtividade",
                            isCustom = false
                        )
                    )
                    repository.insertAgent(
                        AgentEntity(
                            name = "Helena",
                            description = "Mentora de idiomas e comunicação verbal",
                            systemPrompt = "Você é a Helena, uma professora paciente e experiente em comunicação e línguas. Ajude o usuário a se expressar melhor em português e outros idiomas.",
                            emoji = "🌍",
                            category = "Educação",
                            isCustom = false
                        )
                    )
                } else if (_selectedAgent.value == null) {
                    _selectedAgent.value = list.first()
                    loadMessagesForAgent(list.first().id)
                }
            }
        }
    }

    private val cachedApiKey: String by lazy {
        try {
            val field = com.example.BuildConfig::class.java.getField("GEMINI_API_KEY")
            (field.get(null) as? String)?.takeIf { it.isNotBlank() && it != "MY_GEMINI_API_KEY" }
        } catch (e: Exception) {
            null
        } ?: "AQ.Ab8RN6JiI5KmEyEingef2-ttIi7buawByh9aEhKjrl_pJT-IUg"
    }

    private fun getApiKey(): String {
        return cachedApiKey
    }

    private val cachedOpenCodeApiKey: String by lazy {
        try {
            val field = com.example.BuildConfig::class.java.getField("OPENCODE_API_KEY")
            (field.get(null) as? String)?.takeIf { it.isNotBlank() && it != "MY_OPENCODE_API_KEY" }
        } catch (e: Exception) {
            null
        } ?: "oc_sk_132e769f4a78_Qsp7flsrtBCdDQQsnCGJb5wpToOznP_T"
    }

    private fun getOpenCodeApiKey(): String {
        return cachedOpenCodeApiKey
    }

    private val _openAiApiKey = MutableStateFlow(getApiKey())
    val openAiApiKey: StateFlow<String> = _openAiApiKey

    fun saveOpenAiApiKey(key: String) {
        _openAiApiKey.value = key
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.forLanguageTag("pt-BR"))
            isTtsInitialized = (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED)
        }
    }

    fun selectAgent(agent: AgentEntity) {
        _selectedAgent.value = agent
        loadMessagesForAgent(agent.id)
    }

    private fun loadMessagesForAgent(agentId: Long) {
        viewModelScope.launch {
            repository.getMessages(agentId).collect { msgList ->
                _messages.value = msgList
            }
        }
    }

    fun setTab(tab: Int) {
        _currentTab.value = tab
    }

    fun setListening(listening: Boolean) {
        _isListening.value = listening
    }

    fun sendMessage(text: String) {
        val agent = _selectedAgent.value ?: return
        if (text.isBlank()) return

        viewModelScope.launch {
            repository.saveMessage(agent.id, "user", text)
            _isLoading.value = true

            try {
                var reply: String? = null
                if (agent.name.equals("OpenCode", ignoreCase = true)) {
                    try {
                        val ocKey = getOpenCodeApiKey()
                        val messagesList = mutableListOf<OpenCodeMessage>()
                        messagesList.add(OpenCodeMessage(role = "system", content = agent.systemPrompt))
                        for (msg in _messages.value.takeLast(10)) {
                            val role = if (msg.sender == "user") "user" else "assistant"
                            messagesList.add(OpenCodeMessage(role = role, content = msg.text))
                        }
                        messagesList.add(OpenCodeMessage(role = "user", content = text))

                        val ocReq = OpenCodeChatRequest(
                            model = "deepseek-v4.1-flash",
                            messages = messagesList
                        )
                        val ocResp = OpenCodeClient.service.chatCompletions("Bearer $ocKey", ocReq)
                        reply = ocResp.choices?.firstOrNull()?.message?.content
                    } catch (e: Exception) {
                        reply = null
                    }
                }

                if (reply.isNullOrBlank()) {
                    val apiKey = getApiKey()
                    val chatHistory = _messages.value.takeLast(10).map { msg ->
                        Content(parts = listOf(Part(text = "${msg.sender}: ${msg.text}")))
                    }.toMutableList()

                    chatHistory.add(Content(parts = listOf(Part(text = "user: $text"))))

                    val request = GenerateContentRequest(
                        contents = chatHistory,
                        systemInstruction = Content(parts = listOf(Part(text = agent.systemPrompt))),
                        generationConfig = GenerationConfig(temperature = 0.7f)
                    )

                    if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                        reply = "Olá! Para conversar com inteligência artificial real, configure sua chave no AI Studio. (Mensagem simulada: Recebi sua mensagem '$text')"
                    } else {
                        val response = GeminiClient.service.generateContent(apiKey, request)
                        reply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                            ?: "Desculpe, não consegui processar sua resposta."
                    }
                }

                repository.saveMessage(agent.id, "agent", reply)
                speak(reply)
            } catch (e: Exception) {
                val errorMsg = "Erro de conexão: ${e.localizedMessage ?: "Verifique sua internet."}"
                repository.saveMessage(agent.id, "agent", errorMsg)
                speak("Ocorreu um erro ao conectar com o agente.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createNewAgent(name: String, description: String, prompt: String, emoji: String, category: String) {
        viewModelScope.launch {
            val newId = repository.insertAgent(
                AgentEntity(
                    name = name,
                    description = description,
                    systemPrompt = prompt,
                    emoji = emoji,
                    category = category,
                    isCustom = true
                )
            )
            val created = AgentEntity(
                id = newId, name = name, description = description, systemPrompt = prompt, emoji = emoji, category = category, isCustom = true
            )
            selectAgent(created)
            speak("Novo agente $name criado com sucesso!")
        }
    }

    fun createAgentViaVoiceDescription(voiceDescription: String, onFinished: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val apiKey = getApiKey()
                if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                    val cleanDesc = voiceDescription.take(50)
                    val newId = repository.insertAgent(
                        AgentEntity(
                            name = "Agente Voz",
                            description = cleanDesc,
                            systemPrompt = "Você é um assistente criado por voz: $voiceDescription. Seja prestativo e simpático em português.",
                            emoji = "🎙️",
                            category = "Voz",
                            isCustom = true
                        )
                    )
                    val created = AgentEntity(
                        id = newId, name = "Agente Voz", description = cleanDesc, systemPrompt = "...", emoji = "🎙️", category = "Voz", isCustom = true
                    )
                    selectAgent(created)
                    speak("Agente criado por voz com sucesso!")
                } else {
                    val prompt = """
                        Com base na descrição do usuário para um novo agente de inteligência artificial: "$voiceDescription"
                        Gere um JSON com os campos:
                        - "name": Nome criativo do agente (ex: Einstein, NutriBot, DevMentor)
                        - "description": Breve resumo de sua atuação (máximo 60 caracteres)
                        - "systemPrompt": Instrução de sistema completa orientando a personalidade e missão em português
                        - "emoji": Um emoji correspondente (ex: 🔬, 🥗, 💻)

                        Retorne APENAS o JSON puro.
                    """.trimIndent()

                    val request = GenerateContentRequest(
                        contents = listOf(Content(parts = listOf(Part(text = prompt))))
                    )
                    val response = GeminiClient.service.generateContent(apiKey, request)
                    val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
                    
                    var agentName = "Agente de Voz"
                    var agentDesc = voiceDescription.take(60)
                    var agentPrompt = "Você é um assistente especializado em: $voiceDescription."
                    var agentEmoji = "✨"

                    try {
                        val jsonPart = rawText.substringAfter("{").substringBeforeLast("}")
                        val fullJson = "{$jsonPart}"
                        val adapter = GeminiClient.moshi.adapter(AgentJson::class.java)
                        val parsed = adapter.fromJson(fullJson)
                        if (parsed != null) {
                            agentName = parsed.name?.takeIf { it.isNotBlank() } ?: agentName
                            agentDesc = parsed.description?.takeIf { it.isNotBlank() } ?: agentDesc
                            agentPrompt = parsed.systemPrompt?.takeIf { it.isNotBlank() } ?: agentPrompt
                            agentEmoji = parsed.emoji?.takeIf { it.isNotBlank() } ?: agentEmoji
                        }
                    } catch (e: Exception) {
                        // Fallback safely to defaults
                    }

                    val newId = repository.insertAgent(
                        AgentEntity(
                            name = agentName,
                            description = agentDesc,
                            systemPrompt = agentPrompt,
                            emoji = agentEmoji,
                            category = "Personalizado",
                            isCustom = true
                        )
                    )
                    val created = AgentEntity(
                        id = newId, name = agentName, description = agentDesc, systemPrompt = agentPrompt, emoji = agentEmoji, category = "Personalizado", isCustom = true
                    )
                    selectAgent(created)
                    speak("Seu agente $agentName foi criado com sucesso! Agora você já pode conversar com ele.")
                }
            } catch (e: Exception) {
                val newId = repository.insertAgent(
                    AgentEntity(
                        name = "Assistente",
                        description = voiceDescription.take(50),
                        systemPrompt = "Você é um assistente baseado em: $voiceDescription",
                        emoji = "🤖",
                        category = "Voz",
                        isCustom = true
                    )
                )
                val created = AgentEntity(id = newId, name = "Assistente", description = voiceDescription.take(50), systemPrompt = "...", emoji = "🤖", category = "Voz", isCustom = true)
                selectAgent(created)
                speak("Novo agente criado por voz!")
            } finally {
                _isLoading.value = false
                onFinished()
            }
        }
    }

    fun clearCurrentAgentMessages() {
        val agent = _selectedAgent.value ?: return
        viewModelScope.launch {
            repository.clearMessages(agent.id)
            _messages.value = emptyList()
        }
    }

    fun deleteAgent(id: Long) {
        viewModelScope.launch {
            repository.deleteAgent(id)
            if (_selectedAgent.value?.id == id) {
                agents.value.firstOrNull { it.id != id }?.let { selectAgent(it) }
            }
        }
    }

    fun speak(text: String) {
        if (isTtsInitialized) {
            _isSpeaking.value = true
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
            viewModelScope.launch {
                // Approximate speaking time or reset
                val durationMs = (text.length * 70L).coerceIn(2000L, 15000L)
                kotlinx.coroutines.delay(durationMs)
                _isSpeaking.value = false
            }
        }
    }

    fun stopSpeaking() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun repeatLastMessage() {
        val lastAgentMsg = _messages.value.lastOrNull { it.sender == "agent" }
        if (lastAgentMsg != null) {
            speak(lastAgentMsg.text)
        }
    }

    override fun onCleared() {
        tts?.stop()
        tts?.shutdown()
        super.onCleared()
    }
}
