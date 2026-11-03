package com.emotionalintelligence.emova.ui.calm

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emotionalintelligence.emova.R
import com.emotionalintelligence.emova.ui.breathing.BreathingExercise
import com.emotionalintelligence.emova.ui.breathing.BreathingPattern
import com.emotionalintelligence.emova.ui.breathing.breathingPatterns
import com.emotionalintelligence.emova.ui.components.WhyInfoButton
import com.emotionalintelligence.emova.ui.mindfulness.MindfulSession
import com.emotionalintelligence.emova.ui.mindfulness.MindfulSessionPlayer
import com.emotionalintelligence.emova.ui.mindfulness.MindfulSessionRows
import com.emotionalintelligence.emova.ui.sos.GroundingExercise
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Ink = Color(0xFF1A1A2E)
private val Muted = Color(0xFF6B6B8A)
private val Accent = Color(0xFF6C63FF)

/** The home screen's "Calma" habit is done once any of the three exercises is completed. */
private fun markCalmDone(context: Context) {
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    context.getSharedPreferences("emova_today", Context.MODE_PRIVATE)
        .edit().putString("breathing_date", today).apply()
}

/**
 * One calm place for everything that settles you down:
 * breathing patterns, the 5-4-3-2-1 grounding exercise (the old "SOS") and guided meditations.
 * The "why does it work" explanations live behind the 💡 button so the screen stays light.
 *
 * [onImmersiveChange] lets the parent hide its tab bar while an exercise is running.
 */
@Composable
fun CalmScreen(onImmersiveChange: (Boolean) -> Unit = {}) {
    val context = LocalContext.current
    var pattern by remember { mutableStateOf<BreathingPattern?>(null) }
    var session by remember { mutableStateOf<MindfulSession?>(null) }
    var showGrounding by remember { mutableStateOf(false) }

    val activePattern = pattern
    val activeSession = session

    LaunchedEffect(activePattern, activeSession) {
        onImmersiveChange(activePattern != null || activeSession != null)
    }
    DisposableEffect(Unit) { onDispose { onImmersiveChange(false) } }

    when {
        activePattern != null -> BreathingExercise(
            pattern = activePattern,
            onBack = { pattern = null },
            onCompleted = { markCalmDone(context) }
        )
        activeSession != null -> MindfulSessionPlayer(
            session = activeSession,
            onBack = { session = null },
            onCompleted = { markCalmDone(context) }
        )
        else -> CalmHome(
            onPattern = { pattern = it },
            onGrounding = { showGrounding = true },
            onSession = { session = it }
        )
    }

    if (showGrounding) {
        GroundingExercise(
            onDismiss = { showGrounding = false },
            onCompleted = { markCalmDone(context) }
        )
    }
}

@Composable
private fun CalmHome(
    onPattern: (BreathingPattern) -> Unit,
    onGrounding: () -> Unit,
    onSession: (MindfulSession) -> Unit
) {
    var showInfo by remember { mutableStateOf(false) }
    val names = stringArrayResource(R.array.breathing_pattern_names)

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF7F6FB)),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Header + 💡
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.calm_title), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Ink)
                    Text(stringResource(R.string.calm_subtitle), fontSize = 14.sp, color = Muted)
                }
                WhyInfoButton(expanded = showInfo, onClick = { showInfo = !showInfo })
            }
            AnimatedVisibility(visible = showInfo) {
                Column(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Accent.copy(alpha = 0.08f))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    WhyLine("🌬️", stringResource(R.string.calm_sec_breathe), stringResource(R.string.calm_why_breathe))
                    WhyLine("🖐️", stringResource(R.string.calm_sec_ground), stringResource(R.string.calm_why_ground))
                    WhyLine("🎧", stringResource(R.string.calm_sec_meditate), stringResource(R.string.calm_why_meditate))
                }
            }
        }

        // Breathing: compact 2 x 2
        item { SectionLabel(stringResource(R.string.calm_sec_breathe)) }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                breathingPatterns.chunked(2).forEachIndexed { rowIndex, row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        row.forEachIndexed { colIndex, p ->
                            val index = rowIndex * 2 + colIndex
                            BreathingMiniCard(
                                pattern = p,
                                name = names.getOrElse(index) { "" },
                                modifier = Modifier.weight(1f),
                                onClick = { onPattern(p) }
                            )
                        }
                        if (row.size < 2) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }

        // Grounding (the old SOS panel, now just another exercise)
        item { SectionLabel(stringResource(R.string.calm_sec_ground)) }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFEAE8F5), RoundedCornerShape(16.dp))
                    .clickable(onClick = onGrounding)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFEF233C).copy(alpha = 0.10f)),
                    contentAlignment = Alignment.Center
                ) { Text("🖐️", fontSize = 20.sp) }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.calm_ground_title), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Ink)
                    Text(stringResource(R.string.calm_ground_desc), fontSize = 12.sp, color = Muted, lineHeight = 16.sp)
                }
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.calm_ground_time), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFEF233C))
            }
        }

        // Meditations
        item { SectionLabel(stringResource(R.string.calm_sec_meditate)) }
        item { MindfulSessionRows(onSelect = onSession) }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Muted,
        letterSpacing = 1.2.sp,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun WhyLine(emoji: String, title: String, body: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(emoji, fontSize = 18.sp)
        Spacer(Modifier.width(10.dp))
        Column {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Ink)
            Text(body, fontSize = 13.sp, color = Ink, lineHeight = 18.sp)
        }
    }
}

@Composable
private fun BreathingMiniCard(
    pattern: BreathingPattern,
    name: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val timing = listOf(pattern.inhale, pattern.holdIn, pattern.exhale, pattern.holdOut)
        .filter { it > 0 }.joinToString("-")
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFEAE8F5), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(pattern.color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) { Text(pattern.emoji, fontSize = 20.sp) }
        Spacer(Modifier.height(6.dp))
        Text(name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Ink, textAlign = TextAlign.Center, maxLines = 1)
        Text(timing, fontSize = 12.sp, color = pattern.color, fontWeight = FontWeight.Medium)
    }
}
