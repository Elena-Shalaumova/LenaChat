package com.example.easybot.data.remote.dto

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

data class CreateChatRequest(
    val title: String,
    val userId: Int,
    val isIncognito: Boolean = false
)

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

