package com.emotionalintelligence.emova.util

import com.emotionalintelligence.emova.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

/**
 * Talks to OUR Cloudflare Worker, which holds the Gemini API key and calls Gemini
 * (see /worker/gemini-proxy.js). The app never sees the key.
 *
 * Contract with the Worker:
 *   POST  { "system": "...", "prompt": "...", "lang": "es" }
 *   200   { "text": "..." }
 *   other { "error": "..." }
 */
object GeminiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // ⚠️ Replace with your Worker URL if you create a new one.
    private const val URL = "https://icy-sun-bd17.walkyrialopez.workers.dev"

    suspend fun generate(
        systemPrompt: String,
        userPrompt: String,
        language: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val body = JSONObject().apply {
            put("system", systemPrompt)
            put("prompt", userPrompt)
            put("lang", language)
        }

        val request = Request.Builder()
            .url(URL)
            .apply { if (BuildConfig.APP_TOKEN.isNotBlank()) header("X-App-Token", BuildConfig.APP_TOKEN) }
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val raw = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("API error ${response.code}"))
                }
                val text = JSONObject(raw).optString("text", "").trim()
                if (text.isEmpty()) Result.failure(Exception("EMPTY_RESPONSE")) else Result.success(text)
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
