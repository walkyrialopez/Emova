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
    val weekData: List<DayData> = emptyList(),
    val topEmotion: TopEmotionData? = null,
    val avgIntensity: Float = 0f,
    val familyBreakdown: List<Triple<String, Color, Int>> = emptyList(),
    val patterns: List<PatternInsight> = emptyList(),
    val triggerInsights: List<TriggerInsight> = emptyList(),
    val period: InsightsPeriod = InsightsPeriod.WEEK,
    val weekOffset: Int = 0,
    val periodLabel: String = ""
)

class InsightsViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = EmováDatabase.getInstance(application).checkInDao()
    private val _uiState = MutableStateFlow(InsightsUiState2())
    val uiState: StateFlow<InsightsUiState2> = _uiState.asStateFlow()

    // Helper that always picks up the current locale from Application context
    private fun str(resId: Int, vararg args: Any): String =
        getApplication<Application>().getString(resId, *args)

    init {
        viewModelScope.launch {
            combine(
                dao.getAllFlow(),
                _uiState.map { it.weekOffset to it.period }.distinctUntilChanged()
            ) { checkIns, (offset, period) ->
                checkIns to Pair(offset, period)
            }.collect { (checkIns, pair) ->
                val (offset, period) = pair
                compute(checkIns, offset, period)
            }
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
        weekOffset: Int,
        period: InsightsPeriod
    ) {
        // Locale-aware short day names (Sun, Mon, …) and month names
        val shortDays = DateFormatSymbols().shortWeekdays   // index 1=Sun … 7=Sat
        val shortMonths = DateFormatSymbols().shortMonths   // index 0=Jan … 11=Dec

        // Day-of-week resource IDs for pattern strings
        val dayResIds = mapOf(
            1 to R.string.day_sundays, 2 to R.string.day_mondays,
            3 to R.string.day_tuesdays, 4 to R.string.day_wednesdays,
            5 to R.string.day_thursdays, 6 to R.string.day_fridays,
            7 to R.string.day_saturdays
        )

        // ── Period range ──────────────────────────────────────────────────────
        val (periodStart, periodEnd, periodLabel, chartDays) = when (period) {
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
                        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                    }
                    val dayStart = dayCal.timeInMillis
                    val dayEnd = dayStart + 86_400_000L
                    // Use locale-aware short day name
                    val dayLabel = shortDays[dayCal.get(Calendar.DAY_OF_WEEK)]
                    val dayCheckIns = checkIns.filter { it.timestamp in dayStart until dayEnd }
                    val topFamily = dayCheckIns.groupBy { it.emotionFamily }.maxByOrNull { it.value.size }?.key
                    val color = try {
                        if (topFamily != null) EmotionFamily.valueOf(topFamily).color else Color(0xFFEAE8F5)
                    } catch (e: Exception) { Color(0xFFEAE8F5) }
                    DayData(label = dayLabel, count = dayCheckIns.size, color = color)
                }
                listOf(start, end, label, days)
            }
            InsightsPeriod.ALL_TIME -> {
                val start = 0L
                val end = Long.MAX_VALUE / 2
                val label = str(R.string.period_all_time)
                val months = (11 downTo 0).map { monthsAgo ->
                    val mCal = Calendar.getInstance().apply { add(Calendar.MONTH, -monthsAgo) }
                    val mStart = mCal.apply {
                        set(Calendar.DAY_OF_MONTH, 1)
                        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                    }.timeInMillis
                    val mEnd = mStart + mCal.getActualMaximum(Calendar.DAY_OF_MONTH) * 86_400_000L
                    val mCheckIns = checkIns.filter { it.timestamp in mStart until mEnd }
                    val topFamily = mCheckIns.groupBy { it.emotionFamily }.maxByOrNull { it.value.size }?.key
                    val color = try {
                        if (topFamily != null) EmotionFamily.valueOf(topFamily).color else Color(0xFFEAE8F5)
                    } catch (e: Exception) { Color(0xFFEAE8F5) }
                    val monthIndex = Calendar.getInstance().apply { add(Calendar.MONTH, -monthsAgo) }.get(Calendar.MONTH)
                    DayData(label = shortMonths[monthIndex], count = mCheckIns.size, color = color)
                }
                listOf(start, end, label, months)
            }
            InsightsPeriod.MONTH -> {
                val monthCal = Calendar.getInstance().apply {
                    add(Calendar.MONTH, weekOffset)
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                }
                val start = monthCal.timeInMillis
                val daysInMonth = monthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                val end = monthCal.apply {
                    set(Calendar.DAY_OF_MONTH, daysInMonth); set(Calendar.HOUR_OF_DAY, 23)
                }.timeInMillis
                val monthIndex = Calendar.getInstance().apply { add(Calendar.MONTH, weekOffset) }.get(Calendar.MONTH)
                val label = shortMonths[monthIndex]
                val weeks = (0 until 5).map { w ->
                    val wStart = start + w * 7 * 86_400_000L
                    val wEnd = (wStart + 7 * 86_400_000L).coerceAtMost(end)
                    val wCheckIns = checkIns.filter { it.timestamp in wStart until wEnd }
                    val topFamily = wCheckIns.groupBy { it.emotionFamily }.maxByOrNull { it.value.size }?.key
                    val color = try {
                        if (topFamily != null) EmotionFamily.valueOf(topFamily).color else Color(0xFFEAE8F5)
                    } catch (e: Exception) { Color(0xFFEAE8F5) }
                    DayData(label = "W${w + 1}", count = wCheckIns.size, color = color)
                }.take(5)
                listOf(start, end, label, weeks)
            }
        }

        @Suppress("UNCHECKED_CAST")
        val periodCheckIns = checkIns.filter {
            it.timestamp in (periodStart as Long)..(periodEnd as Long)
        }
        @Suppress("UNCHECKED_CAST")
        val chartData = chartDays as List<DayData>

        // ── Streak ────────────────────────────────────────────────────────────
        var streak = 0
        val checkDay = Calendar.getInstance()
        while (true) {
            val s = checkDay.apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val e = s + 86_400_000L
            if (checkIns.any { it.timestamp in s until e }) {
                streak++; checkDay.add(Calendar.DAY_OF_YEAR, -1)
            } else break
        }

        // ── Top emotion ───────────────────────────────────────────────────────
        val topEntry = periodCheckIns.groupBy { it.emotionName }.maxByOrNull { it.value.size }
        val topEmotion = topEntry?.let {
            val sample = it.value.first()
            val family = try { EmotionFamily.valueOf(sample.emotionFamily) } catch (e: Exception) { EmotionFamily.POSITIVE }
            val emotionObj = emotionList.find { e -> e.name == sample.emotionName }
            // Resolve localized name
            val localizedName = emotionObj?.let { e -> str(e.nameResId) } ?: sample.emotionName
            TopEmotionData(localizedName, emotionObj?.emoji ?: "😐", family.color, it.value.size)
        }

        // ── Avg intensity ─────────────────────────────────────────────────────
        val avgIntensity = if (periodCheckIns.isEmpty()) 0f
        else periodCheckIns.map { it.intensity }.average().toFloat()

        // ── Family breakdown ──────────────────────────────────────────────────
        val breakdown = periodCheckIns.groupBy { it.emotionFamily }
            .map { (familyName, entries) ->
                val family = try { EmotionFamily.valueOf(familyName) } catch (e: Exception) { null }
                val label = family?.let { str(it.labelResId) }
                    ?: familyName.lowercase().replaceFirstChar { it.uppercase() }
                val color = family?.color ?: Color.Gray
                Triple(label, color, entries.size)
            }.sortedByDescending { it.third }

        // ── Pattern insights ──────────────────────────────────────────────────
        val patterns = mutableListOf<PatternInsight>()
        val byDayOfWeek = checkIns.groupBy {
            Calendar.getInstance().apply { timeInMillis = it.timestamp }.get(Calendar.DAY_OF_WEEK)
        }

        // Hardest day
        val hardestDay = byDayOfWeek.maxByOrNull { (_, entries) ->
            entries.filter { it.emotionFamily == "DIFFICULT" || it.emotionFamily == "ACTIVATED" }.size.toDouble() / entries.size
        }
        if (hardestDay != null && hardestDay.value.size >= 2) {
            val dayName = str(dayResIds[hardestDay.key] ?: R.string.day_sundays)
            val pct = hardestDay.value.count { it.emotionFamily == "DIFFICULT" || it.emotionFamily == "ACTIVATED" } * 100 / hardestDay.value.size
            if (pct > 50) {
                patterns.add(PatternInsight("📅",
                    str(R.string.pattern_hard_day_title, dayName),
                    str(R.string.pattern_hard_day_desc, pct, dayName.lowercase()),
                    Color(0xFF4CC9F0)))
            }
        }

        // Best day
        val bestDay = byDayOfWeek.maxByOrNull { (_, entries) ->
            entries.filter { it.emotionFamily == "POSITIVE" }.size.toDouble() / entries.size
        }
        if (bestDay != null && bestDay.value.size >= 2) {
            val dayName = str(dayResIds[bestDay.key] ?: R.string.day_sundays)
            val pct = bestDay.value.count { it.emotionFamily == "POSITIVE" } * 100 / bestDay.value.size
            if (pct > 60) {
                patterns.add(PatternInsight("✨",
                    str(R.string.pattern_best_day_title, dayName),
                    str(R.string.pattern_best_day_desc, pct, dayName.lowercase()),
                    Color(0xFFE8A020)))
            }
        }

        // Streak
        if (streak >= 3) {
            patterns.add(PatternInsight("🔥",
                str(R.string.pattern_streak_title, streak),
                str(R.string.pattern_streak_desc, streak),
                Color(0xFF6C63FF)))
        }

        // High intensity
        if (periodCheckIns.size >= 3) {
            val highIntensity = periodCheckIns.filter { it.intensity >= 7 }
            if (highIntensity.size >= 2) {
                patterns.add(PatternInsight("⚡",
                    str(R.string.pattern_high_intensity_title),
                    str(R.string.pattern_high_intensity_desc),
                    Color(0xFFEF233C)))
            }
        }

        // Mostly positive
        if (periodCheckIns.size >= 2) {
            val positivePct = periodCheckIns.count { it.emotionFamily == "POSITIVE" } * 100 / periodCheckIns.size
            if (positivePct > 60) {
                patterns.add(PatternInsight("🌱",
                    str(R.string.pattern_mostly_positive_title),
                    str(R.string.pattern_mostly_positive_desc, positivePct),
                    Color(0xFF06D6A0)))
            }
        }

        // ── Trigger Insights ──────────────────────────────────────────────────
        val triggerInsights = mutableListOf<TriggerInsight>()
        if (periodCheckIns.any { it.intensity >= 8 }) {
            triggerInsights.add(TriggerInsight("⚡",
                str(R.string.pattern_high_intensity_title),
                "You've recorded high-intensity emotions recently. Reflect on the context of these entries.",
                Color(0xFFEF233C)))
        }
        if (periodCheckIns.count { it.emotionFamily == "ACTIVATED" } >= 2) {
            triggerInsights.add(TriggerInsight("🧘",
                "High Activation",
                "Your entries show high energy levels. Physical movement or breathwork might help.",
                Color(0xFF7209B7)))
        }

        _uiState.value = InsightsUiState2(
            streakDays = streak,
            weekData = chartData,
            topEmotion = topEmotion,
            avgIntensity = avgIntensity,
            familyBreakdown = breakdown,
            patterns = patterns,
            triggerInsights = triggerInsights,
            period = period,
            weekOffset = weekOffset,
            periodLabel = periodLabel as String
        )
    }
}