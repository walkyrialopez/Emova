package com.emotionalintelligence.emova.ui.today

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.emotionalintelligence.emova.R
import com.emotionalintelligence.emova.data.EmováDatabase
import com.emotionalintelligence.emova.ui.habits.GoldHabitsViewModel
import com.emotionalintelligence.emova.ui.today.WellnessTipOfTheDay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

// ─── ViewModel ────────────────────────────────────────────────────────────────

data class TodayUiState(
    val greeting: String = "",
    val checkInDone: Boolean = false,
    val checkInTime: String = "",
    val gratitudeDone: Boolean = false,
    val gratitudeCount: Int = 0,
    val breathingDone: Boolean = false,
    val lessonDone: Boolean = false,
    val streakDays: Int = 0
)

class TodayViewModel(application: android.app.Application) : AndroidViewModel(application) {
    private val db = EmováDatabase.getInstance(application)
    private val prefs = application.getSharedPreferences("emova_today", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val todayStart = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .parse(today)?.time ?: 0L
            val todayEnd = todayStart + 86_400_000L

            val checkIns = db.checkInDao().getAllFlow().first()
            val todayCheckIn = checkIns.firstOrNull { it.timestamp in todayStart until todayEnd }

            val gratitude = db.gratitudeEntryDao().getByDate(today)
            val gratitudeItems = gratitude?.items?.split("||")?.filter { it.isNotBlank() } ?: emptyList()

            val breathingDate = prefs.getString("breathing_date", "")
            val breathingDone = breathingDate == today
            val lessonDate = prefs.getString("lesson_date", "")
            val lessonDone = lessonDate == today

            var streak = 0
            val cal = Calendar.getInstance()
            while (true) {
                val dayStart = cal.apply {
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                val dayEnd = dayStart + 86_400_000L
                if (checkIns.any { it.timestamp in dayStart until dayEnd }) {
                    streak++
                    cal.add(Calendar.DAY_OF_YEAR, -1)
                } else break
            }

            // Store a key so the UI can resolve to the correct locale string
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val greetingKey = when {
                hour < 12 -> "morning"
                hour < 17 -> "afternoon"
                hour < 21 -> "evening"
                else      -> "late"
            }

            val timeLabel = todayCheckIn?.let {
                SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(it.timestamp))
            } ?: ""

            _uiState.value = TodayUiState(
                greeting = greetingKey,
                checkInDone = todayCheckIn != null,
                checkInTime = timeLabel,
                gratitudeDone = gratitudeItems.size >= 10,
                gratitudeCount = gratitudeItems.size,
                breathingDone = breathingDone,
                lessonDone = lessonDone,
                streakDays = streak
            )
        }
    }

    fun markLessonDone() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        prefs.edit().putString("lesson_date", today).apply()
        _uiState.value = _uiState.value.copy(lessonDone = true)
    }

    fun markBreathingDone() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        prefs.edit().putString("breathing_date", today).apply()
        _uiState.value = _uiState.value.copy(breathingDone = true)
    }
}

