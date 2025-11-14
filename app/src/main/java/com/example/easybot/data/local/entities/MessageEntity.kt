package com.example.easybot.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    foreignKeys = [ForeignKey(
        entity = ChatEntity::class,
        parentColumns = ["id"],
        childColumns = ["chatId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("chatId"), Index("createdAt")]
)
data class MessageEntity(
    @PrimaryKey val id: String,          // UUID
    val chatId: String,
    val role: String,                    // "user" | "assistant" | "system"
    val text: String,
    val createdAt: Long,
    val remoteId: String? = null,
    val isPendingSync: Boolean = false
)
