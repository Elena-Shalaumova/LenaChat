package com.example.easybot.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.easybot.UserSession
//import com.example.easybot.MessageDto
import com.example.easybot.data.ChatRepository
import com.example.easybot.screens.theme.MessageModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ChatRepository()

    // id текущего чата
    private var chatId: Long = -1L

    // состояние сообщений чата (UI-модель)
    private val _messages = MutableStateFlow<List<MessageModel>>(emptyList())
    val messages: StateFlow<List<MessageModel>> = _messages.asStateFlow()

    fun init(chatId: Long) {
        if (this.chatId == chatId) return
        this.chatId = chatId
        loadMessages()
    }

    // загрузка истории сообщений
    private fun loadMessages() {
        if (chatId == -1L) return
        viewModelScope.launch {
            try {
                _messages.value = repository.getMessages(chatId.toInt())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    // ===== ТЕКСТОВОЕ СООБЩЕНИЕ =====
    fun sendMessage(question: String) {
        if (chatId == -1L || question.isBlank()) return

        viewModelScope.launch {
            // Оптимистично добавляем сообщение пользователя в UI
            val tempUserMessage = MessageModel(id = -1L, chatId = chatId, role=1, type = "text", text = question, imageBase64 = null, createdAt = System.currentTimeMillis() )
            _messages.value = _messages.value + tempUserMessage
            
            try {
                // Отправляем запрос и сразу получаем оба сообщения (пользователя и AI)
                repository.sendMessage(chatId.toInt(), question)
                
                // Перезагружаем чат с сервера, чтобы получить актуальные данные с верными ID
                loadMessages()
            } catch (e: Exception) {
                e.printStackTrace()

                // В случае ошибки, можно показать сообщение об этом
                _messages.value = _messages.value.filter { it.id != -1L } // Убираем временное сообщение

                val errorMessage = MessageModel(
                    id = -2L,
                    chatId = chatId,
                    role = 0,
                    type = "text",
                    text = "Ошибка отправки изображения: ${e.message ?: "неизвестная"}",
                    imageBase64 = null,
                    createdAt = System.currentTimeMillis()
                )
                _messages.value = _messages.value + errorMessage
            }
        }
    }

    // ===== СООБЩЕНИЕ С КАРТИНКОЙ =====
    fun sendImageMessage(base64Image: String, prompt: String?) {
        if (chatId == -1L) return

        viewModelScope.launch {
            // временное сообщение пользователя с картинкой
            val tempImageMessage = MessageModel(
                id = -1L,
                chatId = chatId,
                role = 1,
                type = "image",
                text = prompt,
                imageBase64 = base64Image,
                createdAt = System.currentTimeMillis()
            )
            _messages.value = _messages.value + tempImageMessage

            try {
                repository.sendImageMessage(
                    chatId = chatId.toInt(),
                    base64Image = base64Image,
                    prompt = prompt
                )

                // перезагрузим историю чата с сервера
                loadMessages()
            } catch (e: Exception) {
                e.printStackTrace()

                // убираем временное сообщение
                _messages.value = _messages.value.filter { it.id != -1L }

                val errorMessage = MessageModel(
                    id = -2L,
                    chatId = chatId,
                    role = 0,
                    type = "text",
                    text = "Ошибка отправки изображения: ${e.message ?: "неизвестная"}",
                    imageBase64 = null,
                    createdAt = System.currentTimeMillis()
                )
                _messages.value = _messages.value + errorMessage
            }
        }
    }

    fun clearCurrentChat() {
        if (chatId == -1L) return
        viewModelScope.launch {
            try {
                repository.clearChat(chatId.toInt())
                _messages.value = emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}