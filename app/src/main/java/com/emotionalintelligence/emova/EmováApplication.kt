package com.emotionalintelligence.emova

import android.app.Application
import android.content.Context
import com.emotionalintelligence.emova.util.createLocalizedContext
import com.emotionalintelligence.emova.util.loadLanguage

class EmováApplication : Application() {

    override fun attachBaseContext(base: Context) {
        // Apply saved language to Application context so that
        // getApplication<Application>().getString() in ViewModels
        // always returns strings in the correct language.
        val language = loadLanguage(base)
        super.attachBaseContext(base.createLocalizedContext(language))
    }
}