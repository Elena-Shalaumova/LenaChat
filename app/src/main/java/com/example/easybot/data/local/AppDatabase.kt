package com.example.easybot.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.easybot.data.local.ChatDao

@Database(
    entities = [ChatEntity::class, MessageEntity::class],
    version = 3,
    exportSchema = false
)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: ChatDatabase? = null

        fun getInstance(context: Context): ChatDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    ChatDatabase::class.java,
                    "easybot.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }

}
