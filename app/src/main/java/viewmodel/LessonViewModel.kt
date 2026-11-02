package com.emotionalintelligence.emova.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.emotionalintelligence.emova.R
import com.emotionalintelligence.emova.model.*
import com.emotionalintelligence.emova.util.LessonProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LessonUiState(
    val todaysLesson: Lesson? = null,
    val allLessons: List<Lesson> = emptyList(),
    val completedIds: Set<String> = emptySet(),
    /** True when every lesson is done: the screen then shows a daily practice prompt instead. */
    val allCompleted: Boolean = false,
    val practiceIndex: Int = 0,
    val practiceDone: Boolean = false,
    val practiceAnswer: String = "",
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

    private val progress = LessonProgress(application)

    init {
        val completed = progress.completedIds()
        val todayId = progress.todaysLessonId(lessonPath)
        val todaysLesson = lessonPath.firstOrNull { it.id == todayId }
        val practiceCount = application.resources.getStringArray(R.array.daily_practices).size

        _uiState.value = LessonUiState(
            todaysLesson = todaysLesson,
            allLessons = lessonPath,
            completedIds = completed,
            allCompleted = todaysLesson == null,
            practiceIndex = LessonProgress.practiceIndexForToday(practiceCount),
            practiceDone = progress.isPracticeDoneToday(),
            selectedLesson = todaysLesson,
            gratitudeAnswers = (todaysLesson?.content as? LessonContent.GuidedJournal)
                ?.let { MutableList(it.promptResIds.size) { "" } } ?: emptyList()
        )
    }

    /** Called when the person actually finishes the lesson they are viewing. */
    private fun completeSelected() {
        val lesson = _uiState.value.selectedLesson ?: return
        progress.markCompleted(lesson.id)
        _uiState.value = _uiState.value.copy(completedIds = progress.completedIds())
    }

    fun setPracticeAnswer(text: String) {
        _uiState.value = _uiState.value.copy(practiceAnswer = text)
    }

    fun finishPractice() {
        progress.markPracticeDone()
        _uiState.value = _uiState.value.copy(practiceDone = true)
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
        completeSelected()
    }

    // ── Reflection ────────────────────────────────────────────────────────────
    fun setReflectionAnswer(text: String) {
        _uiState.value = _uiState.value.copy(reflectionAnswer = text)
    }

    fun saveReflection() {
        _uiState.value = _uiState.value.copy(reflectionSaved = true)
        completeSelected()
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
            completeSelected()
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
            completeSelected()
        }
    }
}