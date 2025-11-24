package com.example.easybot.screens.theme

// UI-модель для чата
data class MessageModel(
    val id: Long,
    val chatId: Long,
    val role: Int,          // 1 user, 0 assistant
    val type: String,       // "text" / "image"
    val text: String?,
    val imageBase64: String?,
    val createdAt: Long
)

