package com.example.easybot

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

// --- DTO для настроек ---
data class SettingsDto(val id: Int, val userId: Int, val stream: Boolean, val model: String?, val temperature: Double?, val maxTokens: Int )
data class SettingsRequest(val id: Int, val stream: Boolean, val model: String,val temperature: Double?,val maxTokens: Int? )
data class OllamaVersionDto(val version: String)
data class OllamaModels(val models: List<String>)

// --- DTO для чатов и сообщений ---
data class ChatDto(val id: Int, val title: String)
data class MessageDto(val id: Int,
                      val chatId: Int,
                      val role: Int,
                      val type: String,
                      val text: String?,
                      val images: List<String>,
                      val createdAt: String? )


// --- DTO для запросов ---
data class CreateChatRequest(val title: String, val userId: Int)
data class SendMessageRequest(
    val chatId: Int,
    val userId: Int,
    val text: String?,        // может быть null для чистой картинки
    val base64Images: List<String>  // СПИСОК картинок в base64

)

// --- DTO для ответов ---
data class SendMessageResponse(val userMessage: MessageDto,
                               val aiMessage: MessageDto)

// --- DTO для авторизации ---
data class LoginReq(val login: String, val password: String)

// Для общения с нейросетью
data class ChatRequest(val message: String)
data class ChatResponse(val answer: String)

//DTO для отправки КАРТИНКИ
data class SendImageMessageRequest(
    val chatId: Int,
    val userId: Int,
    val prompt: String?,      // объект text для картинки
    val base64Image: String   // обязательный base64
)


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

    @POST("api/Chat")
    suspend fun createChat(@Body request: CreateChatRequest): ChatDto

    @DELETE("api/Chat/{chatId}")
    suspend fun deleteChat(@Path("chatId") chatId: Int): Response<Unit>

    @POST("api/Chat/{chatId}/clear")
    suspend fun clearChat(@Path("chatId") chatId: Int): Response<Unit>

    // --- Сообщения ---
    @GET("api/Chat/{chatId}/messages")
    suspend fun getMessages(@Path("chatId") chatId: Int): List<MessageDto>

    @POST("api/Chat/send")
    suspend fun sendMessage(
        @Body request: SendMessageRequest
    ): SendMessageResponse

    @POST("api/WebAPIChatAI/AddUser")
    suspend fun addUser(@Body user: RegisterDto): Response<UserDto>

    @POST("api/WebAPIChatAI/Login")
    suspend fun login(@Body req: LoginReq): Response<UserDto>
    
    // --- Ollama (если используется через сервер) ---
    @POST("api/Ai/chat")
    suspend fun chat(@Body request: ChatRequest): ChatResponse

   // @POST("api/Chat/send-image")
   // suspend fun sendImageMessage(@Body request: SendImageMessageRequest): SendMessageResponse
   //@GET("api/settings/{id}")
   //suspend fun getSettings(@Path("id") id: Int): SettingsDto


}


//фабрика
//fun provideApi(baseUrl: String = "http://192.168.3.8:5167/"): WebApiChatAI {
//    val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
//
//    val log = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
//    //val client = OkHttpClient.Builder().addInterceptor(log).build()
//    val client = OkHttpClient.Builder()
//        .addInterceptor(log)
//        .connectTimeout(60, TimeUnit.SECONDS)          // подключение к серверу
//        .writeTimeout(5, TimeUnit.MINUTES)             // загрузка текста/картинки
//        .readTimeout(0, TimeUnit.SECONDS)              // ❗ ждать бесконечно
//        .callTimeout(0, TimeUnit.MILLISECONDS)         // ❗ полный запрет глобального timeout
//        .retryOnConnectionFailure(true)                // авто-повтор при обрыве соединения
//        .build()
//
//    return Retrofit.Builder()
//        .baseUrl(baseUrl)
//        .addConverterFactory(MoshiConverterFactory.create(moshi))
//        .client(client)
//        .build()
//        .create(WebApiChatAI::class.java)
//}
//
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