package com.emotionalintelligence.emova.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.emotionalintelligence.emova.data.EmováDatabase
import com.emotionalintelligence.emova.model.CheckIn
import com.emotionalintelligence.emova.model.Emotion
import com.emotionalintelligence.emova.model.emotionList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CheckInUiState(
    val selectedEmotion: Emotion? = null,
    val intensity: Int = 5,
    val note: String = "",
    val savedCheckInId: Int = -1,   // -1 = not saved yet
    val isLoading: Boolean = false
)

class CheckInViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = EmováDatabase.getInstance(application).checkInDao()

    private val _uiState = MutableStateFlow(CheckInUiState())
    val uiState: StateFlow<CheckInUiState> = _uiState.asStateFlow()

    val emotions = emotionList

    fun selectEmotion(emotion: Emotion) {
        _uiState.value = _uiState.value.copy(selectedEmotion = emotion, savedCheckInId = -1)
    }

    fun setIntensity(value: Int) {
        _uiState.value = _uiState.value.copy(intensity = value)
    }

    fun setNote(text: String) {
        _uiState.value = _uiState.value.copy(note = text)
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedEmotion = null)
    }

    fun saveCheckIn() {
        val state = _uiState.value
        val emotion = state.selectedEmotion ?: return

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true)
            val newId = dao.insert(
                CheckIn(
                    emotionId = emotion.id,
                    emotionName = emotion.name,
                    emotionFamily = emotion.family.name,
                    intensity = state.intensity,
                    note = state.note
                )
            )
            // Signal that save is done by setting the new ID
            _uiState.value = state.copy(
                isLoading = false,
                savedCheckInId = newId.toInt()
            )
        }
    }

    fun resetSaved() {
        _uiState.value = CheckInUiState()
    }
}