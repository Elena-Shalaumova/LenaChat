package com.example.easybot.data.mappers

import com.example.easybot.featurechat.model.MessageModel
import com.example.easybot.data.remote.api.MessageDto


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
