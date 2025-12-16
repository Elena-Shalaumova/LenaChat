package com.example.easybot.data

import android.content.Context
import android.os.Environment
import android.util.Log
import com.example.easybot.*
import com.example.easybot.data.mappers.toModels
import com.example.easybot.dto.ChatDto
import com.example.easybot.dto.LastMessageDto
import com.example.easybot.theme.MessageModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File
import android.widget.Toast

//подключение к бэку
class ChatRepository(
    private val api: WebApiChatAI = provideApi(UserSession.apiBaseUrl),
) {
    private fun getUserId(): Int =
        UserSession.userId?.toInt() ?: error("User not logged in")

    // ---- Чаты ----
    suspend fun getChats(): List<ChatDto> {
        return api.getChats(getUserId())
    }

    suspend fun createChat(title: String, isIncognito: Boolean): ChatDto {
        val request = CreateChatRequest(title = title, userId = getUserId(),isIncognito = isIncognito)
        return api.createChat(request)
    }

    suspend fun deleteChat(chatId: Int) {
        api.deleteChat(chatId)
    }

    suspend fun clearChat(chatId: Int) {
        api.clearChat(chatId)
    }

    // настройки конкретного чата (из settings_chat)
    suspend fun getChatSettings(chatId: Int): ChatSettingsDto {
        return api.getChatSettings(chatId)
    }

    // последние сообщения по всем чатам пользователя
    suspend fun getLastMessagesForUser(): List<LastMessageDto> {
        return api.getLastMessagesForUser(getUserId())
    }

    // ---- Сообщения ----
    suspend fun getMessages(chatId: Int): List<MessageModel> {
        val dtos = api.getMessages(chatId)
        // временный лог
        dtos.forEach {
            Log.d("MSG_DEBUG", "id=${it.id} type=${it.type} imagesCount=${it.images.size}")
        }
        return dtos.toModels()
    }

    // ---------- ТЕКСТ ----------
    // возвращаем ответ ИИ как MessageModel
    suspend fun sendTextMessage(chatId: Int, text: String): MessageModel {
        val userId = getUserId()

        val request = SendMessageRequest(
            chatId = chatId,
            userId = userId,
            text = text,
            base64Images = emptyList()
        )

        val response = api.sendMessage(request)
        // response.aiMessage: MessageDto -> конвертим через toModels()
        return listOf(response.aiMessage).toModels().first()
    }

    // ---------- ОДНА КАРТИНКА (может быть + текст-подпись) ----------
    suspend fun sendImageMessage(
        chatId: Int,
        base64Image: String,
        prompt: String?
    ): MessageModel {
        val userId = getUserId()

        val request = SendMessageRequest(
            chatId = chatId,
            userId = userId,
            text = prompt,
            base64Images = listOf(base64Image)
        )

        val response = api.sendMessage(request)
        return listOf(response.aiMessage).toModels().first()
    }

    // ---------- НЕСКОЛЬКО КАРТИНОК (может быть + текст) ----------
    suspend fun sendImagesMessage(
        chatId: Int,
        images: List<String>,
        prompt: String?
    ): MessageModel {
        val userId = getUserId()

        val request = SendMessageRequest(
            chatId = chatId,
            userId = userId,
            text = prompt,
            base64Images = images
        )

        val response = api.sendMessage(request)
        return listOf(response.aiMessage).toModels().first()
    }
    // ============================================================
    //              NEW — FULL EXPORT + SAVE + MERGE
    // ============================================================

    /**
     * Выгружает ВСЕ чаты + сообщения пользователя,
     * сохраняет в файл user_export.json
     * и делает M ER G E:
     *
     * - старые данные НЕ удаляются
     * - новые добавляются
     * - пропавшие на сервере записи НЕ удаляются из файла
     */
    suspend fun exportAndSaveUserData(context: Context) {
        try {
            val userId = getUserId()

            // 1. получаем свежие данные с сервера
            val newData = api.exportUserData(userId)

            //val file = File(context.filesDir, "user_export.json")

            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "user_export.json"
            )

            // Настройка Moshi
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            val type = Types.newParameterizedType(
                List::class.java,
                ExportChatDto::class.java
            )

            val adapter = moshi.adapter<List<ExportChatDto>>(type)

            // 2. Если файла нет → создаём новый и записываем всё как есть
            if (!file.exists()) {
                file.writeText(adapter.toJson(newData))
                return
            }

            // 3. Файл есть → читаем старые данные
            val oldJson = file.readText()
            val oldData = adapter.fromJson(oldJson) ?: emptyList()

            // 4. МЕРДЖ
            val merged = mergeExports(oldData, newData)

            // 5. Сохраняем результат
            file.writeText(adapter.toJson(merged))

        } catch (e: Exception) {
            Log.e("EXPORT_ERROR", "Failed to export user data", e)
        }
    }

    /**
     * Экспорт ОДНОГО чата в отдельный JSON-файл.
     * В JSON попадает:
     *  - объект chat (ChatDto)
     *  - список messages (MessageDto)
     * Имя файла = название чата (очищенное) + ".json"
     */
    suspend fun exportSingleChatToJson(
        context: Context,
        chat: ChatDto
    ) {
        try {
            // 1. Получаем ВСЕ сообщения этого чата с бэка
            val messages: List<MessageDto> = api.getMessages(chat.id)

            // 2. Собираем объект для экспорта
            val payload = SingleChatExportDto(
                chat = chat,
                messages = messages
            )

            // 3. Делаем безопасное имя файла из названия чата
            val safeTitle = chat.title
                .replace(Regex("""[\\/:*?"<>|]"""), "_")  // запрещённые символы
                .ifBlank { "chat_${chat.id}" }           // на случай пустого названия

            val fileName = "$safeTitle.json"

            // 4. Папка — Downloads, чтобы было удобно забрать с ПК
            val dir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            )
            if (!dir.exists()) dir.mkdirs()

            val file = File(dir, fileName)

            // 5. Сохраняем JSON через Moshi
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            val adapter = moshi.adapter(SingleChatExportDto::class.java)

            file.writeText(adapter.toJson(payload))

            android.util.Log.d(
                "EXPORT_DEBUG",
                "Один чат экспортирован в ${file.absolutePath}"
            )

            Toast.makeText(
                context,
                "Чат «${chat.title}» сохранён в ${fileName}",
                Toast.LENGTH_LONG
            ).show()

        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e("EXPORT_DEBUG", "Ошибка экспорта одного чата", e)
            Toast.makeText(
                context,
                "Ошибка при экспорте чата",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    /**
     * MERGE алгоритм:
     * - старые чаты сохраняются
     * - новые чаты добавляются
     * - сообщения внутри чата объединяются без дубликатов
     */
    private fun mergeExports(
        oldList: List<ExportChatDto>,
        newList: List<ExportChatDto>
    ): List<ExportChatDto> {

        val result = oldList.toMutableList()

        newList.forEach { newChat ->
            val existing = result.find { it.chatId == newChat.chatId }

            if (existing == null) {
                // Чата не было — добавляем
                result.add(newChat)
            } else {
                // Чат есть → мерджим сообщения
                val oldMsgs = existing.messages.toMutableList()

                newChat.messages.forEach { msg ->
                    if (oldMsgs.none { it.messageId == msg.messageId }) {
                        oldMsgs.add(msg)
                    }
                }

                existing.messages = oldMsgs.sortedBy { it.createdAt }
            }
        }

        return result
    }
}


