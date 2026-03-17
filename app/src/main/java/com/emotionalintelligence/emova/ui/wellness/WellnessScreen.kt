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
import com.emotionalintelligence.emova.ui.breathing.BreathingExercise
import com.emotionalintelligence.emova.ui.breathing.BreathingMenu
import com.emotionalintelligence.emova.ui.breathing.BreathingPattern
import com.emotionalintelligence.emova.ui.gratitude.GratitudeScreen
import com.emotionalintelligence.emova.ui.tips.DailyTipsScreen
import com.emotionalintelligence.emova.ui.habits.GoldHabitsScreen

@Composable
fun WellnessScreen(startTab: Int = 0) {
    var selectedTab by remember { mutableStateOf(startTab) }
    var selectedPattern by remember { mutableStateOf<BreathingPattern?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF7F6FB))) {
        if (selectedPattern == null) {
            val tabs = listOf(
                stringResource(R.string.wellness_tab_breathe),
                stringResource(R.string.wellness_tab_gratitude),
                stringResource(R.string.wellness_tab_habits),
                stringResource(R.string.wellness_tab_tips)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
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
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) Color(0xFF6C63FF) else Color(0xFF6B6B8A)
                        )
                    }
                }
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when {
                selectedPattern != null -> BreathingExercise(
                    pattern = selectedPattern!!,
                    onBack = { selectedPattern = null }
                )
                selectedTab == 0 -> BreathingMenu(onSelect = { selectedPattern = it })
                selectedTab == 1 -> GratitudeScreen()
                selectedTab == 2 -> GoldHabitsScreen()
                selectedTab == 3 -> DailyTipsScreen()
            }
        }
    }
}