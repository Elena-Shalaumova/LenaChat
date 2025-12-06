package com.example.easybot.data

import android.util.Log
import com.example.easybot.*
import com.example.easybot.data.mappers.toModels
import com.example.easybot.screens.theme.MessageModel

//подключение к бэку
class ChatRepository(
    private val api: WebApiChatAI = provideApi(UserSession.apiBaseUrl),
) {
    private fun getUserId(): Int =
        UserSession.userId?.toInt() ?: error("User not logged in")

    // ---- Чаты ----
    suspend fun getChats(): List<ChatDto> {
        return api.getChats(getUserId())
    }

    suspend fun createChat(title: String, isIncognito: Boolean): ChatDto {
        val request = CreateChatRequest(title = title, userId = getUserId(),isIncognito = isIncognito)
        return api.createChat(request)
    }

    suspend fun deleteChat(chatId: Int) {
        api.deleteChat(chatId)
    }

    suspend fun clearChat(chatId: Int) {
        api.clearChat(chatId)
    }

    // 🔹 NEW: настройки конкретного чата (из settings_chat)
    suspend fun getChatSettings(chatId: Int): ChatSettingsDto {
        return api.getChatSettings(chatId)
    }

    // 🔹 NEW: последние сообщения по всем чатам пользователя
    suspend fun getLastMessagesForUser(): List<LastMessageDto> {
        return api.getLastMessagesForUser(getUserId())
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
    // возвращаем ответ ИИ как MessageModel
    suspend fun sendTextMessage(chatId: Int, text: String): MessageModel {
        val userId = getUserId()

        val request = SendMessageRequest(
            chatId = chatId,
            userId = userId,
            text = text,
            base64Images = emptyList()
        )

        val response = api.sendMessage(request)
        // response.aiMessage: MessageDto -> конвертим через toModels()
        return listOf(response.aiMessage).toModels().first()
    }

    // ---------- ОДНА КАРТИНКА (может быть + текст-подпись) ----------
    suspend fun sendImageMessage(
        chatId: Int,
        base64Image: String,
        prompt: String?
    ): MessageModel {
        val userId = getUserId()

        val request = SendMessageRequest(
            chatId = chatId,
            userId = userId,
            text = prompt,
            base64Images = listOf(base64Image)
        )

        val response = api.sendMessage(request)
        return listOf(response.aiMessage).toModels().first()
    }

    // ---------- НЕСКОЛЬКО КАРТИНОК (может быть + текст) ----------
    suspend fun sendImagesMessage(
        chatId: Int,
        images: List<String>,
        prompt: String?
    ): MessageModel {
        val userId = getUserId()

        val request = SendMessageRequest(
            chatId = chatId,
            userId = userId,
            text = prompt,
            base64Images = images
        )

        val response = api.sendMessage(request)
        return listOf(response.aiMessage).toModels().first()
    }

}
