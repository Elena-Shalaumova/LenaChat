package com.example.easybot.core.session

object UserSession {
    var userId: Long? = null
    var login: String? = null


    //var apiBaseUrl: String = "http://10.16.69.80:5167/"
    var apiBaseUrl: String = "http://10.16.70.23:5167/"

    // текущая выбранная модель
    var streamEnabled: Boolean? = null
    var selectedModel: String? = null
    var temperature: Double? = null
    var maxTokens: Int? = null

}