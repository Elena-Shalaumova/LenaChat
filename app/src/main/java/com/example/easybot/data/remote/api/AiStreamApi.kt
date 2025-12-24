package com.example.easybot.data.remote.api

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okio.BufferedSource
import org.json.JSONObject
import java.io.IOException

class AiStreamApi(
    private val baseUrl: String,
    private val okHttp: OkHttpClient = OkHttpClient.Builder().build()
) {
    private val jsonMedia = "application/json; charset=utf-8".toMediaType()

    fun chatStream(chatId: Long, message: String): Flow<String> = callbackFlow {
        val payload = JSONObject()
            .put("chatId", chatId)
            .put("message", message)
            .toString()

        val reqBody = payload.toRequestBody(jsonMedia)

        val request = Request.Builder()
            .url(baseUrl.trimEnd('/') + "/api/Ai/chat-stream")
            .post(reqBody)
            .header("Accept", "text/event-stream")
            .build()

        val call = okHttp.newCall(request)

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                close(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    close(IOException("HTTP ${response.code}: ${response.message}"))
                    return
                }

                val source: BufferedSource = response.body?.source()
                    ?: run {
                        close(IOException("Empty body"))
                        return
                    }

                try {
                    while (!source.exhausted()) {
                        val line = source.readUtf8Line() ?: continue
                        if (line.isBlank()) continue

                        if (line.startsWith("data:")) {

                            // ВАЖНО: никакого trim(), иначе теряем пробелы
                            var payload = line.removePrefix("data:")

                            // Обычно после "data:" есть один пробел — убираем только его
                            if (payload.startsWith(" ")) payload = payload.drop(1)

                            if (payload == "[DONE]") {
                                close()
                                return
                            }

                            // вернуть \n обратно, если ты экранируешь на сервере
                            payload = payload.replace("\\n", "\n")

                            // НЕ фильтруем payload по isBlank(), иначе потеряем пробелы
                            trySend(payload).isSuccess
                        }

                    }

                    close()
                } catch (t: Throwable) {
                    close(t)
                }
            }
        })

        awaitClose { call.cancel() }
    }
}
