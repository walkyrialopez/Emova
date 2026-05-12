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
import androidx.compose.ui.draw.shadow
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
import com.emotionalintelligence.emova.util.AppLanguage
import com.emotionalintelligence.emova.util.loadLanguage
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.draw.alpha

// --- UiState ---
data class TodayUiState(
    val greeting: String = "",
    val formattedDate: String = "",
    val checkInDone: Boolean = false,
    val checkInTime: String = "",
    val gratitudeDone: Boolean = false,
    val gratitudeCount: Int = 0,
    val breathingDone: Boolean = false,
    val lessonDone: Boolean = false,
    val todayEmotionName: String = "",
    val todayEmotionEmoji: String = "",
    val todayEmotionIntensity: Int = 0,
    val streakDays: Int = 0
)

// --- ViewModel ---
class TodayViewModel(application: android.app.Application) : AndroidViewModel(application) {
    private val db    = EmováDatabase.getInstance(application)
    private val prefs = application.getSharedPreferences("emova_today", Context.MODE_PRIVATE)
    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            val appLanguage = loadLanguage(getApplication())
            val today       = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val todayStart  = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(today)?.time ?: 0L
            val todayEnd    = todayStart + 86_400_000L

            val checkIns      = db.checkInDao().getAllFlow().first()
            val todayCheckIn  = checkIns.firstOrNull { it.timestamp in todayStart until todayEnd }

            val gratitude      = db.gratitudeEntryDao().getByDate(today)
            val gratitudeItems = gratitude?.items?.split("||")?.filter { it.isNotBlank() } ?: emptyList()

            val breathingDone = prefs.getString("breathing_date", "") == today
            val lessonDone    = prefs.getString("lesson_date", "") == today

            var streak = 0
            val cal = Calendar.getInstance()
            while (true) {
                val s = cal.apply {
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0);      set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                val e = s + 86_400_000L
                if (checkIns.any { it.timestamp in s until e }) {
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

            val (datePattern, dateLocale) = when (appLanguage) {
                AppLanguage.SPANISH -> "EEEE, d 'de' MMMM" to Locale("es", "ES")
                else                -> "EEEE, MMMM d"      to Locale.ENGLISH
            }
            val formattedDate = SimpleDateFormat(datePattern, dateLocale).format(Date())

            val timeLabel = todayCheckIn?.let {
                SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(it.timestamp))
            } ?: ""

            _uiState.value = TodayUiState(
                greeting              = greetingKey,
                formattedDate         = formattedDate,
                checkInDone           = todayCheckIn != null,
                checkInTime           = timeLabel,
                gratitudeDone         = gratitudeItems.isNotEmpty(),
                gratitudeCount        = gratitudeItems.size,
                breathingDone         = breathingDone,
                lessonDone            = lessonDone,
                todayEmotionName      = todayCheckIn?.emotionName ?: "",
                todayEmotionEmoji     = "",
                todayEmotionIntensity = todayCheckIn?.intensity ?: 0,
                streakDays            = streak
            )
        }
    }

    fun markBreathingDone() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        prefs.edit().putString("breathing_date", today).apply()
        _uiState.value = _uiState.value.copy(breathingDone = true)
    }

    fun markLessonDone() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        prefs.edit().putString("lesson_date", today).apply()
        _uiState.value = _uiState.value.copy(lessonDone = true)
    }
}

