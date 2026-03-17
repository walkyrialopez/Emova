package com.emotionalintelligence.emova.ui.breathing

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import com.emotionalintelligence.emova.ui.sos.SOSPanel
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emotionalintelligence.emova.R
import kotlinx.coroutines.delay

// ─── Data ─────────────────────────────────────────────────────────────────────

data class BreathingPattern(
    val nameResId: Int,
    val descResId: Int,
    val inhale: Int,
    val holdIn: Int,
    val exhale: Int,
    val holdOut: Int,
    val color: Color,
    val emoji: String
)

// Indices into breathing_pattern_names / breathing_pattern_descs arrays
val breathingPatterns = listOf(
    BreathingPattern(0, 0, 4, 4, 4, 4, Color(0xFF6C63FF), "⬜"),
    BreathingPattern(1, 1, 4, 7, 8, 0, Color(0xFF4CC9F0), "🌊"),
    BreathingPattern(2, 2, 5, 0, 7, 0, Color(0xFF06D6A0), "🌿"),
    BreathingPattern(3, 3, 2, 0, 4, 0, Color(0xFFE8A020), "⚡")
)

// ─── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun BreathingScreen(onBack: (() -> Unit)? = null) {
    var selectedPattern by remember { mutableStateOf<BreathingPattern?>(null) }
    if (selectedPattern != null) {
        BreathingExercise(pattern = selectedPattern!!, onBack = { selectedPattern = null })
    } else {
        BreathingMenu(onSelect = { selectedPattern = it })
    }
}

// ─── Menu ─────────────────────────────────────────────────────────────────────

@Composable
fun BreathingMenu(onSelect: (BreathingPattern) -> Unit, onBack: (() -> Unit)? = null) {
    val names = stringArrayResource(R.array.breathing_pattern_names)
    val descs = stringArrayResource(R.array.breathing_pattern_descs)

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF7F6FB)),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(R.string.breathe_title), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
            Text(stringResource(R.string.breathe_subtitle), fontSize = 14.sp, color = Color(0xFF6B6B8A))
            Spacer(modifier = Modifier.height(16.dp))
        }
        item {
            SOSPanel()
            Spacer(modifier = Modifier.height(8.dp))
        }
        items(breathingPatterns.size) { index ->
            val pattern = breathingPatterns[index]
            BreathingPatternCard(
                pattern = pattern,
                name = names.getOrElse(index) { "" },
                desc = descs.getOrElse(index) { "" },
                onClick = { onSelect(pattern) }
            )
        }
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFEDECFF)).padding(16.dp)
            ) {
                Text(stringResource(R.string.breathe_tip_label), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6C63FF))
                Spacer(modifier = Modifier.height(6.dp))
                Text(stringResource(R.string.breathe_tip_body),
                    fontSize = 13.sp, color = Color(0xFF1A1A2E), lineHeight = 20.sp)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ─── Pattern Card ─────────────────────────────────────────────────────────────

@Composable
fun BreathingPatternCard(pattern: BreathingPattern, name: String, desc: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFFFFFFF))
            .border(1.dp, Color(0xFFEAE8F5), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick).padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(52.dp).clip(CircleShape).background(pattern.color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center) {
            Text(pattern.emoji, fontSize = 24.sp)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
            Text(desc, fontSize = 12.sp, color = Color(0xFF6B6B8A), lineHeight = 18.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                PhaseChip(stringResource(R.string.breathe_chip_in, pattern.inhale), pattern.color)
                if (pattern.holdIn > 0) PhaseChip(stringResource(R.string.breathe_chip_hold, pattern.holdIn), pattern.color)
                PhaseChip(stringResource(R.string.breathe_chip_out, pattern.exhale), pattern.color)
                if (pattern.holdOut > 0) PhaseChip(stringResource(R.string.breathe_chip_hold, pattern.holdOut), pattern.color)
            }
        }
    }
}

@Composable
fun PhaseChip(label: String, color: Color) {
    Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(color.copy(alpha = 0.12f))
        .padding(horizontal = 8.dp, vertical = 3.dp)) {
        Text(label, fontSize = 11.sp, color = color, fontWeight = FontWeight.Medium)
    }
}

// ─── Exercise ─────────────────────────────────────────────────────────────────

enum class BreathPhase { INHALE, HOLD_IN, EXHALE, HOLD_OUT, DONE }

