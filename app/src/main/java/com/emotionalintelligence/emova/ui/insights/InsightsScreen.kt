package com.emotionalintelligence.emova.ui.insights

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.emotionalintelligence.emova.viewmodel.InsightsViewModel
import com.emotionalintelligence.emova.viewmodel.InsightsPeriod
import com.emotionalintelligence.emova.viewmodel.PatternInsight
import com.emotionalintelligence.emova.viewmodel.TriggerInsight
import com.emotionalintelligence.emova.viewmodel.DayData
import com.emotionalintelligence.emova.viewmodel.InsightsUiState2

@Composable
fun InsightsScreen(vm: InsightsViewModel = viewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F6FB))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(stringResource(R.string.insights_title), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))

        PeriodToggle(period = state.period, onSelect = vm::setPeriod)

        PeriodNavigator(
            label = state.periodLabel,
            canGoForward = state.weekOffset < 0,
            onBack = { vm.shiftWeek(-1) },
            onForward = { vm.shiftWeek(1) }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                modifier = Modifier.weight(1f),
                emoji = "🔥",
                value = "${state.streakDays}",
                label = stringResource(R.string.insights_day_streak)
            )
            StatCard(
                modifier = Modifier.weight(1f),
                emoji = "💫",
                value = if (state.avgIntensity > 0) String.format("%.1f", state.avgIntensity) else "—",
                label = stringResource(R.string.insights_avg_intensity)
            )
            state.topEmotion?.let {
                StatCard(
                    modifier = Modifier.weight(1f),
                    emoji = it.emoji,
                    value = it.name,
                    label = stringResource(R.string.insights_top_feeling),
                    valueSize = 13
                )
            }
        }

        if (state.weekData.isNotEmpty()) {
            EmotionBarChart(data = state.weekData)
        }

        if (state.familyBreakdown.isNotEmpty()) {
            FamilyBreakdownCard(breakdown = state.familyBreakdown)
        }

        if (state.patterns.isNotEmpty()) {
            Text(stringResource(R.string.insights_patterns), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
            state.patterns.forEach { pattern ->
                PatternCard(pattern = pattern)
            }
        }

        if (state.weekData.isEmpty() && state.familyBreakdown.isEmpty()) {
            EmptyState()
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun PeriodToggle(period: InsightsPeriod, onSelect: (InsightsPeriod) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFEAE8F5))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        InsightsPeriod.values().forEach { p ->
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
                    text = when (p) {
                        InsightsPeriod.WEEK     -> stringResource(R.string.insights_weekly)
                        InsightsPeriod.MONTH    -> stringResource(R.string.insights_monthly)
                        InsightsPeriod.ALL_TIME -> stringResource(R.string.insights_all_time)
                    },
                    fontSize = 14.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color = if (selected) Color(0xFF6C63FF) else Color(0xFF6B6B8A)
                )
            }
        }
    }
}

@Composable
fun PeriodNavigator(label: String, canGoForward: Boolean, onBack: () -> Unit, onForward: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Text("←", fontSize = 20.sp, color = Color(0xFF6C63FF))
        }
        Text(label, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1A1A2E))
        IconButton(onClick = onForward, enabled = canGoForward) {
            Text("→", fontSize = 20.sp, color = if (canGoForward) Color(0xFF6C63FF) else Color(0xFFCCCCDD))
        }
    }
}

@Composable
fun StatCard(modifier: Modifier, emoji: String, value: String, label: String, valueSize: Int = 22) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFEAE8F5), RoundedCornerShape(16.dp))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(emoji, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontSize = valueSize.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E),
            textAlign = TextAlign.Center, maxLines = 1)
        Text(label, fontSize = 11.sp, color = Color(0xFF6B6B8A), textAlign = TextAlign.Center)
    }
}

@Composable
fun EmotionBarChart(data: List<DayData>) {
    val maxCount = data.maxOf { it.count }.coerceAtLeast(1)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFEAE8F5), RoundedCornerShape(18.dp))
            .padding(18.dp)
    ) {
        Text(stringResource(R.string.insights_checkins), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { day ->
                val fraction = day.count.toFloat() / maxCount
                val animFraction by animateFloatAsState(
                    targetValue = fraction,
                    animationSpec = tween(500, easing = EaseOutQuart),
                    label = "bar"
                )
                val barHeightDp = if (day.count > 0) (100 * animFraction).coerceAtLeast(8f) else 8f
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.weight(1f).height(120.dp)
                ) {
                    if (day.count > 0) {
                        Text("${day.count}", fontSize = 10.sp, color = Color(0xFF6B6B8A))
                        Spacer(modifier = Modifier.height(2.dp))
                    } else {
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(barHeightDp.dp)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(if (day.count > 0) day.color else Color(0xFFE0DFF5))
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(day.label, fontSize = 11.sp, color = Color(0xFF6B6B8A))
                }
            }
        }
    }
}

@Composable
fun FamilyBreakdownCard(breakdown: List<Triple<String, Color, Int>>) {
    val total = breakdown.sumOf { it.third }.coerceAtLeast(1)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFEAE8F5), RoundedCornerShape(18.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.insights_emotion_mix), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))

        Row(modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp))) {
            breakdown.forEach { (_, color, count) ->
                val fraction by animateFloatAsState(
                    targetValue = count.toFloat() / total,
                    animationSpec = tween(500),
                    label = "frac"
                )
                Box(modifier = Modifier.fillMaxHeight().weight(fraction.coerceAtLeast(0.01f)).background(color))
            }
        }

        breakdown.forEach { (name, color, count) ->
            val pct = count * 100 / total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
                    Text(name, fontSize = 14.sp, color = Color(0xFF1A1A2E))
                }
                Text("$count ($pct%)", fontSize = 13.sp, color = Color(0xFF6B6B8A))
            }
        }
    }
}

@Composable
fun TriggerCard(trigger: TriggerInsight) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(trigger.color.copy(alpha = 0.06f))
            .border(1.dp, trigger.color.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape)
                .background(trigger.color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) { Text(trigger.emoji, fontSize = 20.sp) }
        Column(modifier = Modifier.weight(1f)) {
            Text(trigger.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
            Spacer(modifier = Modifier.height(3.dp))
            Text(trigger.description, fontSize = 13.sp, color = Color(0xFF6B6B8A), lineHeight = 19.sp)
        }
    }
}

@Composable
fun PatternCard(pattern: PatternInsight) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(pattern.color.copy(alpha = 0.08f))
            .border(1.dp, pattern.color.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape)
                .background(pattern.color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) { Text(pattern.emoji, fontSize = 20.sp) }
        Column(modifier = Modifier.weight(1f)) {
            Text(pattern.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
            Spacer(modifier = Modifier.height(3.dp))
            Text(pattern.description, fontSize = 13.sp, color = Color(0xFF6B6B8A), lineHeight = 19.sp)
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("📊", fontSize = 48.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(stringResource(R.string.insights_empty_title), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
        Text(
            stringResource(R.string.insights_empty_subtitle),
            fontSize = 14.sp, color = Color(0xFF6B6B8A), textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
        )
    }
}