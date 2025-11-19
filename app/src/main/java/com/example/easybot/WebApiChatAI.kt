package com.example.easybot

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Response
import retrofit2.http.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

// --- DTO для чатов и сообщений ---
data class ChatDto(val id: Int, val title: String)
// DTO теперь содержит и chatId, т.к. сервер его возвращает
data class MessageDto(val id: Int, val chatId: Int, val text: String, val role: Int)

// --- DTO для запросов ---
data class CreateChatRequest(val title: String, val userId: Int)
data class SendMessageRequest(val chatId: Int, val text: String)
data class SettingsRequest(val id: Int, val stream: Boolean)

// --- DTO для ответов ---
data class SendMessageResponse(val userMessage: MessageDto, val aiMessage: MessageDto)

// --- DTO для авторизации ---
data class LoginReq(val login: String, val password: String)

interface WebApiChatAI {

    // --- Чаты ---
    @GET("api/Chat/user/{userId}/chats")
    suspend fun getChats(@Path("userId") userId: Int): List<ChatDto>

    @POST("api/Chat")
    suspend fun createChat(@Body request: CreateChatRequest): ChatDto

    @DELETE("api/Chat/{chatId}")
    suspend fun deleteChat(@Path("chatId") chatId: Int): Response<Unit>

    @POST("api/Chat/{chatId}/clear")
    suspend fun clearChat(@Path("chatId") chatId: Int): Response<Unit>

    // --- Сообщения ---
    @GET("api/Chat/{chatId}/messages")
    suspend fun getMessages(@Path("chatId") chatId: Int): List<MessageDto>

    // Метод теперь возвращает SendMessageResponse
    @POST("api/Chat/send")
    suspend fun sendMessage(@Body body: SendMessageRequest): SendMessageResponse

    // --- Авторизация ---
    @POST("api/WebAPIChatAI/AddUser")
    suspend fun addUser(@Body user: RegisterDto): Response<UserDto>

    @POST("api/WebAPIChatAI/Login")
    suspend fun login(@Body req: LoginReq): Response<UserDto>

    // --- Настройки ---
    @POST("api/Settings")
    suspend fun saveSettings(@Body request: SettingsRequest): Response<Unit>
}

fun provideApi(baseUrl: String = "http://10.0.2.2:5167/"): WebApiChatAI {
    val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    val log = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
    val client = OkHttpClient.Builder().addInterceptor(log).build()

    return Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(client)
        .build()
        .create(WebApiChatAI::class.java)
}