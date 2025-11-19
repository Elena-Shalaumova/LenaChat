package com.example.easybot

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.easybot.data.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatListViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ChatRepository()

    private val _chats = MutableStateFlow<List<ChatDto>>(emptyList())
    val chats: StateFlow<List<ChatDto>> = _chats.asStateFlow()

    init {
        loadChats()
    }

    fun loadChats() {
        viewModelScope.launch {
            try {
                _chats.value = repository.getChats()
            } catch (e: Exception) {
                e.printStackTrace()
                // Тут можно обработать ошибку, например, показать Toast
            }
        }
    }

    fun createChat(title: String) {
        viewModelScope.launch {
            try {
                repository.createChat(title)
                loadChats() // Перезагружаем список после создания
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteChat(chatId: Int) {
        viewModelScope.launch {
            try {
                repository.deleteChat(chatId)
                loadChats() // Перезагружаем список после удаления
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
