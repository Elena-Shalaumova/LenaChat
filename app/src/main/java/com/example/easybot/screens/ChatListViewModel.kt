package com.example.easybot.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easybot.data.local.entities.ChatEntity
import com.example.easybot.data.repo.ChatRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChatListUiState(
    val items: List<ChatEntity> = emptyList(),
    val isLoading: Boolean = false
)

class ChatListViewModel(private val repo: ChatRepository) : ViewModel() {

    val state: StateFlow<ChatListUiState> = repo.observeChats()
        .map { ChatListUiState(items = it) }
        .stateIn(viewModelScope, SharingStarted.Lazily, ChatListUiState())

    fun createChat(title: String) {
        viewModelScope.launch { repo.createChat(title) }
    }

    fun deleteChat(chatId: String) {
        viewModelScope.launch { repo.deleteChat(chatId) }
    }
}
