package com.omni.ai.ai

data class ChatMessage(val role: Role, val text: String) {
    enum class Role { USER, ASSISTANT }
}

data class GenerationConfig(
    val apiKey: String,
    val model: String,
    val temperature: Float,
    val systemPrompt: String,
)

class AiException(message: String) : Exception(message)

interface AiProvider {
    val displayName: String

    suspend fun testConnection(apiKey: String): Result<List<String>>

    suspend fun generate(
        config: GenerationConfig,
        history: List<ChatMessage>,
        userText: String,
    ): Result<String>
}
