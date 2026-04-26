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

    private const val URL   = "https://api.groq.com/openai/v1/chat/completions"
    private const val MODEL = "llama-3.3-70b-versatile"

    suspend fun getJournalResponse(
        emotionName: String,
        emotionFamily: String,
        prompts: List<String>,
        answers: List<String>,
        recentEmotions: List<String> = emptyList(),  // last 7 days
        language: String = "en"                       // "en" or "es"
    ): Result<String> = withContext(Dispatchers.IO) {

        val journalContent = prompts.zip(answers)
            .filter { (_, answer) -> answer.isNotBlank() }
            .joinToString("\n") { (p, a) -> "Q: $p\nA: $a" }

        val patternContext = if (recentEmotions.size >= 3) {
            val counts = recentEmotions.groupBy { it }
                .mapValues { it.value.size }
                .entries.sortedByDescending { it.value }
                .take(3)
                .joinToString(", ") { "${it.key} (${it.value}x)" }
            if (language == "es")
                "En los últimos días, el usuario ha sentido principalmente: $counts."
            else
                "Over the past days, the user has mainly felt: $counts."
        } else ""

        val systemPrompt = if (language == "es") """
            Eres Emova, una compañera de inteligencia emocional — cálida, perceptiva y honesta.
            No eres un chatbot genérico. Tu superpoder es conectar patrones que el usuario no ve.

            CONTEXTO:
            - Emoción actual: $emotionName (familia: $emotionFamily)
            - $patternContext
            - Entradas del diario:
            $journalContent

            TU RESPUESTA (3 frases, sin listas ni números):
            1. VALIDACIÓN: Reconoce lo que siente con empatía genuina y específica (no clichés como "es completamente normal").
            2. CONEXIÓN INVISIBLE: Si hay patrón en los últimos días, nómbralo. Si no, conecta la emoción con algo concreto del diario (el cuerpo, un hábito, una situación recurrente). Esta es la frase que ninguna otra app daría.
            3. PREGUNTA CURIOSA: Una pregunta abierta que invite a ver la situación desde un ángulo nuevo.

            Escribe en español. Sé directa, humana y breve. No uses "Entiendo que..." al inicio.
        """.trimIndent() else """
            You are Emova, an emotional intelligence companion — warm, perceptive, and honest.
            You are not a generic chatbot. Your superpower is connecting patterns the user can't see yet.

            CONTEXT:
            - Current emotion: $emotionName (family: $emotionFamily)
            - $patternContext
            - Journal entries:
            $journalContent

            YOUR RESPONSE (3 sentences, no lists or numbers):
            1. VALIDATION: Acknowledge what they feel with genuine, specific empathy (no clichés like "that's completely normal").
            2. INVISIBLE CONNECTION: If there's a pattern across recent days, name it. If not, connect this emotion to something concrete in their journal (body, habits, recurring situation). This is the sentence no other app would give.
            3. CURIOUS QUESTION: One open question that invites them to see the situation from a new angle.

            Write in English. Be direct, human, and brief. Don't start with "I understand that..."
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            put("model", MODEL)
            put("max_tokens", 300)
            put("temperature", 0.85)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemPrompt)
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content",
                        if (language == "es") "Por favor, respóndeme ahora."
                        else "Please respond now.")
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
                val text = JSONObject(rawBody)
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                    .trim()
                Result.success(text)
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