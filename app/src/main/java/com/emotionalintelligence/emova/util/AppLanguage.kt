package com.emotionalintelligence.emova.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

// ─── Supported languages ──────────────────────────────────────────────────────

enum class AppLanguage(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    SPANISH("es", "Español");

    companion object {
        fun fromCode(code: String) = entries.firstOrNull { it.code == code } ?: ENGLISH
    }
}

// ─── Creates a properly localized context (used in attachBaseContext) ─────────

fun Context.createLocalizedContext(language: AppLanguage): Context {
    val locale = Locale(language.code)
    Locale.setDefault(locale)
    val config = Configuration(resources.configuration)
    config.setLocale(locale)
    return createConfigurationContext(config)
}

// ─── Persist / load language preference ──────────────────────────────────────

private const val PREFS_NAME = "emova_prefs"
private const val KEY_LANGUAGE = "app_language"

fun saveLanguage(context: Context, language: AppLanguage) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit().putString(KEY_LANGUAGE, language.code).apply()
}

fun loadLanguage(context: Context): AppLanguage {
    val code = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(KEY_LANGUAGE, AppLanguage.ENGLISH.code) ?: AppLanguage.ENGLISH.code
    return AppLanguage.fromCode(code)
}