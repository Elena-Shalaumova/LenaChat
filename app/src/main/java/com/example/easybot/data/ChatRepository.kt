package com.example.easybot.data

import com.example.easybot.*

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
    suspend fun getMessages(chatId: Int): List<MessageDto> {
        return api.getMessages(chatId)
    }

    // Метод теперь возвращает SendMessageResponse
    suspend fun sendMessage(chatId: Int, text: String): SendMessageResponse {
        val request = SendMessageRequest(chatId = chatId, text = text)
        return api.sendMessage(request)
    }
}
