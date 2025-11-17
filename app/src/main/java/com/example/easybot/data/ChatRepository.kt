package com.example.easybot.data.local

import com.example.easybot.* // Импортируем все необходимое, включая ChatRequest
import com.example.easybot.data.local.ChatDao
import com.example.easybot.data.local.MessageEntity
import com.example.easybot.data.local.ChatEntity
import kotlinx.coroutines.flow.Flow

class ChatRepository(
    private val dao: ChatDao,
    private val api: WebApiChatAI = provideApi() // Добавляем экземпляр API
) {
    // ---- Новый метод для общения с Ollama ----
    suspend fun sendMessageToAi(text: String): String {
        val response = api.chat(ChatRequest(message = text))
        return response.answer
    }

    // ---- Чаты текущего пользователя ----
    fun getChatsForCurrentUser(): Flow<List<ChatEntity>> {
        val userId = UserSession.userId ?: error("User not logged in")
        return dao.getChats(userId)
    }

    suspend fun createChat(title: String): Long {
        val userId = UserSession.userId ?: error("User not logged in")

        val chat = ChatEntity(
            userId = userId,
            title  = title
        )
        return dao.insertChat(chat)
    }

    suspend fun deleteChat(chatId: Long) {
        dao.deleteChatWithMessages(chatId)
    }

    // ---- Сообщения ----
    fun getMessages(chatId: Long): Flow<List<MessageEntity>> =
        dao.getMessages(chatId)

    suspend fun insertMessage(chatId: Long, role: String, message: String): Long =
        dao.insertMessage(
            MessageEntity(
                chatId = chatId,
                role   = role,
                message = message
            )
        )

    suspend fun clearChat(chatId: Long) {
        dao.clearChat(chatId)
    }
}
