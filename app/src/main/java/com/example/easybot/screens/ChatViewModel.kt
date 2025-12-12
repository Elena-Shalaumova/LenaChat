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
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.example.easybot.ChatDto

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChatRepository()

    private var chatId: Long = -1L

    private val _messages = MutableStateFlow<List<MessageModel>>(emptyList())
    val messages: StateFlow<List<MessageModel>> = _messages.asStateFlow()

    private val _isAiBusy = MutableStateFlow(false)
    val isAiBusy: StateFlow<Boolean> = _isAiBusy.asStateFlow()

    var isIncognito: Boolean = false
        private set

    var isExporting = false
        private set

    // 👉 текущая корутина запроса к API
    private var currentJob: Job? = null

    // инициализация при заходе в чат
    fun init(chatId: Long, isIncognito: Boolean) {
        if (this.chatId == chatId && this.isIncognito == isIncognito) return

        this.chatId = chatId
        this.isIncognito = isIncognito

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

    // ========== ОСТАНОВИТЬ ТЕКУЩЕЕ ГЕНЕРАЦИЮ ==========
    fun stopGeneration() {
        // отменяем корутину с запросом
        currentJob?.cancel()
    }

    // ========== ТЕКСТОВОЕ СООБЩЕНИЕ ==========
    fun sendMessage(question: String) {
        if (chatId == -1L || question.isBlank()) return
        if (_isAiBusy.value) return

        currentJob = viewModelScope.launch {
            _isAiBusy.value = true

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

            // 2. плейсхолдер ассистента
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
                    repository.sendTextMessage(chatId.toInt(), question)
                }

                if (ai == null) {
                    setErrorToPlaceholder(
                        placeholderIndex,
                        "Ответ занял больше 3 минут, запрос отменён."
                    )
                } else {
                    streamAiMessageInto(placeholderIndex, ai)
                }
            } catch (e: CancellationException) {
                // 👇 пользователь нажал "Стоп" — не считаем это ошибкой
                setErrorToPlaceholder(placeholderIndex, "Генерация остановлена.")
            } catch (e: Exception) {
                e.printStackTrace()
                setErrorToPlaceholder(
                    placeholderIndex,
                    "Произошла ошибка при получении ответа."
                )
            } finally {
                _isAiBusy.value = false
                currentJob = null
            }
        }
    }

    // ========== ОДНА КАРТИНКА ==========
    fun sendImageMessage(base64Image: String, prompt: String?) {
        if (chatId == -1L) return
        if (_isAiBusy.value) return

        currentJob = viewModelScope.launch {
            _isAiBusy.value = true

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
            } catch (e: CancellationException) {
                setErrorToPlaceholder(placeholderIndex, "Генерация остановлена.")
            } catch (e: Exception) {
                e.printStackTrace()
                setErrorToPlaceholder(
                    placeholderIndex,
                    "Произошла ошибка при получении ответа."
                )
            } finally {
                _isAiBusy.value = false
                currentJob = null
            }
        }
    }

    // ========== НЕСКОЛЬКО КАРТИНОК ==========
    fun sendImagesMessage(images: List<String>, prompt: String?) {
        if (chatId == -1L || images.isEmpty()) return
        if (_isAiBusy.value) return

        currentJob = viewModelScope.launch {
            _isAiBusy.value = true

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
            } catch (e: CancellationException) {
                setErrorToPlaceholder(placeholderIndex, "Генерация остановлена.")
            } catch (e: Exception) {
                e.printStackTrace()
                setErrorToPlaceholder(
                    placeholderIndex,
                    "Произошла ошибка при получении ответа."
                )
            } finally {
                _isAiBusy.value = false
                currentJob = null
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

    fun exportAllChats(context: Context, onResult: (success: Boolean) -> Unit = {}) {
        if (isExporting) return

        isExporting = true
        viewModelScope.launch {
            try {
                repository.exportAndSaveUserData(context)

                // Удача
                Toast.makeText(
                    context,
                    "Экспорт выполнен — смотрите user_export.json",
                    Toast.LENGTH_LONG
                ).show()

                onResult(true)

                android.util.Log.d("EXPORT_DEBUG", "Экспорт успешно завершён!")

            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Ошибка экспорта: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()

                onResult(false)

                android.util.Log.e("EXPORT_DEBUG", "Ошибка экспорта", e)

            } finally {
                isExporting = false
            }
        }
    }





    fun exportCurrentChat(context: Context, chat: ChatDto) {
        viewModelScope.launch {
            repository.exportSingleChatToJson(context, chat)
        }
    }







    private fun setErrorToPlaceholder(index: Int, errorText: String) {
        val updated = _messages.value.toMutableList()
        val old = updated.getOrNull(index) ?: return
        updated[index] = old.copy(text = errorText)
        _messages.value = updated
    }

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
