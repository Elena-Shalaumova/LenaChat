package com.example.easybot.screens

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.easybot.data.local.ChatDatabase
import com.example.easybot.data.local.ChatRepository
import com.example.easybot.data.local.MessageEntity
import com.example.easybot.MessageModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ChatRepository by lazy {
        val db = ChatDatabase.getInstance(application)
        ChatRepository(db.chatDao())
    }

    private var chatId: Long = -1L

    // 1. Создаем приватный MutableStateFlow
    private val _messageList = MutableStateFlow<List<MessageModel>>(emptyList())
    // 2. И публичный StateFlow, на который подпишется UI
    val messageList: StateFlow<List<MessageModel>> = _messageList.asStateFlow()

    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun init(chatId: Long) {
        if (this.chatId == chatId) return
        this.chatId = chatId

        // 3. Подписываемся на Flow из репозитория
        repository.getMessages(chatId)
            .onEach { entities ->
                // При любом изменении в базе данных, этот код сработает
                // и обновит наш StateFlow
                _messageList.value = entities.map { it.toModel() }
            }
            .launchIn(viewModelScope) // Запускаем подписку в скоупе ViewModel
    }

    fun sendMessage(text: String) {
        if (chatId == -1L || text.isBlank() || isLoading) return

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                // Просто сохраняем сообщение пользователя. UI обновится сам благодаря подписке.
                repository.insertMessage(chatId, "user", text)

                // Отправляем на сервер и получаем ответ
                val reply = repository.sendMessageToAi(text)

                // Сохраняем ответ. UI снова обновится сам.
                repository.insertMessage(chatId, "model", reply)

            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = e.message

                // Сохраняем ошибку. UI снова обновится сам.
                repository.insertMessage(chatId, "model", "Ошибка: ${e.message}")
            } finally {
                isLoading = false
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

// -------- Mapper --------
private fun MessageEntity.toModel(): MessageModel =
    MessageModel(
        message = message,
        role = role,
        isUser = role == "user"
    )
