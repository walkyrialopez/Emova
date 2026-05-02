package com.emotionalintelligence.emova.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.emotionalintelligence.emova.R
import com.emotionalintelligence.emova.data.EmováDatabase
import com.emotionalintelligence.emova.model.EmotionFamily
import com.emotionalintelligence.emova.model.JournalEntry
import com.emotionalintelligence.emova.model.HabitRecord
import com.emotionalintelligence.emova.util.GeminiService
import com.emotionalintelligence.emova.util.loadLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// 🎯 1. Definimos los pasos de la pantalla
enum class JournalStep {
    BRAIN_DUMP,      // Pantalla 1: El usuario escribe su pensamiento
    AI_CHAT,         // Pantalla 2: La IA responde (Muestra loading y luego el texto)
    INTENSITY_CHECK, // Pantalla 3: El slider final
    COMPLETE         // Terminado, cerrar pantalla
}

data class JournalUiState(
    val checkInId: Int = 0,
    val emotionName: String = "",
    val emotionEmoji: String = "",
    val emotionFamily: String = "",
    val initialIntensity: Int = 0, // Necesitamos saber con qué intensidad empezó

    val currentStep: JournalStep = JournalStep.BRAIN_DUMP,

    val brainDump: String = "",

    val aiResponse: String = "",
    val aiError: String = "",
    val isLoadingAiResponse: Boolean = false,

    val finalIntensity: Int = 0
)

class JournalViewModel(application: Application) : AndroidViewModel(application) {

    private val db = EmováDatabase.getInstance(application)
    private val journalDao = db.journalEntryDao()
    private val habitDao = db.habitRecordDao() // 🎯 Para registrar el hábito

    private val _uiState = MutableStateFlow(JournalUiState())
    val uiState: StateFlow<JournalUiState> = _uiState.asStateFlow()

    fun initialize(
        checkInId: Int,
        emotionName: String,
        emotionEmoji: String,
        emotionFamilyName: String,
        initialIntensity: Int // 🎯 Asegúrate de pasar esto desde la pantalla anterior
    ) {
        _uiState.value = JournalUiState(
            checkInId = checkInId,
            emotionName = emotionName,
            emotionEmoji = emotionEmoji,
            emotionFamily = emotionFamilyName,
            initialIntensity = initialIntensity,
            currentStep = JournalStep.BRAIN_DUMP // Empezamos siempre aquí
        )
    }

    // ─── PASO 1: EL USUARIO ENVÍA SU BRAIN DUMP ─────────────────────────────
    fun submitBrainDump(text: String) {
        _uiState.value = _uiState.value.copy(
            brainDump = text,
            currentStep = JournalStep.AI_CHAT, // Pasamos a la pantalla de chat
            isLoadingAiResponse = true
        )
        fetchAiResponse(text)
    }

    // ─── PASO 2: LLAMADA A LA IA CON EL PROMPT BLINDADO Y BILINGÜE ─────────
    private fun fetchAiResponse(brainDumpText: String) {
        val state = _uiState.value
        viewModelScope.launch {
            val language = loadLanguage(getApplication()).code

            // 🎯 Construimos el Prompt usando el archivo strings.xml (Bilingüe)
            val systemPrompt = getApplication<Application>().getString(
                R.string.ai_system_prompt_difficult,
                state.emotionName,
                state.initialIntensity,
                if (brainDumpText.isBlank()) "No quiso escribir nada inicial." else brainDumpText
            )

            val result = GeminiService.getJournalResponse(
                systemPrompt = systemPrompt,
                language = language
            )

            result.fold(
                onSuccess = { text ->
                    _uiState.value = _uiState.value.copy(
                        aiResponse = text,
                        aiError = "",
                        isLoadingAiResponse = false
                    )
                },
                onFailure = { error ->
                    val errorMsg = if (language == "es") {
                        "No pudimos conectar con el guía en este momento. Intenta revisar tu conexión."
                    } else {
                        "We couldn't connect with the guide right now. Please check your connection."
                    }
                    _uiState.value = _uiState.value.copy(
                        aiError = errorMsg,
                        isLoadingAiResponse = false
                    )
                }
            )
        }
    }

    // ─── TRANSICIÓN AL PASO 3 ──────────────────────────────────────────────
    fun proceedToIntensityCheck() {
        _uiState.value = _uiState.value.copy(currentStep = JournalStep.INTENSITY_CHECK)
    }

    // ─── PASO 3: GUARDAR TODO Y FINALIZAR ──────────────────────────────────
    fun finishAndSave(finalIntensity: Int) {
        val state = _uiState.value
        _uiState.value = state.copy(finalIntensity = finalIntensity, currentStep = JournalStep.COMPLETE)

        viewModelScope.launch {
            // 1. Guardamos la conversación entera (formateada en texto simple)
            val conversationRecord = """
                Usuario: ${state.brainDump.ifBlank { "[Sin registro inicial]" }}
                Guía IA: ${state.aiResponse}
            """.trimIndent()

            journalDao.insert(
                JournalEntry(
                    checkInId = state.checkInId,
                    emotionId = state.emotionFamily.lowercase(),
                    emotionName = state.emotionName,
                    emotionFamily = state.emotionFamily,
                    initialBrainDump = state.brainDump,
                    conversationHistory = conversationRecord
                )
            )

            // 2. 🌟 MAGIA: Si la intensidad bajó, registramos el hábito de "Reflexión"
            if (finalIntensity < state.initialIntensity) {
                habitDao.insert(
                    HabitRecord(habitId = "reflection") // Esto alimentará la tarjeta héroe
                )
            }
        }
    }
}