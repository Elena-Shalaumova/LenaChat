package com.example.easybot.data.remote.dto

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

