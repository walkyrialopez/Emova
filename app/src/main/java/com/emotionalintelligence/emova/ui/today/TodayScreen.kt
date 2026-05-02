package com.emotionalintelligence.emova.ui.today

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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
                gratitudeDone = gratitudeItems.isNotEmpty(),
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
    habitsVm: GoldHabitsViewModel = viewModel(),
    onNavigateToHabits: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    vm: TodayViewModel = viewModel()
) {
    val context = LocalContext.current

    val userName = remember {
        context.getSharedPreferences("emova_prefs", Context.MODE_PRIVATE)
            .getString("user_name", "") ?: ""
    }
    val state by vm.uiState.collectAsState()
    val habitsChecked by habitsVm.checked.collectAsState()
    val microHabitsDone = habitsChecked.size >= 12
    LaunchedEffect(refreshKey) { vm.refresh() }

    val allDone = state.checkInDone && state.gratitudeDone &&
            state.breathingDone && state.lessonDone && microHabitsDone

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
            // --- HEADER ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (userName.isNotBlank()) "$greetingText, $userName" else greetingText,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A2E)
                    )
                    // Agregamos la fecha actual para dar contexto y calma
                    Text(
                        text = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES")).format(Date()),
                        fontSize = 14.sp, color = Color(0xFF6B6B8A)
                    )
                }
                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White)
                ) {
                    Icon(Icons.Outlined.Settings, null, tint = Color(0xFF6B6B8A), modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // --- HÁBITOS PRINCIPALES ---
            Text(
                stringResource(R.string.today_habits_title),
                fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Fila 1
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CompactHabitCard(
                    modifier = Modifier.weight(1f),
                    emoji = "💜", title = stringResource(R.string.habit_checkin_title),
                    done = state.checkInDone, color = Color(0xFF6C63FF),
                    onClick = onNavigateToCheckIn
                )
                CompactHabitCard(
                    modifier = Modifier.weight(1f),
                    emoji = "🌟", title = stringResource(R.string.habit_gratitude_title),
                    done = state.gratitudeDone, color = Color(0xFFE8A020),
                    onClick = onNavigateToGratitude
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Fila 2
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CompactHabitCard(
                    modifier = Modifier.weight(1f),
                    emoji = "🌬️", title = stringResource(R.string.habit_breathing_title),
                    done = state.breathingDone, color = Color(0xFF4CC9F0),
                    onClick = { vm.markBreathingDone(); onNavigateToBreathe() }
                )
                CompactHabitCard(
                    modifier = Modifier.weight(1f),
                    emoji = "⭐", title = stringResource(R.string.habit_lesson_title),
                    done = state.lessonDone, color = Color(0xFF6C63FF),
                    onClick = { vm.markLessonDone(); onNavigateToLesson() }
                )
            }

            // --- TROFEO DE "TODO LISTO" ---
            if (allDone) {
                Spacer(modifier = Modifier.height(32.dp))
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

            Spacer(modifier = Modifier.height(20.dp))

            // --- INTENCIONES DIARIAS ---
            Text(
                text = stringResource(R.string.today_intentions_title), // "Daily Intentions"
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A2E),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFEAE8F5), RoundedCornerShape(20.dp))
                    .padding(vertical = 8.dp)
            ) {
                val intentions = listOf(
                    "appreciation" to stringResource(R.string.intention_appreciation),
                    "focus" to stringResource(R.string.intention_focus), // Asegúrate de añadir este string!
                    "reflection" to stringResource(R.string.intention_reflection)
                )

                intentions.forEach { (id, label) ->
                    val isDone = id in habitsChecked
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { habitsVm.toggle(id) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (isDone) Color(0xFF6C63FF) else Color(0xFFF7F6FB))
                                .border(1.dp, if (isDone) Color(0xFF6C63FF) else Color(0xFFEAE8F5), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isDone) Text("✓", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = label, modifier = Modifier.padding(start = 14.dp),
                            fontSize = 15.sp, color = if (isDone) Color(0xFFAAAAAC) else Color(0xFF1A1A2E),
                            textDecoration = if (isDone) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                        )
                    }
                }

                Divider(color = Color(0xFFF0F0F8), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateToHabits)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.view_all_habits), fontSize = 14.sp, color = Color(0xFF6B6B8A), fontWeight = FontWeight.Medium)
                    Text("→", fontSize = 18.sp, color = Color(0xFFE8A020), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 🎯 EL TIP DEL DÍA: El protagonista sutil antes de las tareas
            WellnessTipOfTheDayToday()

            Spacer(modifier = Modifier.height(80.dp)) // Espacio final para que no tape el menú inferior
        }

        // Sombra inferior
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color(0xFFF7F6FB))))
        )
    }
}

// ─── Tarjeta Compacta (Cuadrada) ──────────────────────────────────────────────
@Composable
fun CompactHabitCard(
    modifier: Modifier = Modifier,
    emoji: String, title: String,
    done: Boolean, color: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(
                width = if (done) 1.5.dp else 1.dp,
                color = if (done) color.copy(alpha = 0.4f) else Color(0xFFEAE8F5),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(if (done) color.copy(alpha = 0.12f) else Color(0xFFF0F0F8)),
            contentAlignment = Alignment.Center
        ) {
            Text(if (done) "✓" else emoji, fontSize = 20.sp, color = if (done) color else Color.Unspecified)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold,
            color = if (done) Color(0xFF6B6B8A) else Color(0xFF1A1A2E)
        )
    }
}

@Composable
fun WellnessTipOfTheDayToday() {
    // Aquí podrías luego conectar esto a una base de datos de tips
    val tipTitle = stringResource(R.string.tip_title_default) // "Daily Insight"
    val tipBody = stringResource(R.string.tip_body_default) // "Naming an emotion reduces its power over you."

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF6C63FF).copy(alpha = 0.05f), Color(0xFF4CC9F0).copy(alpha = 0.05f))
                )
            )
            .border(1.dp, Color(0xFF6C63FF).copy(alpha = 0.1f), RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Text("💡", fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = tipTitle,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6C63FF),
                letterSpacing = 1.sp
            )
            Text(
                text = tipBody,
                fontSize = 14.sp,
                color = Color(0xFF1A1A2E),
                lineHeight = 20.sp
            )
        }
    }
}