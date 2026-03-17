package com.emotionalintelligence.emova.model

import androidx.compose.ui.graphics.Color
import com.emotionalintelligence.emova.R

enum class EmotionFamily(val label: String, val color: Color, val labelResId: Int) {
    POSITIVE ("Positive",  Color(0xFFE8A020), R.string.family_positive),
    DIFFICULT("Difficult", Color(0xFF4CC9F0), R.string.family_difficult),
    ACTIVATED("Activated", Color(0xFFEF233C), R.string.family_activated)
}