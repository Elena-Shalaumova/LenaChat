package com.example.easybot.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.easybot.data.local.dao.ChatDao
import com.example.easybot.data.local.dao.MessageDao
import com.example.easybot.data.local.entities.ChatEntity
import com.example.easybot.data.local.entities.MessageEntity

@Database(
    entities = [ChatEntity::class, MessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
}
