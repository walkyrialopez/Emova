package com.emotionalintelligence.emova.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.emotionalintelligence.emova.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LessonUiState(
    val todaysLesson: Lesson? = null,
    val allLessons: List<Lesson> = emptyList(),
    val selectedLesson: Lesson? = null,
    // Scenario state
    val selectedOptionIndex: Int = -1,
    val showFeedback: Boolean = false,
    // Reflection state
    val reflectionAnswer: String = "",
    val reflectionSaved: Boolean = false,
    // Breathing state
    val breathingStep: Int = 0,
    val breathingActive: Boolean = false,
    val breathingSecondsLeft: Int = 0,
    // Gratitude state
    val gratitudeAnswers: List<String> = emptyList(),
    val currentGratitudeIndex: Int = 0,
    val gratitudeDone: Boolean = false
)

class LessonViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(LessonUiState())
    val uiState: StateFlow<LessonUiState> = _uiState.asStateFlow()

    init {
        // 🎯 APLICADO: Ahora llamamos directamente a lessonLibrary
        // (ya no usamos localizedLessonLibrary porque los IDs son universales)
        val dayIndex = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
        val todaysLesson = lessonLibrary[dayIndex % lessonLibrary.size]

        _uiState.value = LessonUiState(
            todaysLesson = todaysLesson,
            allLessons = lessonLibrary,
            selectedLesson = todaysLesson,
            // 🎯 APLICADO: Ajustamos para leer promptResIds en lugar de prompts
            gratitudeAnswers = if (todaysLesson.content is LessonContent.GuidedJournal)
                MutableList((todaysLesson.content as LessonContent.GuidedJournal).promptResIds.size) { "" }
            else emptyList()
        )
    }

    fun selectLesson(lesson: Lesson) {
        _uiState.value = _uiState.value.copy(
            selectedLesson = lesson,
            selectedOptionIndex = -1,
            showFeedback = false,
            reflectionAnswer = "",
            reflectionSaved = false,
            breathingStep = 0,
            breathingActive = false,
            currentGratitudeIndex = 0,
            gratitudeDone = false,
            // 🎯 APLICADO: Ajustamos para leer promptResIds
            gratitudeAnswers = if (lesson.content is LessonContent.GuidedJournal)
                MutableList((lesson.content as LessonContent.GuidedJournal).promptResIds.size) { "" }
            else emptyList()
        )
    }

    // ── Scenario ──────────────────────────────────────────────────────────────
    fun selectOption(index: Int) {
        _uiState.value = _uiState.value.copy(
            selectedOptionIndex = index,
            showFeedback = true
        )
    }

    // ── Reflection ────────────────────────────────────────────────────────────
    fun setReflectionAnswer(text: String) {
        _uiState.value = _uiState.value.copy(reflectionAnswer = text)
    }

    fun saveReflection() {
        _uiState.value = _uiState.value.copy(reflectionSaved = true)
    }

    // ── Breathing ─────────────────────────────────────────────────────────────
    fun startBreathing() {
        val lesson = _uiState.value.selectedLesson ?: return
        val content = lesson.content as? LessonContent.Breathing ?: return
        _uiState.value = _uiState.value.copy(
            breathingActive = true,
            breathingStep = 0,
            breathingSecondsLeft = content.steps[0].durationSeconds
        )
        runBreathingTimer()
    }

    private fun runBreathingTimer() {
        viewModelScope.launch {
            val lesson = _uiState.value.selectedLesson ?: return@launch
            val steps = (lesson.content as? LessonContent.Breathing)?.steps ?: return@launch
            repeat(4) { // 4 rounds
                steps.forEachIndexed { stepIndex, step ->
                    _uiState.value = _uiState.value.copy(
                        breathingStep = stepIndex,
                        breathingSecondsLeft = step.durationSeconds
                    )
                    repeat(step.durationSeconds) {
                        kotlinx.coroutines.delay(1000)
                        val remaining = _uiState.value.breathingSecondsLeft - 1
                        _uiState.value = _uiState.value.copy(breathingSecondsLeft = remaining)
                    }
                }
            }
            _uiState.value = _uiState.value.copy(breathingActive = false)
        }
    }

    // ── Gratitude ─────────────────────────────────────────────────────────────
    fun setGratitudeAnswer(text: String) {
        val answers = _uiState.value.gratitudeAnswers.toMutableList()
        answers[_uiState.value.currentGratitudeIndex] = text
        _uiState.value = _uiState.value.copy(gratitudeAnswers = answers)
    }

    fun nextGratitude() {
        val state = _uiState.value
        val lesson = state.selectedLesson ?: return
        // 🎯 APLICADO: Ajustamos para leer promptResIds
        val prompts = (lesson.content as? LessonContent.GuidedJournal)?.promptResIds ?: return
        if (state.currentGratitudeIndex < prompts.size - 1) {
            _uiState.value = state.copy(currentGratitudeIndex = state.currentGratitudeIndex + 1)
        } else {
            _uiState.value = state.copy(gratitudeDone = true)
        }
    }
}