package com.example.easybot.data.local.dao

import androidx.room.*
import com.example.easybot.data.local.entities.ChatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chats ORDER BY updatedAt DESC")
    fun observeChats(): Flow<List<ChatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(chat: ChatEntity)

    @Query("DELETE FROM chats WHERE id = :chatId")
    suspend fun delete(chatId: String)

    @Query("SELECT * FROM chats WHERE id = :chatId LIMIT 1")
    suspend fun get(chatId: String): ChatEntity?
}
