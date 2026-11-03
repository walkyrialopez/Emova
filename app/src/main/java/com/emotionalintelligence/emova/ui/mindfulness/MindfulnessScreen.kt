package com.emotionalintelligence.emova.ui.mindfulness

import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emotionalintelligence.emova.R
import com.emotionalintelligence.emova.data.EmováDatabase
import com.emotionalintelligence.emova.model.HabitRecord
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Guided audio sessions.
 *
 * Drop your recordings in  app/src/main/res/raw/  with EXACTLY these names (lowercase):
 *     mindful_5.mp3   mindful_10.mp3   mindful_20.mp3
 * Sessions whose file is missing show "coming soon" instead of crashing, so you can add
 * them one at a time.
 */
data class MindfulSession(
    val minutes: Int,
    val rawName: String,
    val titleRes: Int,
    val descRes: Int,
    val emoji: String,
    val color: Color
)

val mindfulSessions = listOf(
    MindfulSession(5, "mindful_5", R.string.mf_s5_title, R.string.mf_s5_desc, "🌬️", Color(0xFF4CC9F0)),
    MindfulSession(10, "mindful_10", R.string.mf_s10_title, R.string.mf_s10_desc, "🍃", Color(0xFF6C63FF)),
    MindfulSession(20, "mindful_20", R.string.mf_s20_title, R.string.mf_s20_desc, "🌙", Color(0xFF06D6A0))
)

private val Ink = Color(0xFF111130)
private val Muted = Color(0xFF6B6B8A)

/** Compact rows for the Calm screen. Missing audio files show "coming soon" instead of crashing. */
@Composable
fun MindfulSessionRows(onSelect: (MindfulSession) -> Unit) {
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        mindfulSessions.forEach { s ->
            val available = context.resources.getIdentifier(s.rawName, "raw", context.packageName) != 0
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .clickable(enabled = available) { onSelect(s) }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(s.color.copy(alpha = if (available) 0.15f else 0.07f)),
                    contentAlignment = Alignment.Center
                ) { Text(s.emoji, fontSize = 20.sp) }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(s.titleRes), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (available) Ink else Muted)
                    Text(stringResource(s.descRes), fontSize = 12.sp, color = Muted, maxLines = 1)
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (available) stringResource(R.string.mf_minutes, s.minutes) else stringResource(R.string.mf_coming),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (available) s.color else Muted
                )
            }
        }
    }
}

@Composable
fun MindfulSessionPlayer(session: MindfulSession, onBack: () -> Unit, onCompleted: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val habitDao = remember { EmováDatabase.getInstance(context).habitRecordDao() }

    val player = remember(session) {
        val resId = context.resources.getIdentifier(session.rawName, "raw", context.packageName)
        if (resId == 0) null else MediaPlayer.create(context, resId)
    }

    var isPlaying by remember { mutableStateOf(false) }
    var positionMs by remember { mutableIntStateOf(0) }
    var dragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }
    var completed by remember { mutableStateOf(false) }
    val durationMs = remember(player) { player?.duration ?: 0 }

    DisposableEffect(player) {
        player?.setOnCompletionListener {
            isPlaying = false
            completed = true
            onCompleted()
            scope.launch { habitDao.insert(HabitRecord(habitId = "mindfulness")) }
        }
        onDispose {
            player?.setOnCompletionListener(null)
            runCatching { player?.stop() }
            player?.release()
        }
    }

    // Progress ticker
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            if (!dragging) positionMs = player?.currentPosition ?: 0
            delay(300)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = onBack) { Text("← " + stringResource(R.string.mf_back), color = Muted) }
        }
        Spacer(Modifier.weight(1f))

        Box(
            modifier = Modifier.size(140.dp).clip(CircleShape).background(session.color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) { Text(session.emoji, fontSize = 64.sp) }

        Spacer(Modifier.height(24.dp))
        Text(stringResource(session.titleRes), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Ink, textAlign = TextAlign.Center)
        Text(stringResource(R.string.mf_minutes, session.minutes), fontSize = 14.sp, color = Muted)
        Spacer(Modifier.height(8.dp))

        if (completed) {
            Text(stringResource(R.string.mf_done), fontSize = 16.sp, color = session.color, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(28.dp))

        val fraction = if (durationMs > 0) positionMs / durationMs.toFloat() else 0f
        Slider(
            value = if (dragging) dragFraction else fraction,
            onValueChange = { dragging = true; dragFraction = it },
            onValueChangeFinished = {
                player?.seekTo((dragFraction * durationMs).toInt())
                positionMs = (dragFraction * durationMs).toInt()
                dragging = false
            },
            colors = SliderDefaults.colors(thumbColor = session.color, activeTrackColor = session.color, inactiveTrackColor = Color(0xFFEAE8F5))
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatTime(positionMs), fontSize = 12.sp, color = Muted)
            Text(formatTime(durationMs), fontSize = 12.sp, color = Muted)
        }

        Spacer(Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(session.color)
                .clickable(enabled = player != null) {
                    if (isPlaying) {
                        player?.pause(); isPlaying = false
                    } else {
                        if (completed) { player?.seekTo(0); completed = false }
                        player?.start(); isPlaying = true
                    }
                },
            contentAlignment = Alignment.Center
        ) { Text(if (isPlaying) "⏸" else "▶", fontSize = 28.sp, color = Color.White) }

        Spacer(Modifier.weight(1.4f))
    }
}

private fun formatTime(ms: Int): String {
    val totalSeconds = ms / 1000
    return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}
