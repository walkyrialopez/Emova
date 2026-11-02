package com.emotionalintelligence.emova.ui.breathing

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.emotionalintelligence.emova.R
import com.emotionalintelligence.emova.util.BreathHaptics
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
fun BreathingExercise(pattern: BreathingPattern, onBack: () -> Unit, onCompleted: () -> Unit = {}) {
    val names = stringArrayResource(R.array.breathing_pattern_names)
    val patternName = names.getOrElse(pattern.nameResId) { "" }

    var phase by remember { mutableStateOf(BreathPhase.INHALE) }
    var secondsLeft by remember { mutableIntStateOf(pattern.inhale) }
    var roundsLeft by remember { mutableIntStateOf(4) }
    var isRunning by remember { mutableStateOf(false) }

    // ── Vibration guide (eyes-closed mode) ───────────────────────────────
    val context = LocalContext.current
    val haptics = remember { BreathHaptics(context) }
    var hapticsOn by remember { mutableStateOf(BreathHaptics.isEnabled(context)) }
    var showHow by remember { mutableStateOf(!BreathHaptics.introSeen(context)) }
    var hasStarted by remember { mutableStateOf(false) }
    var countdown by remember { mutableIntStateOf(0) }   // 3..1 while "get ready", 0 otherwise

    // Keep the screen on while exercising: with eyes closed nobody touches it, and it must not lock.
    val view = LocalView.current
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false; haptics.cancel() }
    }

    // 3-2-1 so the person has time to close their eyes before the first inhale.
    LaunchedEffect(countdown) {
        if (countdown > 0) {
            delay(1000)
            if (countdown == 1) { countdown = 0; isRunning = true } else countdown--
        }
    }

    // One vibration pattern per phase change.
    LaunchedEffect(isRunning, phase) {
        if (!isRunning || !hapticsOn || !haptics.isAvailable) return@LaunchedEffect
        when (phase) {
            BreathPhase.INHALE -> haptics.inhale()
            BreathPhase.EXHALE -> haptics.exhale()
            BreathPhase.DONE -> haptics.finish()
            else -> Unit   // holds: silence
        }
    }

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

    LaunchedEffect(phase) { if (phase == BreathPhase.DONE) onCompleted() }

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
                            if (countdown > 0) stringResource(R.string.breathe_get_ready) else when (phase) {
                                BreathPhase.INHALE -> stringResource(R.string.breathe_inhale)
                                BreathPhase.HOLD_IN, BreathPhase.HOLD_OUT -> stringResource(R.string.breathe_hold)
                                BreathPhase.EXHALE -> stringResource(R.string.breathe_exhale)
                                else -> ""
                            },
                            fontSize = 20.sp, fontWeight = FontWeight.Bold, color = pattern.color
                        )
                        Text(if (countdown > 0) "$countdown" else "$secondsLeft", fontSize = 42.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))

            // Vibration guide: explained before the first start, toggleable afterwards.
            if (!hasStarted && haptics.isAvailable) {
                HapticGuide(
                    haptics = haptics,
                    enabled = hapticsOn,
                    expanded = showHow,
                    accent = pattern.color,
                    onToggle = { hapticsOn = it; BreathHaptics.setEnabled(context, it) },
                    onExpandChange = { showHow = it }
                )
                Spacer(modifier = Modifier.height(20.dp))
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (countdown > 0) {
                // nothing to press while "get ready" runs
                Spacer(modifier = Modifier.height(56.dp))
            } else if (!isRunning) {
                Button(onClick = {
                    BreathHaptics.markIntroSeen(context)
                    if (hasStarted) isRunning = true else { hasStarted = true; countdown = 3 }
                }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp),
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

// ─── Vibration guide (explains the eyes-closed mode before the exercise starts) ──

@Composable
private fun HapticGuide(
    haptics: BreathHaptics,
    enabled: Boolean,
    expanded: Boolean,
    accent: Color,
    onToggle: (Boolean) -> Unit,
    onExpandChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFEAE8F5), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("📳", fontSize = 22.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.haptic_title), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
                Text(stringResource(R.string.haptic_toggle), fontSize = 12.sp, color = Color(0xFF6B6B8A))
            }
            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(checkedTrackColor = accent)
            )
        }

        if (enabled) {
            TextButton(onClick = { onExpandChange(!expanded) }, contentPadding = PaddingValues(horizontal = 0.dp)) {
                Text(
                    stringResource(if (expanded) R.string.haptic_hide else R.string.haptic_how),
                    fontSize = 13.sp, color = accent, fontWeight = FontWeight.Medium
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.haptic_intro), fontSize = 13.sp, color = Color(0xFF1A1A2E), lineHeight = 19.sp)
                    HapticRow("•", stringResource(R.string.haptic_l_inhale), stringResource(R.string.breathe_inhale), accent) { haptics.inhale() }
                    HapticRow("• •", stringResource(R.string.haptic_l_exhale), stringResource(R.string.breathe_exhale), accent) { haptics.exhale() }
                    HapticRow("·", stringResource(R.string.haptic_l_hold), stringResource(R.string.breathe_hold), accent, onTest = null)
                    HapticRow("▬", stringResource(R.string.haptic_l_done), stringResource(R.string.haptic_m_done), accent) { haptics.finish() }
                    Text(stringResource(R.string.haptic_note), fontSize = 11.sp, color = Color(0xFF6B6B8A), lineHeight = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun HapticRow(symbol: String, label: String, meaning: String, accent: Color, onTest: (() -> Unit)?) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
            .background(accent.copy(alpha = 0.08f))
            .then(if (onTest != null) Modifier.clickable(onClick = onTest) else Modifier)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(symbol, fontSize = 16.sp, color = accent, fontWeight = FontWeight.Bold, modifier = Modifier.width(36.dp))
        Text(label, fontSize = 13.sp, color = Color(0xFF1A1A2E), modifier = Modifier.weight(1f))
        Text("→ $meaning", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = accent)
    }
}
