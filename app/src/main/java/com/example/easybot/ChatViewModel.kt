package com.example.easybot.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.easybot.data.local.ChatDatabase
import com.example.easybot.data.local.ChatRepository
import com.example.easybot.data.local.MessageEntity
import com.example.easybot.MessageModel
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ChatRepository by lazy {
        val db = ChatDatabase.getInstance(application)
        ChatRepository(db.chatDao()) // Repository теперь создается с API внутри
    }

    private var chatId: Long = -1L

    var messageList by mutableStateOf<List<MessageModel>>(emptyList())
        private set

    fun init(chatId: Long) {
        if (this.chatId == chatId) return
        this.chatId = chatId

        viewModelScope.launch {
            repository.getMessages(chatId).collect { entities ->
                messageList = entities.map { it.toModel() }
            }
        }
    }

    fun sendMessage(question: String) {
        if (chatId == -1L || question.isBlank()) return

        viewModelScope.launch {
            // 1. Сохраняем сообщение пользователя в локальную БД
            repository.insertMessage(
                chatId = chatId,
                role = "user",
                message = question
            )

            // 2. Отправляем сообщение в Ollama через наш репозиторий
            try {
                val reply = repository.sendMessageToAi(question)

                // 3. Сохраняем ответ от Ollama в локальную БД
                repository.insertMessage(
                    chatId = chatId,
                    role = "model",
                    message = reply
                )
            } catch (e: Exception) {
                // Обработка ошибок сети или API
                e.printStackTrace()
                // Опционально: можно сохранить сообщение об ошибке в чат
                repository.insertMessage(
                    chatId = chatId,
                    role = "model",
                    message = "Ошибка: ${e.message}"
                )
            }
        }
    }

    fun clearCurrentChat() {
        if (chatId == -1L) return
        viewModelScope.launch {
            repository.clearChat(chatId)
        }
    }
}

private fun MessageEntity.toModel(): MessageModel =
    MessageModel(
        message = message,
        role = role,
        isUser = role == "user"
    )
