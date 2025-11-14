package com.example.easybot

import android.app.Application
import com.example.easybot.data.local.DbProvider
import com.example.easybot.data.repo.ChatRepository

class App : Application() {

    lateinit var chatRepo: ChatRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this                         // <-- сохраним singleton

        val db = DbProvider.get(this)
        chatRepo = ChatRepository(db)
    }

    companion object {
        lateinit var instance: App              // App.instance
            private set
    }
}
