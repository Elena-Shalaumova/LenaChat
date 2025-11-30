package com.example.easybot.data

import android.util.Log
import com.example.easybot.*
import com.example.easybot.data.mappers.toModels
import com.example.easybot.screens.theme.MessageModel

class ChatRepository(
    // фабрика
    private val api: WebApiChatAI = provideApi(),
) {
    private fun getUserId(): Int =
        UserSession.userId?.toInt() ?: error("User not logged in")

    // ---- Чаты ----
    suspend fun getChats(): List<ChatDto> {
        return api.getChats(getUserId())
    }

    suspend fun createChat(title: String): ChatDto {
        val request = CreateChatRequest(title = title, userId = getUserId())
        return api.createChat(request)
    }

    suspend fun deleteChat(chatId: Int) {
        api.deleteChat(chatId)
    }

    suspend fun clearChat(chatId: Int) {
        api.clearChat(chatId)
    }

    // ---- Сообщения ----
    suspend fun getMessages(chatId: Int): List<MessageModel> {
        val dtos = api.getMessages(chatId)
        // временный лог
        dtos.forEach {
            Log.d("MSG_DEBUG", "id=${it.id} type=${it.type} imagesCount=${it.images.size}")
        }
        return dtos.toModels()
    }

    // ---------- ТЕКСТ ----------
    suspend fun sendTextMessage(chatId: Int, text: String) {
        val userId = UserSession.userId?.toInt() ?: return   // или кинь ошибку, как хочешь

        val request = SendMessageRequest(
            chatId = chatId,
            userId = userId,
            text = text,                     // только текст
            base64Images = emptyList()       // без картинок
        )

        api.sendMessage(request)
    }

    // ---------- ОДНА КАРТИНКА (может быть + текст-подпись) ----------
    suspend fun sendImageMessage(
        chatId: Int,
        base64Image: String,
        prompt: String?
    ) {
        val userId = UserSession.userId?.toInt() ?: return   // или кинь ошибку, как хочешь

        val request = SendMessageRequest(
            chatId = chatId,
            userId = userId,
            text = prompt,                   // текст к картинке
            base64Images = listOf(base64Image)
        )

        api.sendMessage(request)
    }

    // ---------- НЕСКОЛЬКО КАРТИНОК (может быть + текст) ----------
    suspend fun sendImagesMessage(
        chatId: Int,
        images: List<String>,
        prompt: String?
    ) {
        val userId = UserSession.userId?.toInt() ?: return   // или кинь ошибку, как хочешь

        val request = SendMessageRequest(
            chatId = chatId,
            userId = userId,
            text = prompt,                   // общий текст для всех картинок
            base64Images = images            // список base64
        )

        api.sendMessage(request)
    }
}