package com.emotionalintelligence.emova.ui.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.emotionalintelligence.emova.R
import com.emotionalintelligence.emova.viewmodel.DayDot
import com.emotionalintelligence.emova.viewmodel.Discovery
import com.emotionalintelligence.emova.viewmodel.EmotionBar
import com.emotionalintelligence.emova.viewmodel.HistoryItem
import com.emotionalintelligence.emova.viewmodel.InsightsPeriod
import com.emotionalintelligence.emova.viewmodel.InsightsViewModel
import com.emotionalintelligence.emova.viewmodel.MIN_FOR_DEEP_PATTERNS

private val Ink = Color(0xFF1A1A2E)
private val Muted = Color(0xFF6B6B8A)
private val Accent = Color(0xFF6C63FF)

/**
 * One simple screen, top to bottom:
 *   1. Discoveries (what your check-ins reveal)  ← the point of the screen
 *   2. Three numbers
 *   3. Your emotions
 *   4. Last 7 days
 */
@Composable
fun InsightsScreen(
    onNavigateToCheckIn: () -> Unit = {},
    vm: InsightsViewModel = viewModel()
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    var showHistory by rememberSaveable { mutableStateOf(false) }
    val expanded = remember { mutableStateMapOf<Int, Boolean>() }
    var toDelete by remember { mutableStateOf<HistoryItem?>(null) }

    toDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text(stringResource(R.string.ins_h_delete_q)) },
            text = { Text(stringResource(R.string.ins_h_delete_body)) },
            confirmButton = {
                TextButton(onClick = { vm.deleteCheckIn(item.id); toDelete = null }) {
                    Text(stringResource(R.string.ins_h_confirm), color = Color(0xFFEF233C))
                }
            },
            dismissButton = {
                TextButton(onClick = { toDelete = null }) { Text(stringResource(R.string.ins_h_cancel)) }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF7F6FB)).padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 32.dp, bottom = 100.dp)
    ) {
        item { Text(stringResource(R.string.insights_title), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Ink) }

        if (state.allTimeCount == 0) {
            item { EmptyState(onNavigateToCheckIn) }
            return@LazyColumn
        }

        item { ViewToggle(showHistory, onChange = { showHistory = it }) }
        item { PeriodToggle(state.period, vm::setPeriod) }

        if (showHistory) {
            if (state.history.isEmpty()) {
                item { Text(stringResource(R.string.ins_h_empty), fontSize = 14.sp, color = Muted) }
            } else {
                item { Text(stringResource(R.string.ins_h_count, state.history.size), fontSize = 12.sp, color = Muted) }
                state.history.groupBy { it.dateLabel }.forEach { (label, list) ->
                    item(key = "day_$label") {
                        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Muted, modifier = Modifier.padding(top = 4.dp))
                    }
                    items(list, key = { it.id }) { entry ->
                        HistoryCard(
                            item = entry,
                            isExpanded = expanded[entry.id] == true,
                            onToggle = { expanded[entry.id] = expanded[entry.id] != true },
                            onDelete = { toDelete = entry }
                        )
                    }
                }
            }
            return@LazyColumn
        }

        // 1) Discoveries / progress toward them
        item {
            Text(stringResource(R.string.ins_discoveries_title), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Ink)
        }
        if (!state.isUnlocked) {
            item { UnlockCard(have = state.allTimeCount, need = MIN_FOR_DEEP_PATTERNS) }
        }
        if (state.discoveries.isEmpty()) {
            item { Text(stringResource(R.string.ins_no_discoveries), fontSize = 14.sp, color = Muted) }
        } else {
            items(state.discoveries) { DiscoveryCard(it) }
        }

        // 2) Numbers
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard(Modifier.weight(1f), "📝", "${state.totalCheckIns}", stringResource(R.string.insights_checkins_label))
                StatCard(Modifier.weight(1f), "☀️", "${state.positivePercent}%", stringResource(R.string.insights_positive_label))
                StatCard(Modifier.weight(1f), "🔥", "${state.streakDays}", stringResource(R.string.ins_streak_label))
            }
        }

        // 3) Emotions
        if (state.bars.isNotEmpty()) {
            item { EmotionBarsCard(state.bars) }
        }

        // 4) Last 7 days
        item { WeekStripCard(state.lastSevenDays) }
    }
}

@Composable
private fun PeriodToggle(period: InsightsPeriod, onSelect: (InsightsPeriod) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFEAE8F5)).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        InsightsPeriod.entries.forEach { p ->
            val selected = p == period
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selected) Color.White else Color.Transparent)
                    .clickable { onSelect(p) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(
                        when (p) {
                            InsightsPeriod.WEEK -> R.string.ins_period_7
                            InsightsPeriod.MONTH -> R.string.ins_period_30
                            InsightsPeriod.ALL_TIME -> R.string.insights_all_time
                        }
                    ),
                    fontSize = 14.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color = if (selected) Accent else Muted
                )
            }
        }
    }
}

@Composable
private fun UnlockCard(have: Int, need: Int) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Accent.copy(alpha = 0.08f)).padding(16.dp)
    ) {
        Text(stringResource(R.string.ins_unlock_title), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Ink)
        Spacer(Modifier.height(4.dp))
        Text(stringResource(R.string.ins_unlock_desc, have, need), fontSize = 13.sp, color = Muted, lineHeight = 18.sp)
        Spacer(Modifier.height(10.dp))
        Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(Color(0xFFEAE8F5))) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth((have / need.toFloat()).coerceIn(0f, 1f))
                    .clip(CircleShape)
                    .background(Accent)
            )
        }
    }
}

