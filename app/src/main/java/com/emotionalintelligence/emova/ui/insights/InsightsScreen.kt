package com.emotionalintelligence.emova.ui.insights

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.emotionalintelligence.emova.R
import com.emotionalintelligence.emova.viewmodel.*

@Composable
fun InsightsScreen(
    onNavigateToCheckIn: () -> Unit = {},
    vm: InsightsViewModel = viewModel()
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F6FB))
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(top = 32.dp, bottom = 100.dp)
    ) {

        // ── Header ────────────────────────────────────────────────────────────
        item {
            Text(
                stringResource(R.string.insights_title),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A2E),
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        // ── Period toggle ─────────────────────────────────────────────────────
        item { PeriodToggle(period = state.period, onSelect = vm::setPeriod) }

        // ── Period navigator ──────────────────────────────────────────────────
        item {
            PeriodNavigator(
                label = state.periodLabel,
                canGoForward = state.weekOffset < 0,
                onBack = { vm.shiftWeek(-1) },
                onForward = { vm.shiftWeek(1) }
            )
        }

        // ── Hero discovery card ───────────────────────────────────────────────
        if (state.patterns.isNotEmpty()) {
            item {
                Text(
                    stringResource(R.string.insights_discovery_label),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6C63FF),
                    modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                )
                HeroPatternCard(pattern = state.patterns.first())
            }
        }

        // ── Stat cards row ────────────────────────────────────────────────────
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                state.topEmotion?.let {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        emoji = it.emoji,
                        value = it.name,
                        label = stringResource(R.string.insights_top_feeling)
                    )
                } ?: StatCard(
                    modifier = Modifier.weight(1f),
                    emoji = "—",
                    value = "—",
                    label = stringResource(R.string.insights_top_feeling)
                )

                StatCard(
                    modifier = Modifier.weight(1f),
                    emoji = "⚖️",
                    value = "${state.positiveBalancePercentage}%",
                    label = stringResource(R.string.insights_positive_label)
                )

                StatCard(
                    modifier = Modifier.weight(1f),
                    emoji = "📝",
                    value = "${state.totalCheckIns}",
                    label = stringResource(R.string.insights_checkins_label)
                )
            }
        }

        // ── Charts & breakdown ────────────────────────────────────────────────
        if (state.weekData.isEmpty() && state.familyBreakdown.isEmpty()) {
            item { EmptyState(onNavigateToCheckIn = onNavigateToCheckIn) }
        } else {
            if (state.weekData.isNotEmpty()) {
                item { EmotionBarChart(data = state.weekData) }
            }
            if (state.familyBreakdown.isNotEmpty()) {
                item { FamilyBreakdownCard(breakdown = state.familyBreakdown) }
            }

            // ── Secondary patterns ────────────────────────────────────────────
            if (state.patterns.size > 1) {
                item {
                    Text(
                        stringResource(R.string.insights_other_patterns),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A2E),
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp, start = 4.dp)
                    )
                }
                items(state.patterns.drop(1)) { pattern ->
                    PatternCard(pattern = pattern)
                }
            }
        }
    }
}

// ─── Period Toggle ────────────────────────────────────────────────────────────

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

// ─── Period Navigator ─────────────────────────────────────────────────────────

@Composable
fun PeriodNavigator(
    label: String,
    canGoForward: Boolean,
    onBack: () -> Unit,
    onForward: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp), spotColor = Color(0x1A000000))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                Text("←", fontSize = 18.sp, color = Color(0xFF6C63FF), fontWeight = FontWeight.Bold)
            }
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1A2E),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            IconButton(onClick = onForward, enabled = canGoForward, modifier = Modifier.size(36.dp)) {
                Text(
                    "→",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (canGoForward) Color(0xFF6C63FF) else Color(0xFFB0B0C0)
                )
            }
        }
    }
}

// ─── Hero Pattern Card ────────────────────────────────────────────────────────

