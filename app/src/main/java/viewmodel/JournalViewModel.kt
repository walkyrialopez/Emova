package com.emotionalintelligence.emova.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.emotionalintelligence.emova.data.EmováDatabase
import com.emotionalintelligence.emova.model.EmotionFamily
import com.emotionalintelligence.emova.model.JournalEntry
import com.emotionalintelligence.emova.model.getPromptResIdsForFamily
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.emotionalintelligence.emova.util.GeminiService

data class JournalUiState(
    val emotionName: String = "",
    val emotionEmoji: String = "",
    val emotionFamily: String = "",
    val checkInId: Int = 0,
    val promptResIds: List<Int> = emptyList(),  // resource IDs — resolved in the UI
    val prompts: List<String> = emptyList(),     // resolved strings — used for DB saving only
    val currentPromptIndex: Int = 0,
    val answers: MutableList<String> = mutableListOf(),
    val currentAnswer: String = "",
    val isSaved: Boolean = false,
    val isComplete: Boolean = false,
    val aiResponse: String = "",
    val isLoadingAiResponse: Boolean = false
)

class JournalViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = EmováDatabase.getInstance(application).journalEntryDao()

    private val _uiState = MutableStateFlow(JournalUiState())
    val uiState: StateFlow<JournalUiState> = _uiState.asStateFlow()

    fun initialize(
        checkInId: Int,
        emotionName: String,
        emotionEmoji: String,
        emotionFamilyName: String
    ) {
        val family = try {
            EmotionFamily.valueOf(emotionFamilyName)
        } catch (e: Exception) {
            EmotionFamily.POSITIVE
        }
        val promptResIds = getPromptResIdsForFamily(family)
        // Resolve once for DB storage — UI will re-resolve via stringResource on each recompose
        val prompts = promptResIds.map { resId ->
            getApplication<Application>().getString(resId)
        }
        _uiState.value = JournalUiState(
            checkInId = checkInId,
            emotionName = emotionName,
            emotionEmoji = emotionEmoji,
            emotionFamily = emotionFamilyName,
            promptResIds = promptResIds,
            prompts = prompts,
            answers = MutableList(promptResIds.size) { "" }
        )
    }

    fun setAnswer(text: String) {
        _uiState.value = _uiState.value.copy(currentAnswer = text)
    }

    fun nextPrompt() {
        val state = _uiState.value
        val updatedAnswers = state.answers.toMutableList()
        updatedAnswers[state.currentPromptIndex] = state.currentAnswer

        if (state.currentPromptIndex < state.prompts.size - 1) {
            _uiState.value = state.copy(
                currentPromptIndex = state.currentPromptIndex + 1,
                currentAnswer = updatedAnswers[state.currentPromptIndex + 1],
                answers = updatedAnswers
            )
        } else {
            _uiState.value = state.copy(answers = updatedAnswers, isComplete = true)
            saveAll(updatedAnswers)
        }
    }

    fun prevPrompt() {
        val state = _uiState.value
        if (state.currentPromptIndex > 0) {
            val updatedAnswers = state.answers.toMutableList()
            updatedAnswers[state.currentPromptIndex] = state.currentAnswer
            _uiState.value = state.copy(
                currentPromptIndex = state.currentPromptIndex - 1,
                currentAnswer = updatedAnswers[state.currentPromptIndex - 1],
                answers = updatedAnswers
            )
        }
    }

    private fun saveAll(answers: List<String>) {
        val state = _uiState.value
        viewModelScope.launch {
            state.prompts.forEachIndexed { index, prompt ->
                val answer = answers.getOrElse(index) { "" }
                if (answer.isNotBlank()) {
                    dao.insert(
                        JournalEntry(
                            checkInId = state.checkInId,
                            emotionId = state.emotionFamily.lowercase(),
                            emotionName = state.emotionName,
                            emotionFamily = state.emotionFamily,
                            prompt = prompt,
                            answer = answer
                        )
                    )
                }
            }
            _uiState.value = _uiState.value.copy(isSaved = true, isLoadingAiResponse = true)
            fetchAiResponse(answers)
        }
    }

    // Add this new function:
    private fun fetchAiResponse(answers: List<String>) {
        val state = _uiState.value
        viewModelScope.launch {
            val response = GeminiService.getJournalResponse(
                emotionName = state.emotionName,
                emotionFamily = state.emotionFamily,
                prompts = state.prompts,
                answers = answers
            )
            _uiState.value = _uiState.value.copy(
                aiResponse = response,
                isLoadingAiResponse = false
            )
        }
    }

    fun skip() {
        nextPrompt()
    }
}