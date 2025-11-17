package com.example.easybot

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

// Классы для общения с Ollama
data class ChatRequest(val message: String)
data class ChatResponse(val answer: String)

// Классы для авторизации (остаются без изменений)
data class LoginReq(val login: String, val password: String)

interface WebApiChatAI {
    // --- Новый метод для чата с Ollama ---
    @POST("api/Ai/chat") // <-- ИСПРАВЛЕНО
    suspend fun chat(@Body request: ChatRequest): ChatResponse

    // --- Старые методы для авторизации ---
    @POST("api/WebAPIChatAI/AddUser")
    suspend fun addUser(@Body user: RegisterDto): Response<UserDto>

    @POST("api/WebAPIChatAI/Login")
    suspend fun login(@Body req: LoginReq): Response<UserDto>
}

fun provideApi(baseUrl: String = "http://10.0.2.2:5167/"): WebApiChatAI {
    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val log = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
    val client = OkHttpClient.Builder().addInterceptor(log).build()

    return Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(client)
        .build()
        .create(WebApiChatAI::class.java)
}
