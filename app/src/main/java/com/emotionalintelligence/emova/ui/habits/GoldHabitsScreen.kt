package com.emotionalintelligence.emova.ui.habits

import android.app.Application
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.emotionalintelligence.emova.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// ─── Data (ids only — display text comes from string arrays) ──────────────────

data class GoldHabit(
    val id: String,
    val emoji: String,
    val category: String   // kept as-is (not displayed in UI)
)

val goldHabits = listOf(
    GoldHabit("water",        "💧", "Body"),
    GoldHabit("sunlight",     "☀️", "Body"),
    GoldHabit("move",         "🚶", "Body"),
    GoldHabit("screen",       "📵", "Focus"),
    GoldHabit("tidy",         "🧹", "Environment"),
    GoldHabit("focus",        "🎯", "Focus"),
    GoldHabit("altruism",     "🎁", "Relationships"),
    GoldHabit("appreciation", "✨", "Relationships"),
    GoldHabit("reflection",   "✍️", "Mind"),
    GoldHabit("dialogue",     "💭", "Mind"),       // NUEVO: Diálogo Interno
    GoldHabit("eating",       "🥗", "Body"),       // NUEVO: Bocado Consciente
    GoldHabit("sleep",        "🌙", "Sleep")
)

// ─── ViewModel ────────────────────────────────────────────────────────────────

class GoldHabitsViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("emova_gold_habits", android.content.Context.MODE_PRIVATE)
    private val _checked = MutableStateFlow<Set<String>>(emptySet())
    val checked: StateFlow<Set<String>> = _checked.asStateFlow()
    private val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    init {
        viewModelScope.launch {
            val savedDate = prefs.getString("date", "")
            if (savedDate == today) {
                val ids = prefs.getStringSet("checked", emptySet()) ?: emptySet()
                _checked.value = ids
            } else {
                prefs.edit().putString("date", today).putStringSet("checked", emptySet()).apply()
            }
        }
    }

    fun toggle(id: String) {
        val current = _checked.value.toMutableSet()
        if (id in current) current.remove(id) else current.add(id)
        _checked.value = current
        prefs.edit().putStringSet("checked", current).apply()
    }
}

// ─── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun GoldHabitsScreen(vm: GoldHabitsViewModel = viewModel()) {
    val checked by vm.checked.collectAsState()
    val doneCount = checked.size
    val total = goldHabits.size

    val titles = stringArrayResource(R.array.gold_habit_titles)
    val subtitles = stringArrayResource(R.array.gold_habit_subtitles)

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF7F6FB)),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(R.string.habits_title), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
            Text(stringResource(R.string.habits_subtitle), fontSize = 14.sp, color = Color(0xFF6B6B8A))
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LinearProgressIndicator(
                    progress = { doneCount.toFloat() / total },
                    modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFFE8A020),
                    trackColor = Color(0xFFEAE8F5)
                )
                Text("$doneCount/$total", fontSize = 14.sp,
                    color = if (doneCount > 0) Color(0xFFE8A020) else Color(0xFF6B6B8A),
                    fontWeight = FontWeight.Bold)
            }

            if (doneCount == total) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFFF8ED))
                        .border(1.dp, Color(0xFFE8A020).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("🏆", fontSize = 28.sp)
                    Column {
                        Text(stringResource(R.string.habits_done_title), fontSize = 15.sp,
                            fontWeight = FontWeight.Bold, color = Color(0xFFE8A020))
                        Text(stringResource(R.string.habits_done_subtitle), fontSize = 13.sp, color = Color(0xFF6B6B8A))
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        itemsIndexed(goldHabits) { index, habit ->
            GoldHabitRow(
                habit = habit,
                title = titles.getOrElse(index) { "" },
                subtitle = subtitles.getOrElse(index) { "" },
                isDone = habit.id in checked,
                onToggle = { vm.toggle(habit.id) }
            )
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// ─── Habit Row ────────────────────────────────────────────────────────────────

@Composable
fun GoldHabitRow(habit: GoldHabit, title: String, subtitle: String, isDone: Boolean, onToggle: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = if (isDone) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "scale"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isDone) Color(0xFFFFF8ED) else Color.White)
            .border(1.dp, if (isDone) Color(0xFFE8A020).copy(alpha = 0.4f) else Color(0xFFEAE8F5), RoundedCornerShape(16.dp))
            .clickable(onClick = onToggle)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(habit.emoji, fontSize = 26.sp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontSize = 15.sp, fontWeight = FontWeight.Medium,
                color = if (isDone) Color(0xFFE8A020) else Color(0xFF1A1A2E),
                textDecoration = if (isDone) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
            )
            Text(subtitle, fontSize = 12.sp, color = Color(0xFF6B6B8A))
        }
        Box(
            modifier = Modifier.size(28.dp).clip(CircleShape)
                .background(if (isDone) Color(0xFFE8A020) else Color(0xFFEAE8F5)),
            contentAlignment = Alignment.Center
        ) {
            Text(if (isDone) "✓" else "", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}