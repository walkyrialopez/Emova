package com.emotionalintelligence.emova.ui.wellness

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emotionalintelligence.emova.R
import com.emotionalintelligence.emova.ui.calm.CalmScreen
import com.emotionalintelligence.emova.ui.gratitude.GratitudeScreen
import com.emotionalintelligence.emova.ui.habits.GoldHabitsScreen
import com.emotionalintelligence.emova.ui.tips.DailyTipsScreen

/**
 * Tabs: 0 Calma (breathe + grounding + meditate), 1 Gratitud, 2 Hábitos, 3 Consejos.
 */
@Composable
fun WellnessScreen(startTab: Int = 0) {
    var selectedTab by remember { mutableIntStateOf(startTab.coerceIn(0, 3)) }
    var exerciseRunning by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF7F6FB))) {
        if (!exerciseRunning) {
            val tabs = listOf(
                stringResource(R.string.wellness_tab_calm),
                stringResource(R.string.wellness_tab_gratitude),
                stringResource(R.string.wellness_tab_habits),
                stringResource(R.string.wellness_tab_tips)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFEAE8F5))
            ) {
                tabs.forEachIndexed { index, label ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(3.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedTab == index) Color.White else Color.Transparent)
                            .clickable { selectedTab = index }
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            maxLines = 1,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) Color(0xFF6C63FF) else Color(0xFF6B6B8A)
                        )
                    }
                }
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (selectedTab) {
                0 -> CalmScreen(onImmersiveChange = { exerciseRunning = it })
                1 -> GratitudeScreen()
                2 -> GoldHabitsScreen()
                3 -> DailyTipsScreen()
            }
        }
    }
}
