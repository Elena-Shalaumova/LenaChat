package com.example.easybot.featurechat.model

data class ChatListItem(
    val chatId: Int,
    val title: String,
    val modelName: String?,
    val lastMessageText: String,
    val lastMessageTime: String?,
    val isIncognito: Boolean
)