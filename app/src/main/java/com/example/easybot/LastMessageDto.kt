package com.example.easybot

data class LastMessageDto(
    val chatId: Int,
    val messageId: Int,
    val text: String,
    val role: Int,
    val type: String,
    val createdAt: String
)
