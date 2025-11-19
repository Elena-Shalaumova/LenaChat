package com.example.easybot.navigation

object Routes {
    const val Register = "register"
    const val SignUp = "signup"
    const val ChatList = "chat_list"
    // Добавляем параметр для названия чата
    const val Chat = "chat/{chatId}/{chatTitle}" 
    const val Settings = "settings"
    const val AdminPanel = "admin_panel"
}
