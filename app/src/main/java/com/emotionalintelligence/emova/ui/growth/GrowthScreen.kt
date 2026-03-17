package com.emotionalintelligence.emova.ui.growth

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
import com.emotionalintelligence.emova.ui.conflict.ConflictScreen
import com.emotionalintelligence.emova.ui.lessons.LessonsScreen
import com.emotionalintelligence.emova.ui.vocabulary.VocabularyScreen

@Composable
fun GrowthScreen(startTab: Int = 0) {
    var selectedTab by remember { mutableStateOf(startTab) }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF7F6FB))
    ) {
        val tabs = listOf(
            stringResource(R.string.growth_tab_lessons),
            stringResource(R.string.growth_tab_reflect),
            stringResource(R.string.growth_tab_words)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFEAE8F5))
        ) {
            tabs.forEachIndexed { index, label ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedTab == index) Color.White else Color.Transparent)
                        .clickable { selectedTab = index }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == index) Color(0xFF6C63FF) else Color(0xFF6B6B8A)
                    )
                }
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (selectedTab) {
                0 -> LessonsScreen()
                1 -> ConflictScreen()
                2 -> VocabularyScreen()
            }
        }
    }
}