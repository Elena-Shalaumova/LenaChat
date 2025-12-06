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

    // 🔹 Тут теперь лежит не ChatDto, а уже собранный ChatListItem
    private val _chats = MutableStateFlow<List<ChatListItem>>(emptyList())
    val chats: StateFlow<List<ChatListItem>> = _chats.asStateFlow()

    init {
        loadChats()
    }

    fun loadChats() {
        viewModelScope.launch {
            try {
                // 1) все чаты пользователя
                val chats = repository.getChats()

                // 2) последние сообщения по всем чатам пользователя
                val lastMessages = repository.getLastMessagesForUser()

                // 3) собираем всё в ChatListItem
                val items = chats.map { chat ->
                    val settings = repository.getChatSettings(chat.id)
                    val last = lastMessages.firstOrNull { it.chatId == chat.id }

                    ChatListItem(
                        chatId = chat.id,
                        title = chat.title,
                        modelName = settings.model,
                        lastMessageText = last?.text ?: "Нет сообщений",
                        lastMessageTime = last?.createdAt,
                        isIncognito = chat.isIncognito
                    )
                }

                _chats.value = items
            } catch (e: Exception) {
                e.printStackTrace()
                // Тут можно обработать ошибку, например, показать Toast/состояние ошибки
            }
        }
    }

    //fun createChat(title: String) {
    fun createChat(title: String, isIncognito: Boolean = false) {
        viewModelScope.launch {
            try {
                // 👇 передаём флаг дальше в репозиторий
                repository.createChat(title = title, isIncognito = isIncognito)

                loadChats() // перезагружаем список после создания
            } catch (e: Exception) {
                e.printStackTrace()
                // тут можешь показать Toast и т.п.
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

    fun renameChat(id: Int, newTitle: String) {
        viewModelScope.launch {
            try {
                api.renameChat(id, RenameChatRequest(newTitle))
                loadChats()
            } catch (e: Exception) {
                // обработка ошибки, тост и т.п.
                e.printStackTrace()
            }
        }
    }
}
