package com.emotionalintelligence.emova.util

import com.emotionalintelligence.emova.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.UnknownHostException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

object GeminiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // 🎯 Mantienes el modelo Groq Llama 3 intacto
    private const val URL   = "https://api.groq.com/openai/v1/chat/completions"
    private const val MODEL = "llama-3.3-70b-versatile"

    // ─── Lógica Principal (Ahora mucho más limpia) ─────────────────────────────

    suspend fun getJournalResponse(
        systemPrompt: String,         // 🎯 Recibe el prompt ya ensamblado y traducido
        language: String = "en"
    ): Result<String> = withContext(Dispatchers.IO) {

        val jsonBody = JSONObject().apply {
            put("model", MODEL)
            put("max_tokens", 300)
            put("temperature", 0.85) // Mantiene la calidez y creatividad que tenías
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemPrompt) // Inyecta las reglas blindadas
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", if (language == "es") "Por favor, respóndeme." else "Please respond.")
                })
            })
        }

        val request = Request.Builder()
            .url(URL)
            .addHeader("Authorization", "Bearer ${BuildConfig.GROQ_API_KEY}")
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        return@withContext try {
            val response = client.newCall(request).execute()
            val rawBody  = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Result.failure(Exception("API error ${response.code}"))
            } else {
                // Parseo Seguro (Evita crashes si Groq falla en el formato)
                val jsonResponse = JSONObject(rawBody)
                val messageContent = jsonResponse.optJSONArray("choices")
                    ?.optJSONObject(0)
                    ?.optJSONObject("message")
                    ?.optString("content", "")?.trim()

                if (!messageContent.isNullOrEmpty()) {
                    Result.success(messageContent)
                } else {
                    Result.failure(Exception("EMPTY_RESPONSE"))
                }
            }
        } catch (e: UnknownHostException) {
            Result.failure(Exception("NO_CONNECTION"))
        } catch (e: SocketTimeoutException) {
            Result.failure(Exception("TIMEOUT"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}