// --- Screen ---
@Composable
fun TodayScreen(
    refreshKey: Any = Unit,
    onNavigateToCheckIn: () -> Unit,
    onNavigateToGratitude: () -> Unit,
    onNavigateToBreathe: () -> Unit,
    onNavigateToLesson: () -> Unit,
    onNavigateToHabits: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    vm: TodayViewModel = viewModel()
) {
    val context  = LocalContext.current
    val state    by vm.uiState.collectAsState()
    val userName = remember {
        context.getSharedPreferences("emova_prefs", Context.MODE_PRIVATE)
            .getString("user_name", "") ?: ""
    }

    val greetingText = when (state.greeting) {
        "morning"   -> stringResource(R.string.greeting_morning)
        "afternoon" -> stringResource(R.string.greeting_afternoon)
        "evening"   -> stringResource(R.string.greeting_evening)
        else        -> stringResource(R.string.greeting_late)
    }

    // 1. Buscamos la emoción completa en tu lista
    val currentEmotion = remember(state.todayEmotionName) {
        com.emotionalintelligence.emova.model.emotionList
            .firstOrNull { it.name == state.todayEmotionName }
    }

    val todayEmoji = currentEmotion?.emoji ?: "✨"

    val displayEmotionName = currentEmotion?.let { stringResource(it.nameResId) } ?: state.todayEmotionName

    val coreHabits = listOf(state.checkInDone, state.gratitudeDone, state.breathingDone, state.lessonDone)
    val doneCoreCount = coreHabits.count { it }

    LaunchedEffect(refreshKey) { vm.refresh() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F6FB))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp) // Alineación más amplia y limpia
            .padding(top = 32.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp) // Espaciado unificado
    ) {
        // 1. Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (userName.isNotBlank()) "$greetingText, $userName" else greetingText,
                    fontSize = 24.sp, // Ligeramente más grande para jerarquía
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A2E)
                )
                Text(
                    text = state.formattedDate,
                    fontSize = 14.sp,
                    color = Color(0xFF6B6B8A)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.streakDays > 1) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0xFFFFF8EC))
                            .border(1.dp, Color(0xFFFFD166).copy(alpha = 0.5f), RoundedCornerShape(999.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("🔥", fontSize = 14.sp)
                        Text(
                            text = stringResource(R.string.today_streak_days, state.streakDays),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E)
                        )
                    }
                }
                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier
                        .size(40.dp)
                        .shadow(elevation = 2.dp, shape = CircleShape, spotColor = Color(0x1A000000))
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Icon(
                        Icons.Outlined.Settings,
                        contentDescription = null,
                        tint = Color(0xFF6B6B8A),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 2. Emotion Hero / Check-in CTA (Radio unificado de 24dp)
        if (state.checkInDone && state.todayEmotionName.isNotBlank()) {
            EmotionHeroCard(
                emoji     = todayEmoji,
                name      = state.todayEmotionName,
                intensity = state.todayEmotionIntensity,
                time      = state.checkInTime
            )
        } else {
            CheckInCtaCard(onClick = onNavigateToCheckIn)
        }

        //Progress Section
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.today_progress_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A2E)
                )
                Text(
                    text = stringResource(R.string.today_progress_count, doneCoreCount, coreHabits.size),
                    fontSize = 14.sp,
                    color = if (doneCoreCount > 0) Color(0xFF6C63FF) else Color(0xFF6B6B8A),
                    fontWeight = FontWeight.Bold
                )
            }

            val animProgress by animateFloatAsState(
                targetValue = doneCoreCount.toFloat() / coreHabits.size,
                animationSpec = tween(600, easing = EaseOutQuart),
                label = "progress"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp) // Barra más orgánica y gruesa
                    .clip(CircleShape)
                    .background(Color(0xFFEAE8F5))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animProgress)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(Color(0xFF6C63FF))
                )
            }
        }
        // 5. Core Habit Action List
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            HabitActionRow(
                emoji     = "📝",
                iconBg    = Color(0xFFEEEDFE),
                title     = stringResource(R.string.habit_checkin_title),
                subtitle  = if (state.checkInDone)
                    stringResource(R.string.habit_checkin_done, state.todayEmotionName, state.checkInTime)
                else
                    stringResource(R.string.habit_checkin_cta),
                isDone    = state.checkInDone,
                accentColor = Color(0xFF6C63FF),
                onClick   = onNavigateToCheckIn
            )

            HabitActionRow(
                emoji     = "💛",
                iconBg    = Color(0xFFFFF8EC),
                title     = stringResource(R.string.habit_gratitude_title),
                subtitle  = if (state.gratitudeDone)
                    stringResource(R.string.habit_gratitude_done, state.gratitudeCount)
                else
                    stringResource(R.string.habit_gratitude_cta),
                isDone    = state.gratitudeDone,
                accentColor = Color(0xFFE8A020),
                onClick   = onNavigateToGratitude
            )

            HabitActionRow(
                emoji     = "🌬️",
                iconBg    = Color(0xFFE1F5EE),
                title     = stringResource(R.string.habit_breathing_title),
                subtitle  = if (state.breathingDone)
                    stringResource(R.string.habit_breathing_done)
                else
                    stringResource(R.string.habit_breathing_cta),
                isDone    = state.breathingDone,
                accentColor = Color(0xFF4CC9F0),
                onClick   = { vm.markBreathingDone(); onNavigateToBreathe() }
            )

            HabitActionRow(
                emoji     = "📚",
                iconBg    = Color(0xFFEEEDFE),
                title     = stringResource(R.string.habit_lesson_title),
                subtitle  = if (state.lessonDone)
                    stringResource(R.string.habit_lesson_done)
                else
                    stringResource(R.string.habit_lesson_cta),
                isDone    = state.lessonDone,
                accentColor = Color(0xFF6C63FF),
                onClick   = { vm.markLessonDone(); onNavigateToLesson() }
            )
        }

        // 6. Micro-habits shortcut
        MicroHabitsShortcut(onClick = onNavigateToHabits)

        // 7. All done trophy
        if (doneCoreCount == coreHabits.size) {
            AllDoneCard()
        }

        // 3. Daily Tip
        DailyTipCard()
    }
}

