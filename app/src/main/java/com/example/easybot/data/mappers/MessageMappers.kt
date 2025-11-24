package com.example.easybot.data.mappers

import com.example.easybot.screens.theme.MessageModel
import com.example.easybot.MessageDto


// -----------------------------
// DTO → UI Model
// -----------------------------
fun MessageDto.toModel(): MessageModel =
    MessageModel(
        id = id.toLong(),
        chatId = chatId.toLong(),
        role = role,
       // type = if (base64Image != null) "image" else "text",
        type = type,
        text = text,
        imageBase64 = base64Image,
        createdAt = createdAt?.toLongOrNull() ?: System.currentTimeMillis()
    )
// -----------------------------
// DTO list -> UI Model list
// -----------------------------
fun List<MessageDto>.toModels(): List<MessageModel> =
    this.map { it.toModel() }
