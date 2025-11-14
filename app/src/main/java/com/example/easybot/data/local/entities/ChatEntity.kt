package com.example.easybot.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val id: String,          // UUID
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isArchived: Boolean = false
)