@Composable
private fun DiscoveryCard(d: Discovery) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White).padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(d.color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) { Text(d.emoji, fontSize = 20.sp) }
        Spacer(Modifier.width(14.dp))
        Text(d.text, fontSize = 14.sp, color = Ink, lineHeight = 20.sp, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(modifier: Modifier, emoji: String, value: String, label: String) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(16.dp)).background(Color.White).padding(vertical = 14.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(emoji, fontSize = 22.sp)
        Spacer(Modifier.height(4.dp))
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Ink)
        Text(label, fontSize = 11.sp, color = Muted, textAlign = TextAlign.Center, maxLines = 1)
    }
}

@Composable
private fun EmotionBarsCard(bars: List<EmotionBar>) {
    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White).padding(16.dp)) {
        Text(stringResource(R.string.ins_mix_title), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Ink)
        Spacer(Modifier.height(12.dp))
        bars.forEach { bar ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("${bar.emoji} ${bar.name}", fontSize = 13.sp, color = Ink, modifier = Modifier.width(120.dp), maxLines = 1)
                Box(modifier = Modifier.weight(1f).height(10.dp).clip(CircleShape).background(Color(0xFFF0F0F8))) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(bar.fraction.coerceIn(0.04f, 1f))
                            .clip(CircleShape)
                            .background(bar.color)
                    )
                }
                Text("${bar.count}", fontSize = 12.sp, color = Muted, modifier = Modifier.width(32.dp), textAlign = TextAlign.End)
            }
        }
    }
}

@Composable
private fun WeekStripCard(days: List<DayDot>) {
    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White).padding(16.dp)) {
        Text(stringResource(R.string.ins_week_title), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Ink)
        Text(stringResource(R.string.ins_week_note), fontSize = 11.sp, color = Muted)
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            days.forEach { day ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.size(34.dp).clip(CircleShape).background(day.color),
                        contentAlignment = Alignment.Center
                    ) {
                        if (day.count > 0) Text("${day.count}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(day.label, fontSize = 11.sp, color = Muted)
                }
            }
        }
    }
}

@Composable
private fun EmptyState(onNavigateToCheckIn: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🔍", fontSize = 48.sp)
        Spacer(Modifier.height(12.dp))
        Text(stringResource(R.string.insights_empty_title), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Ink)
        Spacer(Modifier.height(6.dp))
        Text(stringResource(R.string.ins_empty_subtitle), fontSize = 14.sp, color = Muted, textAlign = TextAlign.Center)
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onNavigateToCheckIn,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Accent)
        ) { Text(stringResource(R.string.insights_first_checkin_cta), color = Color.White) }
    }
}

@Composable
private fun ViewToggle(showHistory: Boolean, onChange: (Boolean) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(false to R.string.ins_tab_summary, true to R.string.ins_tab_history).forEach { (isHistory, label) ->
            val selected = showHistory == isHistory
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (selected) Accent else Color.White)
                    .clickable { onChange(isHistory) }
                    .padding(horizontal = 18.dp, vertical = 8.dp)
            ) {
                Text(
                    stringResource(label),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (selected) Color.White else Muted
                )
            }
        }
    }
}

@Composable
private fun HistoryCard(item: HistoryItem, isExpanded: Boolean, onToggle: () -> Unit, onDelete: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable(onClick = onToggle)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(item.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) { Text(item.emoji, fontSize = 20.sp) }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Ink)
                val intensityText = if (item.finalIntensity > 0 && item.finalIntensity != item.intensity)
                    stringResource(R.string.ins_h_intensity, item.intensity, item.finalIntensity)
                else stringResource(R.string.ins_h_intensity_single, item.intensity)
                Text("${item.time} · $intensityText", fontSize = 12.sp, color = Muted)
            }
            Text(if (isExpanded) "▲" else "▼", fontSize = 11.sp, color = item.color)
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(modifier = Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (item.triggers.isNotEmpty() || item.hardToControl) {
                    val tags = buildList {
                        item.triggers.forEach { add("${it.emoji} ${stringResource(it.labelResId)}") }
                        if (item.hardToControl) add(stringResource(R.string.ins_h_hard))
                    }
                    Text(tags.joinToString("  ·  "), fontSize = 12.sp, color = Muted, lineHeight = 18.sp)
                }
                HistoryField(stringResource(R.string.ins_h_note), item.note)
                HistoryField(stringResource(R.string.ins_h_could), item.couldHaveDone)
                HistoryField(stringResource(R.string.ins_h_perspective), item.perspective)
                HistoryField(stringResource(R.string.w_plan_cue), item.planCue)
                HistoryField(stringResource(R.string.w_plan_instead), item.planInstead)
                HistoryField(stringResource(R.string.w_plan_will), item.plan)
                if (item.aiComment.isNotBlank()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(item.color.copy(alpha = 0.08f))
                            .padding(12.dp)
                    ) {
                        Text(stringResource(R.string.ins_h_ai), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = item.color)
                        Spacer(Modifier.height(4.dp))
                        Text(item.aiComment, fontSize = 13.sp, color = Ink, lineHeight = 19.sp)
                    }
                }
                TextButton(onClick = onDelete) {
                    Text(stringResource(R.string.ins_h_delete), fontSize = 12.sp, color = Color(0xFFEF233C))
                }
            }
        }
    }
}

@Composable
private fun HistoryField(label: String, value: String) {
    if (value.isBlank()) return
    Column {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Muted)
        Text(value, fontSize = 14.sp, color = Ink, lineHeight = 20.sp)
    }
}