@Composable
fun EmotionHeroCard(emoji: String, name: String, intensity: Int, time: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(24.dp), spotColor = Color(0x266C63FF))
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFFEEEDFE), Color(0xFFE1F5EE))
                )
            )
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 28.sp)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A2E)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Intensity $intensity • $time",
                fontSize = 13.sp,
                color = Color(0xFF6B6B8A)
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(Color.White.copy(alpha = 0.6f))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "Done",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6C63FF)
            )
        }
    }
}

@Composable
fun CheckInCtaCard(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(24.dp), spotColor = Color(0x336C63FF))
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF6C63FF))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("✨", fontSize = 32.sp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.today_checkin_cta_title),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.today_checkin_cta_subtitle),
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "→",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun DailyTipCard() {
    val titles = androidx.compose.ui.res.stringArrayResource(R.array.wellness_tip_titles)
    val bodies = androidx.compose.ui.res.stringArrayResource(R.array.wellness_tip_bodies)
    val index  = remember { Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % titles.size }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFFFF8EC))
            // El borde ultrafino mantiene la elegancia sin endurecer la interfaz
            .border(0.5.dp, Color(0xFFFFD166).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Text("💡", fontSize = 18.sp)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.wellness_tip_label).uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFE8A020),
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = titles.getOrElse(index) { "" },
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A2E)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = bodies.getOrElse(index) { "" },
                fontSize = 13.sp,
                color = Color(0xFF6B6B8A),
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun HabitActionRow(
    emoji: String,
    iconBg: Color,
    title: String,
    subtitle: String,
    isDone: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    // ESTADO APAGADO: Cambia drásticamente la jerarquía visual de lo completado
    val cardBg = if (isDone) Color.Transparent else Color.White
    val titleColor = if (isDone) Color(0xFFAAAAAC) else Color(0xFF1A1A2E)
    val subtitleColor = if (isDone) Color(0xFFAAAAAC).copy(alpha = 0.7f) else Color(0xFF6B6B8A)
    val iconBoxBg = if (isDone) Color(0xFFEAE8F5).copy(alpha = 0.5f) else iconBg

    Row(
        modifier = Modifier
            .fillMaxWidth()
            // 1. LA SOMBRA PRIMERO: Así queda por debajo de la tarjeta y no ensucia el interior
            .then(
                if (!isDone) Modifier.shadow(
                    elevation = 3.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = Color(0x1A000000),
                    ambientColor = Color(0x1A000000)
                ) else Modifier
            )
            // 2. EL FONDO DESPUÉS: Dibuja el blanco sólido tapando la sombra interior
            .background(color = cardBg, shape = RoundedCornerShape(20.dp))
            // 3. EL BORDE
            .border(
                width = 1.dp,
                color = if (isDone) Color(0xFFEAE8F5) else Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            )
            // 4. CLIP Y CLIC: Para que al tocar la tarjeta, el efecto gris de pulsación no se salga de las curvas
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            // 5. PADDING INTERNO: El espacio entre el borde de la tarjeta y el texto
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBoxBg),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 22.sp, modifier = Modifier.alpha(if (isDone) 0.5f else 1f))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = titleColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = subtitleColor,
                lineHeight = 18.sp
            )
        }

        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (isDone) accentColor.copy(alpha = 0.6f) else Color(0xFFF7F6FB))
                .border(
                    width = if (isDone) 0.dp else 1.5.dp,
                    color = if (isDone) Color.Transparent else Color(0xFFD0D0E0),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isDone) {
                Text("✓", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun MicroHabitsShortcut(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFEAE8F5), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFF8EC)),
                contentAlignment = Alignment.Center
            ) {
                Text("🌱", fontSize = 20.sp)
            }
            Column {
                Text(
                    text = stringResource(R.string.today_microhabits_title),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A2E)
                )
                Text(
                    text = stringResource(R.string.today_microhabits_subtitle),
                    fontSize = 13.sp,
                    color = Color(0xFF6B6B8A)
                )
            }
        }
        Text(
            text = "→",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE8A020)
        )
    }
}

@Composable
fun AllDoneCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF6C63FF).copy(alpha = 0.08f))
            .border(1.dp, Color(0xFF6C63FF).copy(alpha = 0.2f), RoundedCornerShape(24.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("🏆", fontSize = 42.sp)
        Text(
            text = stringResource(R.string.today_all_done_title),
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF6C63FF)
        )
        Text(
            text = stringResource(R.string.today_all_done_subtitle),
            fontSize = 14.sp,
            color = Color(0xFF6B6B8A),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}