package com.example.easybot.data.remote.dto

data class SettingsDto(
    val id: Int,
    val userId: Int,
    val stream: Boolean,
    val model: String?,
    val temperature: Double?,
    val maxTokens: Int?
)

data class SettingsRequest(
    val id: Int,
    val stream: Boolean,
    val model: String,
    val temperature: Double?,
    val maxTokens: Int?
)

data class OllamaVersionDto(val version: String)

