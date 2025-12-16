package com.example.easybot.data.remote.api

import com.example.easybot.data.remote.dto.ChatDto
import com.example.easybot.data.remote.dto.ChatRequest
import com.example.easybot.data.remote.dto.ChatResponse
import com.example.easybot.data.remote.dto.ChatSettingsDto
import com.example.easybot.data.remote.dto.ChatSettingsRequest
import com.example.easybot.data.remote.dto.CreateChatRequest
import com.example.easybot.data.remote.dto.ExportChatDto
import com.example.easybot.data.remote.dto.LastMessageDto
import com.example.easybot.data.remote.dto.LoginReq
import com.example.easybot.data.remote.dto.MessageDto
import com.example.easybot.data.remote.dto.OllamaVersionDto
import com.example.easybot.data.remote.dto.RenameChatRequest
import com.example.easybot.data.remote.dto.SendImageMessageRequest
import com.example.easybot.data.remote.dto.SendMessageRequest
import com.example.easybot.data.remote.dto.SendMessageResponse
import com.example.easybot.data.remote.dto.SettingsDto
import com.example.easybot.data.remote.dto.SettingsRequest
import com.example.easybot.data.remote.dto.UserDto
import com.example.easybot.data.remote.dto.RegisterDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface EasyBotApi {

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
    suspend fun exportUserData(@Path("userId") userId: Int): List<ExportChatDto>

    @POST("api/Chat")
    suspend fun createChat(@Body request: CreateChatRequest): ChatDto

    @DELETE("api/Chat/{chatId}")
    suspend fun deleteChat(@Path("chatId") chatId: Int): Response<Unit>

    @POST("api/Chat/{chatId}/clear")
    suspend fun clearChat(@Path("chatId") chatId: Int): Response<Unit>

    @PUT("api/Chat/{chatId}/rename")
    suspend fun renameChat(
        @Path("chatId") chatId: Int,
        @Body request: RenameChatRequest
    ): Response<Unit>

    // --- Сообщения ---
    @GET("api/Chat/{chatId}/messages")
    suspend fun getMessages(@Path("chatId") chatId: Int): List<MessageDto>

    @POST("api/Chat/send")
    suspend fun sendMessage(@Body request: SendMessageRequest): SendMessageResponse

    @POST("api/WebAPIChatAI/AddUser")
    suspend fun addUser(@Body user: RegisterDto): Response<UserDto>

    @POST("api/WebAPIChatAI/Login")
    suspend fun login(@Body req: LoginReq): Response<UserDto>

    // --- Ollama ---
    @POST("api/Ai/chat")
    suspend fun chat(@Body request: ChatRequest): ChatResponse

    @GET("/api/Chat/getChatSettings/{chatId}")
    suspend fun getChatSettings(@Path("chatId") chatId: Int): ChatSettingsDto

    @POST("/api/Chat/chat/saveChatSettings")
    suspend fun saveChatSettings(@Body request: ChatSettingsRequest): Response<ChatSettingsDto>

    @GET("api/WebAPIChatAI/user/{userId}/last-messages")
    suspend fun getLastMessagesForUser(@Path("userId") userId: Int): List<LastMessageDto>

    @GET("Chat/export/{userId}")
    suspend fun exportAllChats(@Path("userId") userId: Int): List<ExportChatDto>
}

