package com.example.easybot.data.remote.dto

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

