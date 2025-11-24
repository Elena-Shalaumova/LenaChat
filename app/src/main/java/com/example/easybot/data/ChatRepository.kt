package com.example.easybot.data

import com.example.easybot.*
import com.example.easybot.data.mappers.toModels
import com.example.easybot.data.mappers.toModel
import com.example.easybot.screens.theme.MessageModel

class ChatRepository(
    private val api: WebApiChatAI = provideApi(),
) {
    private fun getUserId(): Int = UserSession.userId?.toInt() ?: error("User not logged in")

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
        return dtos.toModels()
    }

    // Метод теперь возвращает SendMessageResponse
    suspend fun sendMessage(chatId: Int, text: String): SendMessageResponse {
        val request = SendMessageRequest(chatId = chatId, text = text)
        return api.sendMessage(request)
    }
    // сообщение с КАРТИНКОЙ
    suspend fun sendImageMessage(
        chatId: Int,
        base64Image: String,
        prompt: String? = null
    ): SendMessageResponse {
        val request = SendImageMessageRequest(
            chatId = chatId,
            userId = getUserId(),
            prompt = prompt,
            base64Image = base64Image
        )
        return api.sendImageMessage(request)
    }
}
