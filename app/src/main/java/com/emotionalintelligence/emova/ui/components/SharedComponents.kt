package com.emotionalintelligence.emova.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shared educational insight card used across Breathing, CheckIn,
 * Gratitude, Journal, and Today screens.
 *
 * Usage:
 *   WhyItWorksCard(message = stringResource(R.string.why_it_works_breathing))
 *
 * Now that this lives in one place, you can also make it dismissible in future:
 *   WhyItWorksCard(message = "...", onDismiss = { ... })
 */
@Composable
fun WhyItWorksCard(
    message: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF6C63FF).copy(alpha = 0.08f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("💡", fontSize = 20.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = message,
            fontSize = 13.sp,
            color = Color(0xFF1A1A2E),
            lineHeight = 18.sp
        )
    }
}