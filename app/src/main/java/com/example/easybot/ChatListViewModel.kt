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

    private val _chats = MutableStateFlow<List<ChatListItem>>(emptyList())
    val chats: StateFlow<List<ChatListItem>> = _chats.asStateFlow()

    init {
        loadChats()
    }

    fun loadChats() {
        viewModelScope.launch {
            try {
                val chats = repository.getChats()
                val lastMessages = repository.getLastMessagesForUser()

                val items = chats.map { chat ->
                    val settings = repository.getChatSettings(chat.id)
                    val last = lastMessages.firstOrNull { it.chatId == chat.id }

                    ChatListItem(
                        chatId = chat.id,
                        title = chat.title,
                        modelName = settings.model,
                        lastMessageText = last?.text ?: "Нет сообщений",
                        lastMessageTime = last?.createdAt?.replace("T", " "),
                        isIncognito = chat.isIncognito
                    )
                }

                _chats.value = items
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ⬇ НОВАЯ версия createChat — suspend + возвращает ChatListItem
    suspend fun createChat(title: String, isIncognito: Boolean = false): ChatListItem {
        return try {
            // репозиторий должен вернуть DTO созданного чата с id и флагом
            val dto = repository.createChat(title = title, isIncognito = isIncognito)

            val item = ChatListItem(
                chatId = dto.id,
                title = dto.title,
                modelName = null,             // пока модель не выбрана
                lastMessageText = "Нет сообщений",
                lastMessageTime = null,
                isIncognito = dto.isIncognito // важно: берём с бэка
            )

            // добавляем в текущий список, чтобы он сразу появился в списке чатов
            _chats.value = _chats.value + item

            item
        } catch (e: Exception) {
            e.printStackTrace()
            throw e          // пробрасываем, чтобы диалог мог отреагировать
        }
    }

    fun deleteChat(chatId: Int) {
        viewModelScope.launch {
            try {
                repository.deleteChat(chatId)
                loadChats()
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
                e.printStackTrace()
            }
        }
    }
}
