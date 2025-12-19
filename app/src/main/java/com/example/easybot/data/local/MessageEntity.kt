package com.example.easybot.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chatId: Long,
    val role: Int,  // 1 user, 0 assistant
    val type: String,            // "text" / "image"
    val text: String?,
    val imageBase64: String?,
    val createdAt: Long
    //val message: String,      // именно message, не text
    //val createdAt: Long = System.currentTimeMillis()       // System.currentTimeMillis() будем передавать снаружи
)
