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

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChatRepository()

    private var chatId: Long = -1L

    private val _messages = MutableStateFlow<List<MessageModel>>(emptyList())
    val messages: StateFlow<List<MessageModel>> = _messages.asStateFlow()

    // флаг: ИИ сейчас отвечает или нет
    //private val _isAiBusy = MutableStateFlow(false)
    //val isAiBusy: StateFlow<Boolean> = _isAiBusy.asStateFlow()

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
                // 3. получаем полный ответ от репозитория
                val ai = repository.sendTextMessage(chatId.toInt(), question)

                // 4. “печатаем” его внутрь плейсхолдера
                streamAiMessageInto(placeholderIndex, ai)

                // при желании можно после этого дернуть loadMessages() для sync с БД
                // loadMessages()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ========== ОДНА КАРТИНКА (с текстом или без) ==========
    fun sendImageMessage(base64Image: String, prompt: String?) {
        if (chatId == -1L) return

        viewModelScope.launch {

            // пузырь пользователя
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

            // плейсхолдер ассистента
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
                val ai = repository.sendImageMessage(
                    chatId = chatId.toInt(),
                    base64Image = base64Image,
                    prompt = prompt
                )

                streamAiMessageInto(placeholderIndex, ai)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ========== НЕСКОЛЬКО КАРТИНОК (с текстом или без) ==========
    fun sendImagesMessage(images: List<String>, prompt: String?) {
        if (chatId == -1L || images.isEmpty()) return

        viewModelScope.launch {

            // пузырь пользователя
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

            // плейсхолдер ассистента
            //Плейсхолдер — это временное “пустое” сообщение ассистента, которое ты вставляешь в список сообщений до того, как пришёл настоящий ответ.
            //
            //То есть ты заранее добавляешь пузырёк бота, чтобы потом постепенно записывать в него текст (стриминг).
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
                val ai = repository.sendImagesMessage(
                    chatId = chatId.toInt(),
                    images = images,
                    prompt = prompt
                )

                streamAiMessageInto(placeholderIndex, ai)

            } catch (e: Exception) {
                e.printStackTrace()
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





    //постепенно дописываем текст ответа в уже существующий плейсхолдер ассистента (по индексу).
    private suspend fun streamAiMessageInto(index: Int, full: MessageModel) {
        val fullText = full.text.orEmpty()
        if (fullText.isEmpty()) return

        val chunkSize = 4     // сколько символов добавляем за шаг
        val delayMs = 55L     // пауза между шагами (мс)

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