// ─── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun TodayScreen(
    refreshKey: Any = Unit,
    onNavigateToCheckIn: () -> Unit,
    onNavigateToGratitude: () -> Unit,
    onNavigateToBreathe: () -> Unit,
    onNavigateToLesson: () -> Unit,
    onNavigateToHabits: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},   // 👈 add this
    vm: TodayViewModel = viewModel()
) {
    val context = LocalContext.current
    val userName = remember {
        context.getSharedPreferences("emova_prefs", android.content.Context.MODE_PRIVATE)
            .getString("user_name", "") ?: ""
    }
    val state by vm.uiState.collectAsState()

    // Hoist habitsVm here so we can include micro habits in the counter
    val habitsVm: GoldHabitsViewModel = viewModel()
    val habitsChecked by habitsVm.checked.collectAsState()
    val microHabitsDone = habitsChecked.size >= 12

    LaunchedEffect(refreshKey) { vm.refresh() }

    val allDone = state.checkInDone && state.gratitudeDone &&
            state.breathingDone && state.lessonDone && microHabitsDone
    val doneCount = listOf(
        state.checkInDone, state.gratitudeDone,
        state.breathingDone, state.lessonDone, microHabitsDone
    ).count { it }
    val totalHabits = 5

    // Resolve greeting key to localized string
    val greetingText = when (state.greeting) {
        "morning"   -> stringResource(R.string.greeting_morning)
        "afternoon" -> stringResource(R.string.greeting_afternoon)
        "evening"   -> stringResource(R.string.greeting_evening)
        else        -> stringResource(R.string.greeting_late)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F6FB))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 32.dp)
        ) {
            // Greeting row with settings icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (userName.isNotBlank()) "$greetingText, $userName" else greetingText,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A2E)
                    )
                    Text(
                        text = if (allDone) stringResource(R.string.today_subtitle_all_done)
                        else stringResource(R.string.today_subtitle_default),
                        fontSize = 14.sp, color = Color(0xFF6B6B8A)
                    )
                }
                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEAE8F5))
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        tint = Color(0xFF6B6B8A),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Progress + streak row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Progress card
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFEAE8F5), RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$doneCount/$totalHabits",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (allDone) Color(0xFF6C63FF) else Color(0xFF1A1A2E)
                    )
                    Text(stringResource(R.string.today_label), fontSize = 13.sp, color = Color(0xFF6B6B8A))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            state.checkInDone, state.gratitudeDone,
                            state.breathingDone, state.lessonDone, microHabitsDone
                        ).forEach { done ->
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (done) Color(0xFF6C63FF) else Color(0xFFEAE8F5))
                            )
                        }
                    }
                }

                // Streak card
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFEAE8F5), RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🔥", fontSize = 28.sp)
                    Text(
                        text = "${state.streakDays}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A2E)
                    )
                    Text(
                        text = stringResource(R.string.today_day_streak),
                        fontSize = 13.sp,
                        color = Color(0xFF6B6B8A)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            WellnessTipOfTheDay()
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                stringResource(R.string.today_habits_title),
                fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E)
            )
            Spacer(modifier = Modifier.height(12.dp))

            HabitCard(
                emoji = "💜",
                title = stringResource(R.string.habit_checkin_title),
                subtitle = if (state.checkInDone)
                    stringResource(R.string.habit_checkin_done, state.checkInTime)
                else stringResource(R.string.habit_checkin_pending),
                doneClickable = true,
                done = state.checkInDone,
                color = Color(0xFF6C63FF),
                onClick = onNavigateToCheckIn
            )
            Spacer(modifier = Modifier.height(10.dp))

            HabitCard(
                emoji = "🌟",
                title = stringResource(R.string.habit_gratitude_title),
                subtitle = when {
                    state.gratitudeDone -> stringResource(R.string.habit_gratitude_done)
                    state.gratitudeCount > 0 -> stringResource(R.string.habit_gratitude_progress, state.gratitudeCount)
                    else -> stringResource(R.string.habit_gratitude_pending)
                },
                done = state.gratitudeDone,
                color = Color(0xFFE8A020),
                onClick = onNavigateToGratitude
            )
            Spacer(modifier = Modifier.height(10.dp))

            HabitCard(
                emoji = "🌬️",
                title = stringResource(R.string.habit_breathing_title),
                subtitle = if (state.breathingDone)
                    stringResource(R.string.habit_breathing_done)
                else stringResource(R.string.habit_breathing_pending),
                done = state.breathingDone,
                color = Color(0xFF4CC9F0),
                onClick = { vm.markBreathingDone(); onNavigateToBreathe() }
            )
            Spacer(modifier = Modifier.height(10.dp))

            HabitCard(
                emoji = "⭐",
                title = stringResource(R.string.habit_lesson_title),
                subtitle = if (state.lessonDone)
                    stringResource(R.string.habit_lesson_done)
                else stringResource(R.string.habit_lesson_pending),
                done = state.lessonDone,
                color = Color(0xFF6C63FF),
                onClick = { vm.markLessonDone(); onNavigateToLesson() }
            )

            Spacer(modifier = Modifier.height(20.dp))
            MicroHabitsCard(onNavigateToHabits = onNavigateToHabits, habitsVm = habitsVm)

            if (allDone) {
                Spacer(modifier = Modifier.height(28.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF6C63FF).copy(alpha = 0.08f))
                        .border(1.dp, Color(0xFF6C63FF).copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🏆", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.today_all_done_title),
                            fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6C63FF)
                        )
                        Text(
                            stringResource(R.string.today_all_done_subtitle),
                            fontSize = 13.sp, color = Color(0xFF6B6B8A)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        } // end Column

        // Bottom fade to hint scrollable content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xFFF7F6FB))
                    )
                )
        )
    } // end Box
}

// ─── Micro Habits Card ────────────────────────────────────────────────────────

@Composable
fun MicroHabitsCard(
    onNavigateToHabits: () -> Unit,
    habitsVm: GoldHabitsViewModel = viewModel()
) {
    val checked by habitsVm.checked.collectAsState()
    val total = 12
    val done = checked.size

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFFFF8ED))
            .border(1.dp, Color(0xFFE8A020).copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .clickable(onClick = onNavigateToHabits)
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("✨", fontSize = 26.sp)
                Column {
                    Text(
                        stringResource(R.string.micro_habits_title),
                        fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E)
                    )
                    Text(
                        text = when {
                            done == 0    -> stringResource(R.string.micro_habits_pending)
                            done == total -> stringResource(R.string.micro_habits_all_done, total)
                            else          -> stringResource(R.string.micro_habits_progress, done, total)
                        },
                        fontSize = 13.sp, color = Color(0xFF6B6B8A)
                    )
                }
            }
            Text("→", fontSize = 18.sp, color = Color(0xFFE8A020), fontWeight = FontWeight.Bold)
        }
        if (done > 0) {
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { done.toFloat() / total },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color(0xFFE8A020),
                trackColor = Color(0xFFEAE8F5)
            )
        }
    }
}

// ─── Habit Card ───────────────────────────────────────────────────────────────

@Composable
fun HabitCard(
    emoji: String,
    title: String,
    subtitle: String,
    done: Boolean,
    color: Color,
    doneClickable: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(
                width = if (done) 1.5.dp else 1.dp,
                color = if (done) color.copy(alpha = 0.4f) else Color(0xFFEAE8F5),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(enabled = !done || doneClickable, onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (done) color.copy(alpha = 0.12f) else Color(0xFFF0F0F8)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (done) "✓" else emoji,
                fontSize = if (done) 20.sp else 22.sp,
                color = if (done) color else Color.Unspecified
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title, fontSize = 15.sp, fontWeight = FontWeight.Bold,
                color = if (done) Color(0xFF6B6B8A) else Color(0xFF1A1A2E)
            )
            Text(subtitle, fontSize = 13.sp, color = Color(0xFF6B6B8A), lineHeight = 18.sp)
        }
        if (!done) {
            Text("→", fontSize = 18.sp, color = color)
        }
    }
}