@Composable
fun BreathingExercise(pattern: BreathingPattern, onBack: () -> Unit) {
    val names = stringArrayResource(R.array.breathing_pattern_names)
    val patternName = names.getOrElse(pattern.nameResId) { "" }

    var phase by remember { mutableStateOf(BreathPhase.INHALE) }
    var secondsLeft by remember { mutableIntStateOf(pattern.inhale) }
    var roundsLeft by remember { mutableIntStateOf(4) }
    var isRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning, phase, roundsLeft) {
        if (!isRunning || phase == BreathPhase.DONE) return@LaunchedEffect
        while (secondsLeft > 0) { delay(1000); secondsLeft-- }
        val next = when (phase) {
            BreathPhase.INHALE -> if (pattern.holdIn > 0) BreathPhase.HOLD_IN else BreathPhase.EXHALE
            BreathPhase.HOLD_IN -> BreathPhase.EXHALE
            BreathPhase.EXHALE -> if (pattern.holdOut > 0) BreathPhase.HOLD_OUT else if (roundsLeft <= 1) BreathPhase.DONE else BreathPhase.INHALE
            BreathPhase.HOLD_OUT -> if (roundsLeft <= 1) BreathPhase.DONE else BreathPhase.INHALE
            BreathPhase.DONE -> BreathPhase.DONE
        }
        if (next == BreathPhase.INHALE) roundsLeft--
        phase = next
        secondsLeft = when (next) {
            BreathPhase.INHALE -> pattern.inhale; BreathPhase.HOLD_IN -> pattern.holdIn
            BreathPhase.EXHALE -> pattern.exhale; BreathPhase.HOLD_OUT -> pattern.holdOut
            BreathPhase.DONE -> 0
        }
    }

    val targetScale = when (phase) { BreathPhase.INHALE, BreathPhase.HOLD_IN -> 1.4f; else -> 0.7f }
    val animDuration = when (phase) { BreathPhase.INHALE -> pattern.inhale * 1000; BreathPhase.EXHALE -> pattern.exhale * 1000; else -> 300 }
    val circleScale by animateFloatAsState(targetValue = targetScale, animationSpec = tween(animDuration, easing = EaseInOutSine), label = "circle")

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF7F6FB)).padding(horizontal = 20.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = onBack) { Text(stringResource(R.string.breathe_back), color = Color(0xFF6B6B8A), fontSize = 15.sp) }
        }
        Text(patternName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E), textAlign = TextAlign.Center)
        Text(stringResource(R.string.breathe_rounds_left, roundsLeft), fontSize = 13.sp, color = Color(0xFF6B6B8A))
        Spacer(modifier = Modifier.weight(1f))
        if (phase == BreathPhase.DONE) {
            Text("🎉", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.breathe_well_done), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
            Text(stringResource(R.string.breathe_completed, patternName), fontSize = 15.sp, color = Color(0xFF6B6B8A))
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = pattern.color)) {
                Text(stringResource(R.string.breathe_done), fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
            }
        } else {
            Box(contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.size((220 * circleScale).dp).clip(CircleShape).background(pattern.color.copy(alpha = 0.08f)))
                Box(modifier = Modifier.size((160 * circleScale).dp).clip(CircleShape).background(pattern.color.copy(alpha = 0.2f))
                    .border(3.dp, pattern.color.copy(alpha = 0.5f), CircleShape), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            when (phase) {
                                BreathPhase.INHALE -> stringResource(R.string.breathe_inhale)
                                BreathPhase.HOLD_IN, BreathPhase.HOLD_OUT -> stringResource(R.string.breathe_hold)
                                BreathPhase.EXHALE -> stringResource(R.string.breathe_exhale)
                                else -> ""
                            },
                            fontSize = 20.sp, fontWeight = FontWeight.Bold, color = pattern.color
                        )
                        Text("$secondsLeft", fontSize = 42.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
                    }
                }
            }
            Spacer(modifier = Modifier.height(48.dp))
            if (!isRunning) {
                Button(onClick = { isRunning = true }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = pattern.color)) {
                    Text(stringResource(R.string.breathe_begin), fontWeight = FontWeight.Bold, color = Color.White, fontSize = 17.sp)
                }
            } else {
                OutlinedButton(onClick = { isRunning = false }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, pattern.color)) {
                    Text(stringResource(R.string.breathe_pause), fontWeight = FontWeight.Bold, color = pattern.color, fontSize = 17.sp)
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}