package com.example.easybot.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.easybot.App
import com.example.easybot.data.repo.ChatRepository

class ChatListVmFactory(private val repo: ChatRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatListViewModel(repo) as T
    }
}

class ChatVmFactory(
    private val repo: ChatRepository,
   // private val chatId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
       // return ChatViewModel(repo, chatId) as T
        return ChatListViewModel(repo) as T
    }
}

//fun App.repo(): ChatRepository = this.chatRepo
