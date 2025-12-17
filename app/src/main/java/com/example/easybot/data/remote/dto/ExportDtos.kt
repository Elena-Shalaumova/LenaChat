package com.example.easybot.data.remote.dto

import com.example.easybot.data.remote.api.ChatDto
import com.example.easybot.data.remote.api.MessageDto

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
    val chat: ChatDto,
    val messages: List<MessageDto>
)
