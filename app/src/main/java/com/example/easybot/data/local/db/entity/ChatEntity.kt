package com.example.easybot.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,   // <-- новый столбец
    val title: String,
    val createdAt: Long = System.currentTimeMillis()
)
