package com.example.easybot.dto

data class SettingsDto(val id: Int, val userId: Int, val stream: Boolean, val model: String?, val temperature: Double?, val maxTokens: Int? )
data class SettingsRequest(val id: Int, val stream: Boolean, val model: String, val temperature: Double?, val maxTokens: Int? )
data class OllamaVersionDto(val version: String)

data class ChatDto(
    val id: Int,
    val userId: Int,
    val title: String,
    val model: String? = null,
    val isIncognito: Boolean
)

data class MessageDto(
    val id: Int,
    val chatId: Int,
    val role: Int,
    val type: String,
    val text: String?,
    val images: List<String>,
    val createdAt: String?
)

data class ChatSettingsDto(
    val id: Int,
    val chatId: Int,
    val model: String,
    val temperature: Double?,
    val maxTokens: Int?
)

data class ChatSettingsRequest(
    val id: Int? = null,
    val chatId: Int,
    val model: String,
    val temperature: Double?,
    val maxTokens: Int?
)

data class CreateChatRequest(val title: String, val userId: Int, val isIncognito: Boolean = false)

data class SendMessageRequest(
    val chatId: Int,
    val userId: Int,
    val text: String?,
    val base64Images: List<String>
)

data class SendMessageResponse(
    val userMessage: MessageDto?,
    val aiMessage: MessageDto
)

data class LoginReq(val login: String, val password: String)

data class ChatRequest(val message: String)

data class ChatResponse(val answer: String)

data class SendImageMessageRequest(
    val chatId: Int,
    val userId: Int,
    val prompt: String?,
    val base64Image: String
)

data class RenameChatRequest(val title: String)
