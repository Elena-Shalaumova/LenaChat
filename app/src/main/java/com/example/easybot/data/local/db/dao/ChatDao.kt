package com.example.easybot.data.local.db.dao

import androidx.room.*
import com.example.easybot.data.local.db.entity.ChatEntity
import com.example.easybot.data.local.db.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    // -------- ЧАТЫ --------

    // Список всех чатов (для ChatRepository.getChats)
    @Query("SELECT * FROM chats WHERE userId = :userId ORDER BY id DESC")
    fun getChats(userId: Long): Flow<List<ChatEntity>>

    // Вставка/обновление чата (для insertChat в репозитории)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity): Long

    // Удалить один чат
    @Query("DELETE FROM chats WHERE id = :chatId")
    suspend fun deleteChat(chatId: Long)

    // Получить один чат по id (если где-то надо)
    @Query("SELECT * FROM chats WHERE id = :chatId LIMIT 1")
    suspend fun getChat(chatId: Long): ChatEntity?

    // -------- СООБЩЕНИЯ --------

    // Сообщения конкретного чата
    @Query("SELECT * FROM chat_messages WHERE chatId = :chatId ORDER BY createdAt ASC")
    fun getMessages(chatId: Long): Flow<List<MessageEntity>>

    // Вставить сообщение
    @Insert
    suspend fun insertMessage(message: MessageEntity): Long

    // Очистить все сообщения в чате
    @Query("DELETE FROM chat_messages WHERE chatId = :chatId")
    suspend fun clearChat(chatId: Long)

    // Удалить чат вместе с его сообщениями
    @Transaction
    suspend fun deleteChatWithMessages(chatId: Long) {
        clearChat(chatId)
        deleteChat(chatId)
    }
}