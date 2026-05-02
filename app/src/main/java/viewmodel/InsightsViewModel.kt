package com.emotionalintelligence.emova.viewmodel

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.emotionalintelligence.emova.R
import com.emotionalintelligence.emova.data.EmováDatabase
import com.emotionalintelligence.emova.model.EmotionFamily
import com.emotionalintelligence.emova.model.emotionList
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.DateFormatSymbols
import java.util.*
import com.emotionalintelligence.emova.model.HabitRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DayData(val label: String, val count: Int, val color: Color)
data class TopEmotionData(val name: String, val emoji: String, val color: Color, val count: Int)

data class TriggerInsight(
    val emoji: String,
    val title: String,
    val description: String,
    val color: Color
)

data class PatternInsight(
    val emoji: String,
    val title: String,
    val description: String,
    val color: Color
)

enum class InsightsPeriod { WEEK, MONTH, ALL_TIME }

data class InsightsUiState2(
    val streakDays: Int = 0,
    val avgIntensity: Float = 0f,
    val triggerInsights: List<TriggerInsight> = emptyList(),
    val period: InsightsPeriod = InsightsPeriod.WEEK,
    val periodLabel: String = "This week",
    val weekOffset: Int = 0,

    // 🎯 MÉTRICAS ÉLITE
    val totalCheckIns: Int = 0,
    val positiveBalancePercentage: Int = 0,
    val topEmotion: TopEmotionData? = null, // 🎯 Corregido de Emotion? a TopEmotionData?

    val weekData: List<DayData> = emptyList(),
    val familyBreakdown: List<Triple<String, Color, Int>> = emptyList(),
    val patterns: List<PatternInsight> = emptyList()
)

class InsightsViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = EmováDatabase.getInstance(application).checkInDao()
    private val _uiState = MutableStateFlow(InsightsUiState2())
    val uiState: StateFlow<InsightsUiState2> = _uiState.asStateFlow()

    private fun str(resId: Int, vararg args: Any): String =
        getApplication<Application>().getString(resId, *args)

    init {
        viewModelScope.launch {
            // 🎯 Combinamos: CheckIns + Hábitos + Filtros
            combine(
                dao.getAllFlow(),
                EmováDatabase.getInstance(getApplication()).habitRecordDao().getAllFlow(), // 🎯 Escuchamos hábitos
                _uiState.map { it.weekOffset to it.period }.distinctUntilChanged()
            ) { checkIns, habits, (offset, period) ->
                // Le pasamos todo a compute
                compute(checkIns, habits, offset, period)
            }.collect() // El collect vacío es suficiente porque compute actualiza el _uiState
        }
    }

    fun setPeriod(period: InsightsPeriod) {
        _uiState.value = _uiState.value.copy(period = period, weekOffset = 0)
    }

    fun shiftWeek(delta: Int) {
        val newOffset = (_uiState.value.weekOffset + delta).coerceAtMost(0)
        _uiState.value = _uiState.value.copy(weekOffset = newOffset)
    }

    private fun compute(
        checkIns: List<com.emotionalintelligence.emova.model.CheckIn>,
        habits: List<com.emotionalintelligence.emova.model.HabitRecord>,
        weekOffset: Int,
        period: InsightsPeriod
    ) {
        val shortDays = DateFormatSymbols().shortWeekdays
        val shortMonths = DateFormatSymbols().shortMonths

        val dayResIds = mapOf(
            1 to R.string.day_sundays, 2 to R.string.day_mondays,
            3 to R.string.day_tuesdays, 4 to R.string.day_wednesdays,
            5 to R.string.day_thursdays, 6 to R.string.day_fridays,
            7 to R.string.day_saturdays
        )

        // ── 1. Rango del Período ──────────────────────────────────────────────
        // Usamos una estructura más segura para evitar errores de casting
        val (pStart, pEnd, pLabel, cDays) = when (period) {
            InsightsPeriod.WEEK -> {
                val end = Calendar.getInstance().apply {
                    add(Calendar.WEEK_OF_YEAR, weekOffset)
                    set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
                }.timeInMillis
                val start = Calendar.getInstance().apply {
                    add(Calendar.WEEK_OF_YEAR, weekOffset)
                    add(Calendar.DAY_OF_YEAR, -6)
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                }.timeInMillis
                val label = when (weekOffset) {
                    0  -> str(R.string.period_this_week)
                    -1 -> str(R.string.period_last_week)
                    else -> str(R.string.period_weeks_ago, -weekOffset)
                }
                val days = (6 downTo 0).map { daysAgo ->
                    val dayCal = Calendar.getInstance().apply {
                        add(Calendar.WEEK_OF_YEAR, weekOffset)
                        add(Calendar.DAY_OF_YEAR, -daysAgo)
                        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                    }
                    val dStart = dayCal.timeInMillis
                    val dEnd = dStart + 86_400_000L
                    val dCheckIns = checkIns.filter { it.timestamp in dStart until dEnd }
                    val topFamily = dCheckIns.groupBy { it.emotionFamily }.maxByOrNull { it.value.size }?.key
                    val color = try {
                        if (topFamily != null) EmotionFamily.valueOf(topFamily).color else Color(0xFFEAE8F5)
                    } catch (e: Exception) { Color(0xFFEAE8F5) }
                    DayData(label = shortDays[dayCal.get(Calendar.DAY_OF_WEEK)], count = dCheckIns.size, color = color)
                }
                // Retornamos como objetos específicos para evitar Any
                Quadruple(start, end, label, days)
            }
            InsightsPeriod.MONTH -> {
                val monthCal = Calendar.getInstance().apply {
                    add(Calendar.MONTH, weekOffset)
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                }
                val start = monthCal.timeInMillis
                val end = Calendar.getInstance().apply {
                    timeInMillis = start
                    set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                    set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
                }.timeInMillis
                val label = shortMonths[Calendar.getInstance().apply { add(Calendar.MONTH, weekOffset) }.get(Calendar.MONTH)]
                val weeks = (0 until 5).map { w ->
                    val wStart = start + w * 7 * 86_400_000L
                    val wEnd = (wStart + 7 * 86_400_000L).coerceAtMost(end)
                    val wCheckIns = checkIns.filter { it.timestamp in wStart until wEnd }
                    val topFamily = wCheckIns.groupBy { it.emotionFamily }.maxByOrNull { it.value.size }?.key
                    val color = try {
                        if (topFamily != null) EmotionFamily.valueOf(topFamily).color else Color(0xFFEAE8F5)
                    } catch (e: Exception) { Color(0xFFEAE8F5) }
                    DayData(label = "W${w + 1}", count = wCheckIns.size, color = color)
                }
                Quadruple(start, end, label, weeks)
            }
            InsightsPeriod.ALL_TIME -> {
                val start = 0L
                val end = Long.MAX_VALUE / 2
                val label = str(R.string.period_all_time)
                val months = (11 downTo 0).map { monthsAgo ->
                    val mCal = Calendar.getInstance().apply { add(Calendar.MONTH, -monthsAgo) }
                    val mStart = mCal.apply { set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0) }.timeInMillis
                    val mEnd = mStart + mCal.getActualMaximum(Calendar.DAY_OF_MONTH) * 86_400_000L
                    val mCheckIns = checkIns.filter { it.timestamp in mStart until mEnd }
                    val topFamily = mCheckIns.groupBy { it.emotionFamily }.maxByOrNull { it.value.size }?.key
                    val color = try {
                        if (topFamily != null) EmotionFamily.valueOf(topFamily).color else Color(0xFFEAE8F5)
                    } catch (e: Exception) { Color(0xFFEAE8F5) }
                    DayData(label = shortMonths[mCal.get(Calendar.MONTH)], count = mCheckIns.size, color = color)
                }
                Quadruple(start, end, label, months)
            }
        }

        val periodCheckIns = checkIns.filter { it.timestamp in pStart..pEnd }
        val chartData = cDays

        // ── 2. Métricas ───────────────────────────────────────────────────────
        val totalCount = periodCheckIns.size
        val positiveCount = periodCheckIns.count { it.emotionFamily.equals("POSITIVE", ignoreCase = true) }
        val balance = if (totalCount > 0) (positiveCount * 100) / totalCount else 0

        // Streak
        var streak = 0
        val checkDay = Calendar.getInstance()
        while (true) {
            val s = checkDay.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
            val e = s + 86_400_000L
            if (checkIns.any { it.timestamp in s until e }) { streak++; checkDay.add(Calendar.DAY_OF_YEAR, -1) } else break
        }

        // Top Emotion
        val topEmotion = periodCheckIns.groupBy { it.emotionName }.maxByOrNull { it.value.size }?.let { entry ->
            val sample = entry.value.first()
            val family = try { EmotionFamily.valueOf(sample.emotionFamily) } catch (e: Exception) { EmotionFamily.POSITIVE }
            val emotionObj = emotionList.find { it.name == sample.emotionName }
            TopEmotionData(emotionObj?.let { str(it.nameResId) } ?: sample.emotionName, emotionObj?.emoji ?: "😐", family.color, entry.value.size)
        }

        // Breakdown & Intensity
        val avgInt = if (periodCheckIns.isEmpty()) 0f else periodCheckIns.map { it.intensity }.average().toFloat()
        val bDown = periodCheckIns.groupBy { it.emotionFamily }.map { (fName, entries) ->
            val family = try { EmotionFamily.valueOf(fName) } catch (e: Exception) { null }
            Triple(family?.let { str(it.labelResId) } ?: fName, family?.color ?: Color.Gray, entries.size)
        }.sortedByDescending { it.third }

        // ── 3. PATRONES (Correlación Hábitos) ─────────────────────────────────
        val patterns = mutableListOf<PatternInsight>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val checkInsByDay = periodCheckIns.groupBy { dateFormat.format(Date(it.timestamp)) }
        val habitsByDay = habits.filter { it.timestamp in pStart..pEnd }.groupBy { dateFormat.format(Date(it.timestamp)) }

        var refDays = 0; var diffOnRef = 0
        var gratDays = 0; var posOnGrat = 0

        checkInsByDay.forEach { (date, dailyCheckIns) ->
            val dailyHabits = habitsByDay[date] ?: emptyList()

            // 🎯 Si en tu modelo de HabitRecord no se llama 'habitId', cámbialo aquí:
            if (dailyHabits.any { it.habitId == "reflection" }) {
                refDays++
                if (dailyCheckIns.any { (it.emotionFamily == "DIFFICULT" || it.emotionFamily == "ACTIVATED") && it.intensity >= 7 }) diffOnRef++
            }
            if (dailyHabits.any { it.habitId == "appreciation" }) {
                gratDays++
                if (dailyCheckIns.any { it.emotionFamily == "POSITIVE" }) posOnGrat++
            }
        }

        // Insights de Correlación (Prioridad Héroe)
        if (refDays >= 2 && diffOnRef == 0) {
            patterns.add(PatternInsight(
                emoji = "🛡️",
                title = str(R.string.pattern_shield_title),
                description = str(R.string.pattern_shield_desc),
                color = Color(0xFF6C63FF)
            ))
        } else if (gratDays >= 2 && posOnGrat >= (gratDays / 2)) {
            patterns.add(PatternInsight(
                emoji = "✨",
                title = str(R.string.pattern_gratitude_title),
                description = str(R.string.pattern_gratitude_desc),
                color = Color(0xFF06D6A0)
            ))
        }
        // --- Patrones de días (Hardest/Best) ---
        val byDayOfWeek = periodCheckIns.groupBy { Calendar.getInstance().apply { timeInMillis = it.timestamp }.get(Calendar.DAY_OF_WEEK) }

        byDayOfWeek.maxByOrNull { (_, entries) -> entries.count { it.emotionFamily == "DIFFICULT" || it.emotionFamily == "ACTIVATED" }.toDouble() / entries.size }?.let { (day, entries) ->
            if (entries.size >= 2 && (entries.count { it.emotionFamily == "DIFFICULT" || it.emotionFamily == "ACTIVATED" } * 100 / entries.size) > 50) {
                patterns.add(PatternInsight("📅", str(R.string.pattern_hard_day_title, str(dayResIds[day]!!)), str(R.string.pattern_hard_day_desc, (entries.count { it.emotionFamily == "DIFFICULT" || it.emotionFamily == "ACTIVATED" } * 100 / entries.size), str(dayResIds[day]!!).lowercase()), Color(0xFF4CC9F0)))
            }
        }

        // ── 4. Guardar Estado Final ───────────────────────────────────────────
        _uiState.value = InsightsUiState2(
            streakDays = streak,
            avgIntensity = avgInt,
            period = period,
            periodLabel = pLabel,
            weekOffset = weekOffset,
            totalCheckIns = totalCount,
            positiveBalancePercentage = balance,
            topEmotion = topEmotion,
            weekData = chartData,
            familyBreakdown = bDown,
            patterns = patterns
        )
    }

    // 🎯 Pequeño Helper para evitar el error de Any en el destructuring
    data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

}