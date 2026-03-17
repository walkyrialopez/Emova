package com.emotionalintelligence.emova.util

import com.google.ai.client.generativeai.GenerativeModel
import com.emotionalintelligence.emova.BuildConfig

object GeminiService {

    private val model = GenerativeModel(
        modelName = "gemini-3-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    suspend fun getJournalResponse(
        emotionName: String,
        emotionFamily: String,
        prompts: List<String>,
        answers: List<String>
    ): String {
        val journalContent = prompts.zip(answers)
            .filter { (_, answer) -> answer.isNotBlank() }
            .joinToString("\n") { (prompt, answer) -> "Q: $prompt\nA: $answer" }

        val prompt = """
            You are a warm, empathetic emotional intelligence companion. 
            A user just completed a journal entry after feeling $emotionName 
            (emotion family: $emotionFamily).
            
            Their journal entries:
            $journalContent
            
            Respond with exactly 3 short sentences:
            1. Acknowledge what they shared with genuine empathy (no clichés)
            2. An 'Invisible Connection': Explain how this emotion might be linked to their physical body or a specific habit (like exercise or sleep).
            3. A practical tip for emotional intelligence.
            
            Keep it warm, human, and concise. Do not use bullet points or numbers.
            Do not start with "I" or "It sounds like."
        """.trimIndent()

        return try {
            val response = model.generateContent(prompt)
            response.text ?: "Thank you for sharing that. Take a moment to sit with what you've written."
        } catch (e: Exception) {
            "Emova is resting. (Error: ${e.localizedMessage})"
        }
    }
}