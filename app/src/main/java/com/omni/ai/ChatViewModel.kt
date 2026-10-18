package com.omni.ai

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.omni.ai.ai.AiException
import com.omni.ai.ai.AiProvider
import com.omni.ai.ai.ChatMessage
import com.omni.ai.ai.GeminiProvider
import com.omni.ai.ai.GenerationConfig
import com.omni.ai.data.AppSettings
import com.omni.ai.data.SettingsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val store = SettingsStore(application)
    private val provider: AiProvider = GeminiProvider()

    val settings: StateFlow<AppSettings> = store.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun send(raw: String) {
        val text = raw.trim()
        if (text.isEmpty() || _busy.value) return

        val current = settings.value
        when {
            current.apiKey.isBlank() -> {
                _error.value = "पहिल्यांदा Settings मध्ये Gemini API key टाका."
                return
            }
            current.model.isBlank() -> {
                _error.value = "Settings मध्ये 'जोडणी तपासा' दाबून मॉडेल निवडा."
                return
            }
        }

        val history = _messages.value
        _messages.value = history + ChatMessage(ChatMessage.Role.USER, text)
        _error.value = null
        _busy.value = true

        viewModelScope.launch {
            val config = GenerationConfig(
                apiKey = current.apiKey,
                model = current.model,
                temperature = current.temperature,
                systemPrompt = systemPrompt(current.companionName),
            )
            provider.generate(config, history, text)
                .onSuccess { reply ->
                    _messages.value = _messages.value + ChatMessage(ChatMessage.Role.ASSISTANT, reply)
                }
                .onFailure { e ->
                    _error.value = e.message ?: "अज्ञात त्रुटी"
                }
            _busy.value = false
        }
    }

    suspend fun fetchModels(apiKey: String): Result<List<String>> {
        if (apiKey.isBlank()) return Result.failure(AiException("आधी API key टाका."))
        return provider.testConnection(apiKey)
    }

    suspend fun saveSettings(settings: AppSettings) {
        store.save(settings)
    }

    fun clearError() {
        _error.value = null
    }

    fun clearChat() {
        _messages.value = emptyList()
        _error.value = null
    }

    private fun systemPrompt(name: String): String = """
        तुम्ही "$name" नावाची एक उबदार, मैत्रीपूर्ण AI सहाय्यक आहात.
        - वापरकर्ता ज्या भाषेत बोलतो (मराठी, हिंदी, इंग्रजी किंवा मिश्र) त्याच भाषेत उत्तर द्या.
        - तुम्ही खरी व्यक्ती आहात असे कधीही सांगू नका.
        - उत्तरे स्पष्ट, संक्षिप्त आणि उपयुक्त ठेवा.
        - वापरकर्त्याच्या सीमा आणि गोपनीयतेचा आदर करा.
    """.trimIndent()
}
