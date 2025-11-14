package com.example.easybot

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easybot.data.repo.ChatRepository
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    // один "дефолтный" чат, потом сделаем список чатов
    private val chatId = "default_chat"

    // достаём репозиторий через App.instance
    private val repo: ChatRepository = App.instance.chatRepo

    // то, что уже было — список для UI
    val messageList by lazy {
        mutableStateListOf<MessageModel>()
    }

    // Gemini остаётся как был
    val generativeModel: GenerativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = Constants.apiKey
    )

    init {
        // при запуске VM загружаем историю из Room
        viewModelScope.launch {
            repo.observeMessages(chatId).collectLatest { entities ->
                messageList.clear()
                messageList.addAll(
                    entities.map { msg ->
                        MessageModel(
                            message = msg.text,
                            role = msg.role
                        )
                    }
                )
            }
        }
    }

    fun sendMessage(question: String) {
        viewModelScope.launch {
            try {
                // строим history из текущих сообщений (из БД)
                val chat = generativeModel.startChat(
                    history = messageList.map {
                        content(it.role) { text(it.message) }
                    }.toList()
                )

                // 1) сохраняем сообщение пользователя в БД
                repo.addUserMessage(chatId, question)
                // оно само появится в messageList через collectLatest

                // 2) временный "Typing..." только в UI (не в БД)
                messageList.add(MessageModel("Typing....", "model"))

                // 3) запрос к модели
                val response = chat.sendMessage(question)

                // убираем "Typing..."
                if (messageList.lastOrNull()?.message == "Typing....") {
                    messageList.removeLast()
                }

                val answerText = response.text.orEmpty()

                // 4) сохраняем ответ в БД
                repo.addAssistantMessage(chatId, answerText)
                // он тоже сам прилетит в messageList

            } catch (e: Exception) {
                Log.e("ChatViewModel", "sendMessage error", e)
                if (messageList.lastOrNull()?.message == "Typing....") {
                    messageList.removeLast()
                }
                messageList.add(
                    MessageModel("Error: " + (e.message ?: "unknown"), "model")
                )
            }
        }
    }
}
