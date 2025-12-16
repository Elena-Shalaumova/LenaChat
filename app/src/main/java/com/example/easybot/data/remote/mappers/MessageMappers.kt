package com.example.easybot.data.remote.mappers

import com.example.easybot.data.remote.dto.MessageDto
import com.example.easybot.presentation.feature_chat.ui.theme.MessageModel


// -----------------------------
// DTO → UI Model
// -----------------------------
fun MessageDto.toModel(): MessageModel =
    MessageModel(
        id = id.toLong(),
        chatId = chatId.toLong(),
        role = role,
        type = type,
        text = text,
        images = images,
        createdAt = createdAt?.toLongOrNull() ?: System.currentTimeMillis()
    )
// -----------------------------
// DTO list -> UI Model list
// -----------------------------
fun List<MessageDto>.toModels(): List<MessageModel> =
    this.map { it.toModel() }
