package com.example.easybot

// Data models used across screens

data class ChatListItem(
    val chatId: Int,
    val title: String,
    val modelName: String?,
    val lastMessageText: String,
    val lastMessageTime: String?,
    val isIncognito: Boolean
)

data class ExportMessageDto(
    val messageId: Long,
    val chatId: Long,
    val role: Int,
    val content: String,
    val createdAt: String
)

data class ExportChatDto(
    val chatId: Long,
    val title: String,
    val createdAt: String,
    var messages: List<ExportMessageDto>
)

data class SingleChatExportDto(
    val chat: com.example.easybot.dto.ChatDto,
    val messages: List<com.example.easybot.dto.MessageDto>
)
