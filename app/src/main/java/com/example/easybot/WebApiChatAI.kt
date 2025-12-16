package com.example.easybot

import com.example.easybot.dto.ChatDto
import com.example.easybot.dto.ChatRequest
import com.example.easybot.dto.ChatResponse
import com.example.easybot.dto.ChatSettingsDto
import com.example.easybot.dto.ChatSettingsRequest
import com.example.easybot.dto.CreateChatRequest
import com.example.easybot.dto.LastMessageDto
import com.example.easybot.dto.LoginReq
import com.example.easybot.dto.MessageDto
import com.example.easybot.dto.OllamaVersionDto
import com.example.easybot.dto.RegistrationDto
import com.example.easybot.dto.RenameChatRequest
import com.example.easybot.dto.SendImageMessageRequest
import com.example.easybot.dto.SendMessageRequest
import com.example.easybot.dto.SendMessageResponse
import com.example.easybot.dto.SettingsDto
import com.example.easybot.dto.SettingsRequest
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Response
import retrofit2.http.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.collections.List
import com.example.easybot.UserSession
import com.example.easybot.ExportChatDto
import com.example.easybot.ExportMessageDto
//data class OllamaModels(val models: List<String>)

interface ApiService {
    @GET("api/Ai/ollama-version")
    suspend fun getOllamaVersion(): OllamaVersionDto

    @GET("api/Ai/models")
    suspend fun getModels(): List<String>
}

interface WebApiChatAI {

    // --- Настройки ---
    @GET("api/settings/{userId}")
    suspend fun getSettings(@Path("userId") userId: Int): SettingsDto

    @POST("api/settings/save")
    suspend fun saveSettings(@Body request: SettingsRequest): Response<Unit>

    @GET("api/Ai/models")
    suspend fun getAvailableModels(): List<String>

    @GET("api/Ai/ollama-version")
    suspend fun getOllamaVersion(): OllamaVersionDto

    // --- Админка ---
    @GET("api/admin/users")
    suspend fun getAllUsers(): List<UserDto>

    // --- Чаты ---
    @GET("api/Chat/user/{userId}/chats")
    suspend fun getChats(@Path("userId") userId: Int): List<ChatDto>

    @GET("api/WebAPIChatAI/export/user/{userId}")
    suspend fun exportUserData(
        @Path("userId") userId: Int
    ): List<ExportChatDto>


    @POST("api/Chat")
    suspend fun createChat(@Body request: CreateChatRequest): ChatDto

    @DELETE("api/Chat/{chatId}")
    suspend fun deleteChat(@Path("chatId") chatId: Int): Response<Unit>

    @POST("api/Chat/{chatId}/clear")
    suspend fun clearChat(@Path("chatId") chatId: Int): Response<Unit>

    // 🔹 Переименовать чат
    @PUT("api/Chat/{chatId}/rename")
    suspend fun renameChat(
        @Path("chatId") chatId: Int,
        @Body request: RenameChatRequest
    ): Response<Unit>

    // --- Сообщения ---
    @GET("api/Chat/{chatId}/messages")
    suspend fun getMessages(@Path("chatId") chatId: Int): List<MessageDto>

    @POST("api/Chat/send")
    suspend fun sendMessage(
        @Body request: SendMessageRequest
    ): SendMessageResponse

    @POST("api/WebAPIChatAI/AddUser")
    suspend fun addUser(@Body user: RegistrationDto): Response<UserDto>

    @POST("api/WebAPIChatAI/Login")
    suspend fun login(@Body req: LoginReq): Response<UserDto>

    // --- Ollama (если используется через сервер) ---
    @POST("api/Ai/chat")
    suspend fun chat(@Body request: ChatRequest): ChatResponse

    @GET("/api/Chat/getChatSettings/{chatId}")
    suspend fun getChatSettings(
        @Path("chatId") chatId: Int
    ): ChatSettingsDto

    @POST("/api/Chat/chat/saveChatSettings")
    //@POST("/api/Chat/saveChatSettings")
    suspend fun saveChatSettings(
        @Body request: ChatSettingsRequest
    ): Response<ChatSettingsDto>


    @GET("api/WebAPIChatAI/user/{userId}/last-messages")
    suspend fun getLastMessagesForUser(
        @Path("userId") userId: Int
    ): List<LastMessageDto>

    @GET("Chat/export/{userId}")
    suspend fun exportAllChats(@Path("userId") userId: Int): List<ExportChatDto>


}


//фабрика
fun provideApi(baseUrl: String): WebApiChatAI {
    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val log = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    val client = OkHttpClient.Builder()
        .addInterceptor(log)
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.MINUTES)
        .readTimeout(0, TimeUnit.SECONDS)
        .callTimeout(0, TimeUnit.MILLISECONDS)
        .retryOnConnectionFailure(true)
        .build()

    return Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(client)
        .build()
        .create(WebApiChatAI::class.java)
}

// Вместо фиксированного api делаем геттер,
// который всегда берет актуальный URL из UserSession
val api: WebApiChatAI
    get() = provideApi(UserSession.apiBaseUrl)