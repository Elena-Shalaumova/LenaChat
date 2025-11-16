package com.example.easybot.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.easybot.Constants
import com.example.easybot.data.local.ChatDatabase
import com.example.easybot.data.local.ChatRepository
import com.example.easybot.data.local.MessageEntity
import com.example.easybot.MessageModel
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ChatRepository by lazy {
        val db = ChatDatabase.getInstance(application)
        ChatRepository(db.chatDao())
    }

    private val generativeModel: GenerativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = Constants.apiKey
        )
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
            // Сохраняем сообщение пользователя
            repository.insertMessage(
                chatId = chatId,
                role = "user",
                message = question
            )

            // Готовим историю для Gemini
            try {
                val chatHistory = repository.getMessages(chatId).first()
                val chat = generativeModel.startChat(
                    history = chatHistory
                        .filter { it.role != "user" || it.message != question } // Убираем последнее сообщение пользователя из истории
                        .map { 
                            content(it.role) { text(it.message) }
                        }
                )

                // Отправляем сообщение и получаем ответ
                val response = chat.sendMessage(question)

                // Сохраняем ответ Gemini
                response.text?.let {
                    repository.insertMessage(
                        chatId = chatId,
                        role = "model",
                        message = it
                    )
                }

            } catch (e: Exception) {
                // Обработка ошибок (например, записать в лог)
                e.printStackTrace()
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
