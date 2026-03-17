package com.emotionalintelligence.emova

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.emotionalintelligence.emova.notifications.createNotificationChannel
import com.emotionalintelligence.emova.ui.navigation.EmováNavGraph
import com.emotionalintelligence.emova.ui.theme.EmotionalIntelligenceTheme
import com.emotionalintelligence.emova.util.createLocalizedContext
import com.emotionalintelligence.emova.util.loadLanguage

class MainActivity : ComponentActivity() {

    // Called before onCreate — the right place to apply locale reliably
    override fun attachBaseContext(newBase: Context) {
        val language = loadLanguage(newBase)
        super.attachBaseContext(newBase.createLocalizedContext(language))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        createNotificationChannel(this)
        enableEdgeToEdge()
        setContent {
            EmotionalIntelligenceTheme {
                EmováNavGraph()
            }
        }
    }
}