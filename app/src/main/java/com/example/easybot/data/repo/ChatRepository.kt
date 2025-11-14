package com.example.easybot.data.repo

//import androidx.paging.Pager
//import androidx.paging.PagingConfig
//import androidx.paging.PagingData
import com.example.easybot.data.local.AppDatabase
import com.example.easybot.data.local.entities.ChatEntity
import com.example.easybot.data.local.entities.MessageEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ChatRepository(
    private val db: AppDatabase
) {
    private val chats = db.chatDao()
    private val messages = db.messageDao()

    // список чатов
    fun observeChats(): Flow<List<ChatEntity>> = chats.observeChats()

    // НОВОЕ: поток сообщений конкретного чата (для ChatViewModel)
    fun observeMessages(chatId: String): Flow<List<MessageEntity>> =
        messages.observeMessages(chatId)

    // оставляем пагинацию "на будущее" – сейчас ChatViewModel её не использует,
    // но пусть лежит, не мешает
 //   fun messagesPager(chatId: String): Flow<PagingData<MessageEntity>> =
    //    Pager(
          //  config = PagingConfig(pageSize = 40, enablePlaceholders = false),
         //   pagingSourceFactory = { messages.pagingSource(chatId) }
    //    ).flow

    suspend fun createChat(title: String): String {
        val now = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()
        chats.upsert(ChatEntity(id = id, title = title, createdAt = now, updatedAt = now))
        return id
    }

    suspend fun deleteChat(chatId: String) {
        messages.deleteByChat(chatId)
        chats.delete(chatId)
    }

    suspend fun addUserMessage(chatId: String, text: String): String {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        messages.upsert(
            MessageEntity(
                id = id,
                chatId = chatId,
                role = "user",
                text = text,
                createdAt = now,
                isPendingSync = true
            )
        )
        bumpChat(chatId, now)
        return id
    }

    suspend fun addAssistantMessage(chatId: String, text: String) {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        messages.upsert(
            MessageEntity(
                id = id,
                chatId = chatId,
                role = "assistant", // или "model", главное — чтобы совпадало с UI
                text = text,
                createdAt = now
            )
        )
        bumpChat(chatId, now)
    }

    private suspend fun bumpChat(chatId: String, time: Long) {
        val current = db.chatDao().get(chatId) ?: return
        db.chatDao().upsert(current.copy(updatedAt = time))
    }
}
