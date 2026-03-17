package com.emotionalintelligence.emova.viewmodel

import androidx.lifecycle.ViewModel
import com.emotionalintelligence.emova.ui.conflict.conflictSteps
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ConflictUiState(
    val started: Boolean = false,
    val currentStep: Int = 0,
    val currentAnswer: String = "",
    val answers: List<String> = List(5) { "" },
    val isComplete: Boolean = false
)

class ConflictViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ConflictUiState())
    val uiState: StateFlow<ConflictUiState> = _uiState.asStateFlow()

    fun start() {
        _uiState.value = ConflictUiState(started = true)
    }

    fun setAnswer(text: String) {
        _uiState.value = _uiState.value.copy(currentAnswer = text)
    }

    fun nextStep() {
        val state = _uiState.value
        val updatedAnswers = state.answers.toMutableList()
        updatedAnswers[state.currentStep] = state.currentAnswer

        if (state.currentStep < conflictSteps.size - 1) {
            _uiState.value = state.copy(
                currentStep = state.currentStep + 1,
                currentAnswer = updatedAnswers[state.currentStep + 1],
                answers = updatedAnswers
            )
        } else {
            _uiState.value = state.copy(
                answers = updatedAnswers,
                isComplete = true
            )
        }
    }

    fun prevStep() {
        val state = _uiState.value
        val updatedAnswers = state.answers.toMutableList()
        updatedAnswers[state.currentStep] = state.currentAnswer
        if (state.currentStep > 0) {
            _uiState.value = state.copy(
                currentStep = state.currentStep - 1,
                currentAnswer = updatedAnswers[state.currentStep - 1],
                answers = updatedAnswers
            )
        }
    }

    fun skipStep() {
        val state = _uiState.value
        val updatedAnswers = state.answers.toMutableList()
        updatedAnswers[state.currentStep] = ""

        if (state.currentStep < conflictSteps.size - 1) {
            _uiState.value = state.copy(
                currentStep = state.currentStep + 1,
                currentAnswer = updatedAnswers[state.currentStep + 1],
                answers = updatedAnswers
            )
        } else {
            _uiState.value = state.copy(answers = updatedAnswers, isComplete = true)
        }
    }

    fun restart() {
        _uiState.value = ConflictUiState()
    }
}