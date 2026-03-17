package com.emotionalintelligence.emova.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary            = EmovaAccent,
    onPrimary          = Color.White,
    primaryContainer   = EmovaAccentLight,
    onPrimaryContainer = TextPrimary,
    background         = BackgroundLight,
    onBackground       = TextPrimary,
    surface            = SurfaceLight,
    onSurface          = TextPrimary,
    surfaceVariant     = SurfaceDimLight,
    onSurfaceVariant   = TextSecondary,
    outline            = SurfaceDimLight
)

@Composable
fun EmotionalIntelligenceTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography  = Typography,
        content     = content
    )
}