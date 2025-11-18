package com.example.easybot.screens.theme

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.easybot.UserSession
import com.example.easybot.data.local.ChatRepository
import com.example.easybot.data.local.ChatDatabase
import com.example.easybot.data.local.ChatEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatListViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ChatRepository by lazy {
        val db = ChatDatabase.getInstance(application)
        ChatRepository(db.chatDao())
    }

    init {
        // можешь оставить как страховку,
        // но по идее после логина userId уже должен быть не null
        if (UserSession.userId == null) {
            UserSession.userId = -1L
        }
    }

    // список чатов текущего пользователя
    val chats: StateFlow<List<ChatEntity>> =
        repository
            .getChatsForCurrentUser()                       // ← БЕЗ параметров
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
                initialValue = emptyList()
            )

    // создать чат с дефолтным названием
    fun createChat() {
        viewModelScope.launch {
            repository.createChat("Новый чат")              // ← только title
        }
    }

    // если где-то нужно создавать с другим названием
    fun createChat(title: String) {
        viewModelScope.launch {
            repository.createChat(title)                    // ← только title
        }
    }

    fun deleteChat(chatId: Long) {
        viewModelScope.launch {
            repository.deleteChat(chatId)
        }
    }
}