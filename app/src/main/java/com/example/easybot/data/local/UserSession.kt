package com.example.easybot    // ВАЖНО: без .data.local, без других хвостов

object UserSession {
    var userId: Long? = null
    var login: String? = null

    var apiBaseUrl: String = "http://10.16.77.51:5167/"
    //var apiBaseUrl: String = "http://192.168.3.8:5167/"
    // текущая выбранная модель
    var selectedModel: String? = null
    var temperature: Double? = null
    var maxTokens: Int? = null
}