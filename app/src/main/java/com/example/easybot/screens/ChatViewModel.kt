package com.example.easybot.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.easybot.MessageDto
import com.example.easybot.data.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ChatRepository()

    private var chatId: Long = -1L

    private val _messages = MutableStateFlow<List<MessageDto>>(emptyList())
    val messages: StateFlow<List<MessageDto>> = _messages.asStateFlow()

    fun init(chatId: Long) {
        if (this.chatId == chatId) return
        this.chatId = chatId
        loadMessages()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            try {
                _messages.value = repository.getMessages(chatId.toInt())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendMessage(question: String) {
        if (chatId == -1L || question.isBlank()) return

        viewModelScope.launch {
            // Оптимистично добавляем сообщение пользователя в UI
            val tempUserMessage = MessageDto(id = -1, chatId = chatId.toInt(), text = question, role = 1)
            _messages.value = _messages.value + tempUserMessage
            
            try {
                // Отправляем запрос и сразу получаем оба сообщения (пользователя и AI)
                val response = repository.sendMessage(chatId.toInt(), question)
                
                // Перезагружаем чат с сервера, чтобы получить актуальные данные с верными ID
                loadMessages()

            } catch (e: Exception) {
                e.printStackTrace()
                // В случае ошибки, можно показать сообщение об этом
                _messages.value = _messages.value.filter { it.id != -1 } // Убираем временное сообщение
                _messages.value = _messages.value + MessageDto(id = -2, chatId = chatId.toInt(), text = "Ошибка: ${e.message}", role = 0)
            }
        }
    }

    fun clearCurrentChat() {
        if (chatId == -1L) return
        viewModelScope.launch {
            try {
                repository.clearChat(chatId.toInt())
                loadMessages()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
