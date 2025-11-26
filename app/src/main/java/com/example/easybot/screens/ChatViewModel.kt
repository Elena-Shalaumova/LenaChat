package com.example.easybot.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.easybot.data.ChatRepository
import com.example.easybot.screens.theme.MessageModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChatRepository()

    private var chatId: Long = -1L

    private val _messages = MutableStateFlow<List<MessageModel>>(emptyList())
    val messages: StateFlow<List<MessageModel>> = _messages.asStateFlow()

    fun init(chatId: Long) {
        if (this.chatId == chatId) return
        this.chatId = chatId
        loadMessages()
    }

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

    // ========== ТЕКСТОВОЕ СООБЩЕНИЕ ==========
    fun sendMessage(question: String) {
        if (chatId == -1L || question.isBlank()) return

        viewModelScope.launch {

            // 1. Добавляем временное сообщение
            val tmp = MessageModel(
                id = -1L,
                chatId = chatId,
                role = 1,
                type = "text",
                text = question,
                imageBase64 = null,
                createdAt = System.currentTimeMillis()
            )
            _messages.value = _messages.value + tmp

            try {
                // 2. Отправляем на сервер
                repository.sendTextMessage(
                    chatId = chatId.toInt(),
                    text = question
                )

                // 3. Перезагружаем историю
                loadMessages()

            } catch (e: Exception) {
                e.printStackTrace()

                // Удаляем временное сообщение
                _messages.value = _messages.value.filter { it.id != -1L }

                val errorMsg = MessageModel(
                    id = -2L,
                    chatId = chatId,
                    role = 0,
                    type = "text",
                    text = "Ошибка отправки сообщения: ${e.message}",
                    imageBase64 = null,
                    createdAt = System.currentTimeMillis()
                )
                _messages.value = _messages.value + errorMsg
            }
        }
    }

    // ========== СООБЩЕНИЕ С КАРТИНКОЙ ==========
    fun sendImageMessage(base64Image: String, prompt: String?) {
        if (chatId == -1L) return

        viewModelScope.launch {

            // 1. Временное сообщение
            val tmp = MessageModel(
                id = -1L,
                chatId = chatId,
                role = 1,
                type = "image",
                text = prompt,
                imageBase64 = base64Image,
                createdAt = System.currentTimeMillis()
            )
            _messages.value = _messages.value + tmp

            try {
                repository.sendImageMessage(
                    chatId = chatId.toInt(),
                    base64Image = base64Image,
                    prompt = prompt
                )

                loadMessages()

            } catch (e: Exception) {
                e.printStackTrace()

                _messages.value = _messages.value.filter { it.id != -1L }

                val err = MessageModel(
                    id = -2L,
                    chatId = chatId,
                    role = 0,
                    type = "text",
                    text = "Ошибка отправки изображения: ${e.message}",
                    imageBase64 = null,
                    createdAt = System.currentTimeMillis()
                )

                _messages.value = _messages.value + err
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

//    fun sendImageFromCamera(base64Image: String) {
//        viewModelScope.launch {
//            try {
//                val userId = UserSession.userId ?: return@launch
//                val chatId = currentChatId ?: return@launch
//
//                val request = SendMessageRequest(
//                    chatId = chatId,
//                    userId = userId,
//                    text = null,              // текста нет
//                    base64Image = base64Image // только картинка
//                )
//
//                val response = api.sendMessage(request)
//
//                // обновляем локальный список сообщений (как у тебя уже делается для обычного sendMessage)
//                // например:
//                // _messages.add(response.userMessage)
//                // _messages.add(response.aiMessage)
//
//            } catch (e: Exception) {
//                // обработка ошибки
//                e.printStackTrace()
//            }
//        }
//    }


}
