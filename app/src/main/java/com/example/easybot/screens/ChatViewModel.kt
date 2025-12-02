package com.example.easybot.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.easybot.data.ChatRepository
import com.example.easybot.screens.theme.MessageModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull   // <--- ВАЖНО

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChatRepository()

    private var chatId: Long = -1L

    private val _messages = MutableStateFlow<List<MessageModel>>(emptyList())
    val messages: StateFlow<List<MessageModel>> = _messages.asStateFlow()

    // инициализация при заходе в чат
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

            // 1. пузырь пользователя
            val userMsg = MessageModel(
                id = -1L,
                chatId = chatId,
                role = 1,
                type = "text",
                text = question,
                images = emptyList(),
                createdAt = System.currentTimeMillis()
            )
            _messages.value = _messages.value + userMsg

            // 2. заранее добавляем пустой пузырь ассистента ("...")
            val placeholderIndex = _messages.value.size
            _messages.value = _messages.value + MessageModel(
                id = -2L,
                chatId = chatId,
                role = 0,
                type = "text",
                text = "...",
                images = emptyList(),
                createdAt = System.currentTimeMillis()
            )

            try {
                // 3. ЖДЁМ ОТВЕТ НЕ ДОЛЬШЕ 3 МИНУТ
                val ai = withTimeoutOrNull(3 * 60_000L) {
                    repository.sendTextMessage(chatId.toInt(), question)
                }

                if (ai == null) {
                    // таймаут – заменяем плейсхолдер текстом ошибки
                    setErrorToPlaceholder(
                        placeholderIndex,
                        "Ответ занял больше 3 минут, запрос отменён."
                    )
                } else {
                    // 4. “печатаем” его внутрь плейсхолдера
                    streamAiMessageInto(placeholderIndex, ai)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                setErrorToPlaceholder(
                    placeholderIndex,
                    "Произошла ошибка при получении ответа."
                )
            }
        }
    }

    // ========== ОДНА КАРТИНКА (с текстом или без) ==========
    fun sendImageMessage(base64Image: String, prompt: String?) {
        if (chatId == -1L) return

        viewModelScope.launch {

            val userMsg = MessageModel(
                id = -1L,
                chatId = chatId,
                role = 1,
                type = "image",
                text = prompt,
                images = listOf(base64Image),
                createdAt = System.currentTimeMillis()
            )
            _messages.value = _messages.value + userMsg

            val placeholderIndex = _messages.value.size
            _messages.value = _messages.value + MessageModel(
                id = -2L,
                chatId = chatId,
                role = 0,
                type = "text",
                text = "...",
                images = emptyList(),
                createdAt = System.currentTimeMillis()
            )

            try {
                val ai = withTimeoutOrNull(3 * 60_000L) {
                    repository.sendImageMessage(
                        chatId = chatId.toInt(),
                        base64Image = base64Image,
                        prompt = prompt
                    )
                }

                if (ai == null) {
                    setErrorToPlaceholder(
                        placeholderIndex,
                        "Ответ занял больше 3 минут, запрос отменён."
                    )
                } else {
                    streamAiMessageInto(placeholderIndex, ai)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                setErrorToPlaceholder(
                    placeholderIndex,
                    "Произошла ошибка при получении ответа."
                )
            }
        }
    }

    // ========== НЕСКОЛЬКО КАРТИНОК (с текстом или без) ==========
    fun sendImagesMessage(images: List<String>, prompt: String?) {
        if (chatId == -1L || images.isEmpty()) return

        viewModelScope.launch {

            val userMsg = MessageModel(
                id = -1L,
                chatId = chatId,
                role = 1,
                type = "image",
                text = prompt,
                images = images,
                createdAt = System.currentTimeMillis()
            )
            _messages.value = _messages.value + userMsg

            val placeholderIndex = _messages.value.size
            _messages.value = _messages.value + MessageModel(
                id = -2L,
                chatId = chatId,
                role = 0,
                type = "text",
                text = "...",
                images = emptyList(),
                createdAt = System.currentTimeMillis()
            )

            try {
                val ai = withTimeoutOrNull(3 * 60_000L) {
                    repository.sendImagesMessage(
                        chatId = chatId.toInt(),
                        images = images,
                        prompt = prompt
                    )
                }

                if (ai == null) {
                    setErrorToPlaceholder(
                        placeholderIndex,
                        "Ответ занял больше 3 минут, запрос отменён."
                    )
                } else {
                    streamAiMessageInto(placeholderIndex, ai)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                setErrorToPlaceholder(
                    placeholderIndex,
                    "Произошла ошибка при получении ответа."
                )
            }
        }
    }

    // очистка чата
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

    // --- вспомогательная функция: записать текст ошибки в плейсхолдер ---
    private fun setErrorToPlaceholder(index: Int, errorText: String) {
        val updated = _messages.value.toMutableList()
        val old = updated.getOrNull(index) ?: return
        updated[index] = old.copy(text = errorText)
        _messages.value = updated
    }

    // постепенный стриминг ответа в плейсхолдер
    private suspend fun streamAiMessageInto(index: Int, full: MessageModel) {
        val fullText = full.text.orEmpty()
        if (fullText.isEmpty()) return

        val chunkSize = 4
        val delayMs = 55L

        for (i in fullText.indices step chunkSize) {
            val end = minOf(i + chunkSize, fullText.length)
            val current = fullText.substring(0, end)

            val updated = _messages.value.toMutableList()
            val old = updated.getOrNull(index) ?: return
            updated[index] = old.copy(text = current)
            _messages.value = updated

            delay(delayMs)
        }
    }
}
