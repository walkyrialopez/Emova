package com.emotionalintelligence.emova.ui.growth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emotionalintelligence.emova.R
import com.emotionalintelligence.emova.ui.conflict.ConflictScreen
import com.emotionalintelligence.emova.ui.lessons.LessonsScreen
import com.emotionalintelligence.emova.ui.vocabulary.VocabularyScreen

private enum class GrowthPage { LESSONS, CONFLICT, VOCABULARY }

/**
 * Growth = daily lesson first. Vocabulary and conflict work are tools one tap away
 * (they used to be tabs of equal weight, which made the screen feel crowded).
 *
 * [startTab] keeps the old numbering: 0 lessons, 1 conflict ("Reflect"), 2 vocabulary ("Words").
 */
@Composable
fun GrowthScreen(startTab: Int = 0) {
    var page by remember {
        mutableStateOf(when (startTab) { 1 -> GrowthPage.CONFLICT; 2 -> GrowthPage.VOCABULARY; else -> GrowthPage.LESSONS })
    }

    BackHandler(enabled = page != GrowthPage.LESSONS) { page = GrowthPage.LESSONS }

    when (page) {
        GrowthPage.LESSONS -> LessonsScreen(
            onOpenVocabulary = { page = GrowthPage.VOCABULARY },
            onOpenConflict = { page = GrowthPage.CONFLICT }
        )
        else -> Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF7F6FB))) {
            TextButton(
                onClick = { page = GrowthPage.LESSONS },
                modifier = Modifier.padding(start = 8.dp, top = 8.dp)
            ) { Text(stringResource(R.string.growth_back), color = Color(0xFF6B6B8A), fontSize = 15.sp) }

            Column(modifier = Modifier.weight(1f)) {
                if (page == GrowthPage.CONFLICT) ConflictScreen() else VocabularyScreen()
            }
        }
    }
}
