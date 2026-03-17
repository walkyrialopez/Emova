package com.emotionalintelligence.emova.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.emotionalintelligence.emova.data.EmováDatabase
import com.emotionalintelligence.emova.data.ExportHelper
import com.emotionalintelligence.emova.util.AppLanguage
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SettingsUiState(
    val notificationsEnabled: Boolean = false,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
    val language: AppLanguage = AppLanguage.ENGLISH
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("emova_prefs", Context.MODE_PRIVATE)
    private val db = EmováDatabase.getInstance(application)

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            notificationsEnabled = prefs.getBoolean("notif_enabled", false),
            reminderHour = prefs.getInt("reminder_hour", 20),
            reminderMinute = prefs.getInt("reminder_minute", 0),
            language = AppLanguage.fromCode(
                prefs.getString("app_language", AppLanguage.ENGLISH.code) ?: AppLanguage.ENGLISH.code
            )
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // Convenience flow so MainActivity can observe just the language
    val language: StateFlow<AppLanguage> = uiState.map { it.language }
        .stateIn(viewModelScope, SharingStarted.Eagerly, _uiState.value.language)

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("notif_enabled", enabled).apply()
        _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
    }

    fun setReminderTime(hour: Int, minute: Int) {
        prefs.edit().putInt("reminder_hour", hour).putInt("reminder_minute", minute).apply()
        _uiState.value = _uiState.value.copy(reminderHour = hour, reminderMinute = minute)
    }

    fun setLanguage(language: AppLanguage) {
        prefs.edit().putString("app_language", language.code).apply()
        _uiState.value = _uiState.value.copy(language = language)
    }

    fun exportData(context: Context) {
        viewModelScope.launch {
            val checkIns = db.checkInDao().getAllFlow().first()
            val journal = db.journalEntryDao().getAllFlow().first()
            ExportHelper.exportToCsv(context, checkIns, journal)
        }
    }
}