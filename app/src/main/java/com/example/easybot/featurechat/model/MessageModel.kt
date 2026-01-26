package com.example.easybot.featurechat.model

// UI-модель для чата
data class MessageModel(
    val id: Long,
    val chatId: Long,
    val role: Int,          // 1 user, 0 assistant
    val type: String,       // "text" / "image"
    val text: String?,
    val images: List<String> = emptyList(),
    val document: DocumentAttachment? = null,
    val createdAt: Long
)