@Composable
fun HeroPatternCard(pattern: PatternInsight) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(20.dp), spotColor = pattern.color.copy(alpha = 0.4f))
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color.White, pattern.color.copy(alpha = 0.08f))
                )
            )
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(pattern.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(pattern.emoji, fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = pattern.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1A1A2E)
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = pattern.description,
            fontSize = 14.sp,
            color = Color(0xFF4A4A68),
            lineHeight = 22.sp
        )
    }
}

// ─── Stat Card ────────────────────────────────────────────────────────────────

@Composable
fun StatCard(modifier: Modifier = Modifier, emoji: String, value: String, label: String) {
    Column(
        modifier = modifier
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp), spotColor = Color(0x1A000000))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(emoji, fontSize = 22.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A2E),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        Text(
            label,
            fontSize = 11.sp,
            color = Color(0xFF6B6B8A),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

// ─── Emotion Bar Chart ────────────────────────────────────────────────────────

@Composable
fun EmotionBarChart(data: List<DayData>) {
    val maxCount = data.maxOf { it.count }.coerceAtLeast(1)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(18.dp), spotColor = Color(0x1A000000))
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .padding(18.dp)
    ) {
        Text(
            text = stringResource(R.string.insights_checkins),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A2E)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { day ->
                val targetFraction = if (day.count > 0) day.count.toFloat() / maxCount else 0.02f
                val animFraction by animateFloatAsState(
                    targetValue = targetFraction,
                    animationSpec = tween(500, easing = EaseOutQuart),
                    label = "bar"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    if (day.count > 0) {
                        Text("${day.count}", fontSize = 10.sp, color = Color(0xFF6B6B8A))
                        Spacer(modifier = Modifier.height(4.dp))
                    } else {
                        Spacer(modifier = Modifier.height(18.dp))
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .fillMaxHeight(animFraction)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(if (day.count > 0) day.color else Color(0xFFEAE8F5))
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(day.label, fontSize = 11.sp, color = Color(0xFF6B6B8A))
                }
            }
        }
    }
}

// ─── Family Breakdown Card ────────────────────────────────────────────────────

@Composable
fun FamilyBreakdownCard(breakdown: List<Triple<String, Color, Int>>) {
    val total = breakdown.sumOf { it.third }.coerceAtLeast(1)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(18.dp), spotColor = Color(0x1A000000))
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            stringResource(R.string.insights_emotion_mix),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A2E)
        )

        Row(modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp))) {
            breakdown.forEach { (_, color, count) ->
                val fraction by animateFloatAsState(
                    targetValue = count.toFloat() / total,
                    animationSpec = tween(500),
                    label = "frac"
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(fraction.coerceAtLeast(0.01f))
                        .background(color)
                )
            }
        }

        breakdown.forEach { (name, color, count) ->
            val pct = count * 100 / total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
                    Text(name, fontSize = 14.sp, color = Color(0xFF1A1A2E))
                }
                Text("$count ($pct%)", fontSize = 13.sp, color = Color(0xFF6B6B8A))
            }
        }
    }
}

// ─── Pattern Card (secondary) ─────────────────────────────────────────────────

@Composable
fun PatternCard(pattern: PatternInsight) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(pattern.color.copy(alpha = 0.08f))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(pattern.color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(pattern.emoji, fontSize = 20.sp)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                pattern.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A2E)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                pattern.description,
                fontSize = 13.sp,
                color = Color(0xFF6B6B8A),
                lineHeight = 19.sp
            )
        }
    }
}

// ─── Empty State ──────────────────────────────────────────────────────────────

@Composable
fun EmptyState(onNavigateToCheckIn: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("📊", fontSize = 48.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            stringResource(R.string.insights_empty_title),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A2E)
        )
        Text(
            stringResource(R.string.insights_empty_subtitle),
            fontSize = 14.sp,
            color = Color(0xFF6B6B8A),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onNavigateToCheckIn,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF)),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                stringResource(R.string.insights_first_checkin_cta),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}