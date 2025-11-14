//package com.example.easybot.data.local.dao
//
//import androidx.paging.PagingSource
//import androidx.room.*
//import com.example.easybot.data.local.entities.MessageEntity
//
//@Dao
//interface MessageDao {
//
//    // Пагинация истории для конкретного чата (снизу вверх)
//    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY createdAt DESC")
//    fun pagingSource(chatId: String): PagingSource<Int, MessageEntity>
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun upsert(msg: MessageEntity)
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun upsertAll(msgs: List<MessageEntity>)
//
//    @Query("DELETE FROM messages WHERE chatId = :chatId")
//    suspend fun deleteByChat(chatId: String)
//}
package com.example.easybot.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.easybot.data.local.entities.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    // Для ViewModel сейчас используем вот это:
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY createdAt")
    fun observeMessages(chatId: String): Flow<List<MessageEntity>>

    // Оставляем пагинацию "на вырост"
    //@Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY createdAt DESC")
    //fun pagingSource(chatId: String): PagingSource<Int, MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(msg: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(msgs: List<MessageEntity>)

    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun deleteByChat(chatId: String)
}
