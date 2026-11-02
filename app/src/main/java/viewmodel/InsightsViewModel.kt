package com.emotionalintelligence.emova.viewmodel

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.emotionalintelligence.emova.R
import com.emotionalintelligence.emova.data.EmováDatabase
import com.emotionalintelligence.emova.model.CheckIn
import com.emotionalintelligence.emova.model.EmotionFamily
import com.emotionalintelligence.emova.model.HabitRecord
import com.emotionalintelligence.emova.model.Trigger
import com.emotionalintelligence.emova.model.allEmotions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

// ─── UI models ────────────────────────────────────────────────────────────────

enum class InsightsPeriod { WEEK, MONTH, ALL_TIME }   // last 7 days / last 30 days / everything

data class Discovery(val emoji: String, val text: String, val color: Color)
data class EmotionBar(val name: String, val emoji: String, val color: Color, val count: Int, val fraction: Float)
data class HistoryItem(
    val id: Int,
    val dateLabel: String,        // "Today", "Yesterday" or "Monday 3 November"
    val time: String,
    val emoji: String,
    val name: String,
    val color: Color,
    val intensity: Int,
    val finalIntensity: Int,      // 0 = not answered
    val triggers: List<Trigger>,
    val hardToControl: Boolean,
    val note: String,
    val couldHaveDone: String,
    val planCue: String,
    val planInstead: String,
    val plan: String,
    val aiComment: String
)

data class DayDot(val label: String, val count: Int, val color: Color)

data class InsightsUiState(
    val period: InsightsPeriod = InsightsPeriod.WEEK,
    val totalCheckIns: Int = 0,
    val streakDays: Int = 0,
    val positivePercent: Int = 0,
    val discoveries: List<Discovery> = emptyList(),
    val bars: List<EmotionBar> = emptyList(),
    val lastSevenDays: List<DayDot> = emptyList(),
    val history: List<HistoryItem> = emptyList(),
    /** Total check-ins ever; used for the "keep going" card. */
    val allTimeCount: Int = 0,
    val checkInsToUnlock: Int = MIN_FOR_DEEP_PATTERNS
) {
    val isUnlocked: Boolean get() = allTimeCount >= MIN_FOR_DEEP_PATTERNS
}

const val MIN_FOR_DEEP_PATTERNS = 7

// ─── ViewModel ────────────────────────────────────────────────────────────────

class InsightsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = EmováDatabase.getInstance(application)
    private val period = MutableStateFlow(InsightsPeriod.WEEK)

    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    private fun str(resId: Int, vararg args: Any): String = getApplication<Application>().getString(resId, *args)

    init {
        viewModelScope.launch {
            combine(db.checkInDao().getAllFlow(), db.habitRecordDao().getAllFlow(), period) { checkIns, habits, p ->
                compute(checkIns, habits, p)
            }.flowOn(Dispatchers.Default).collect { state -> _uiState.value = state }
        }
    }

    fun setPeriod(p: InsightsPeriod) { period.update { p } }

    fun deleteCheckIn(id: Int) {
        viewModelScope.launch { db.checkInDao().deleteById(id) }
    }

    // ─── Computation (pure function of the data → easy to reason about) ─────

    private fun compute(all: List<CheckIn>, habits: List<HabitRecord>, p: InsightsPeriod): InsightsUiState {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        fun CheckIn.date(): LocalDate = Instant.ofEpochMilli(timestamp).atZone(zone).toLocalDate()
        fun CheckIn.hour(): Int = Instant.ofEpochMilli(timestamp).atZone(zone).hour

        val days = when (p) { InsightsPeriod.WEEK -> 7L; InsightsPeriod.MONTH -> 30L; InsightsPeriod.ALL_TIME -> null }
        val from = days?.let { today.minusDays(it - 1) }
        val current = if (from == null) all else all.filter { it.date() >= from }
        val previous = if (days == null || from == null) emptyList()
        else all.filter { val d = it.date(); d < from && d >= from.minusDays(days) }

        // Summary numbers
        val total = current.size
        val positive = current.count { it.isPositive() }
        val positivePct = if (total == 0) 0 else positive * 100 / total

        val datesWithCheckIn = all.map { it.date() }.toSet()
        var streak = 0
        var cursor = if (today in datesWithCheckIn) today else today.minusDays(1) // today not done yet ≠ broken
        while (cursor in datesWithCheckIn) { streak++; cursor = cursor.minusDays(1) }

        // Emotion bars
        val bars = current.groupBy { it.emotionName }.map { (name, list) ->
            val emotion = allEmotions.firstOrNull { it.name == name }
            val family = runCatching { EmotionFamily.valueOf(list.first().emotionFamily) }.getOrNull()
            EmotionBar(
                name = emotion?.let { str(it.nameResId) } ?: name,
                emoji = emotion?.emoji ?: "🙂",
                color = family?.color ?: Color.Gray,
                count = list.size,
                fraction = list.size / total.toFloat()
            )
        }.sortedByDescending { it.count }.take(6)

        // Last 7 days strip (always last 7 days, regardless of period)
        val strip = (6L downTo 0L).map { back ->
            val d = today.minusDays(back)
            val list = all.filter { it.date() == d }
            val topFamily = list.groupBy { it.emotionFamily }.maxByOrNull { it.value.size }?.key
            DayDot(
                label = d.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                count = list.size,
                color = topFamily?.let { runCatching { EmotionFamily.valueOf(it).color }.getOrNull() } ?: Color(0xFFEAE8F5)
            )
        }

        return InsightsUiState(
            period = p,
            totalCheckIns = total,
            streakDays = streak,
            positivePercent = positivePct,
            discoveries = buildDiscoveries(all, current, previous, habits, streak) { it.date() to it.hour() },
            bars = bars,
            lastSevenDays = strip,
            history = buildHistory(current, zone, today),
            allTimeCount = all.size
        )
    }

    private fun buildHistory(list: List<CheckIn>, zone: ZoneId, today: LocalDate): List<HistoryItem> {
        val dayFormat = DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.getDefault())
        val timeFormat = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(Locale.getDefault())
        return list.sortedByDescending { it.timestamp }.map { c ->
            val dt = Instant.ofEpochMilli(c.timestamp).atZone(zone)
            val date = dt.toLocalDate()
            val emotion = allEmotions.firstOrNull { it.name == c.emotionName }
            val family = runCatching { EmotionFamily.valueOf(c.emotionFamily) }.getOrNull()
            HistoryItem(
                id = c.id,
                dateLabel = when (date) {
                    today -> str(R.string.ins_h_today)
                    today.minusDays(1) -> str(R.string.ins_h_yesterday)
                    else -> date.format(dayFormat).replaceFirstChar { it.uppercase() }
                },
                time = dt.format(timeFormat),
                emoji = emotion?.emoji ?: "💭",
                name = emotion?.let { str(it.nameResId) } ?: c.emotionName,
                color = family?.color ?: Color.Gray,
                intensity = c.intensity,
                finalIntensity = c.finalIntensity,
                triggers = c.triggerList.mapNotNull { Trigger.fromId(it) },
                hardToControl = c.hardToControl,
                note = c.note,
                couldHaveDone = c.couldHaveDone,
                planCue = c.planCue,
                planInstead = c.planInstead,
                plan = c.plan,
                aiComment = c.aiComment
            )
        }
    }

    private fun CheckIn.isPositive() = emotionFamily.equals("POSITIVE", ignoreCase = true)
    private fun CheckIn.isHard() = !isPositive()   // DIFFICULT + ACTIVATED (+ legacy families)

    /**
     * Simple, honest patterns. Every rule has a minimum sample size so the app never
     * announces a "pattern" built from one or two data points.
     */
    private fun buildDiscoveries(
        all: List<CheckIn>,
        current: List<CheckIn>,
        previous: List<CheckIn>,
        habits: List<HabitRecord>,
        streak: Int,
        dateAndHour: (CheckIn) -> Pair<LocalDate, Int>
    ): List<Discovery> {
        if (current.isEmpty()) return emptyList()
        val out = mutableListOf<Discovery>()
        val purple = Color(0xFF6C63FF); val green = Color(0xFF06D6A0); val blue = Color(0xFF4CC9F0); val amber = Color(0xFFE8A020)

        // 1) Most frequent emotion — works from the very first check-ins
        current.groupBy { it.emotionName }.maxByOrNull { it.value.size }?.let { (name, list) ->
            val e = allEmotions.firstOrNull { it.name == name }
            val label = (e?.let { str(it.nameResId) } ?: name).lowercase()
            out += Discovery(e?.emoji ?: "🙂", str(R.string.ins_d_top, label, list.size * 100 / current.size), purple)
        }

        // 1b) The emotion the person says is hardest to control (from the worksheet's step 1)
        current.filter { it.hardToControl }.groupBy { it.emotionName }.maxByOrNull { it.value.size }?.let { (name, list) ->
            if (list.size >= 2) {
                val e = allEmotions.firstOrNull { it.name == name }
                val label = (e?.let { str(it.nameResId) } ?: name).lowercase()
                out += Discovery("🎯", str(R.string.ins_d_hard, label, list.size), amber)
            }
        }

        // 2) What's behind the hard moments: trigger → hard-emotion share
        val byTrigger = mutableMapOf<String, MutableList<CheckIn>>()
        current.forEach { c -> c.triggerList.forEach { byTrigger.getOrPut(it) { mutableListOf() }.add(c) } }
        byTrigger.filter { it.value.size >= 3 }
            .maxByOrNull { (_, l) -> l.count { it.isHard() }.toDouble() / l.size }?.let { (id, list) ->
                val pct = list.count { it.isHard() } * 100 / list.size
                val trigger = Trigger.fromId(id)
                if (pct >= 60 && trigger != null) {
                    out += Discovery(trigger.emoji, str(R.string.ins_d_trigger, str(trigger.labelResId).lowercase(), pct), blue)
                }
            }

        // 3) Time of day
        val byTime = current.groupBy { timeBucket(dateAndHour(it).second) }
        byTime.filter { it.value.size >= 3 }
            .maxByOrNull { (_, l) -> l.count { it.isHard() }.toDouble() / l.size }?.let { (bucket, list) ->
                val pct = list.count { it.isHard() } * 100 / list.size
                if (pct >= 60) out += Discovery("🕒", str(R.string.ins_d_time, str(bucket.resId), pct), amber)
            }

        // 4) Day of week (needs a bit more data: this is where noise sneaks in)
        if (current.size >= 8) {
            val byDay = current.groupBy { dateAndHour(it).first.dayOfWeek }.filter { it.value.size >= 3 }
            byDay.maxByOrNull { (_, l) -> l.count { it.isHard() }.toDouble() / l.size }?.let { (dow, list) ->
                val pct = list.count { it.isHard() } * 100 / list.size
                if (pct >= 60) out += Discovery("📅", str(R.string.ins_d_day_hard, dayName(dow), pct), blue)
            }
            byDay.maxByOrNull { (_, l) -> l.count { it.isPositive() }.toDouble() / l.size }?.let { (dow, list) ->
                val pct = list.count { it.isPositive() } * 100 / list.size
                if (pct >= 60) out += Discovery("☀️", str(R.string.ins_d_day_best, dayName(dow), pct), green)
            }
        }

        // 5) Does reflecting help? (real before/after data from the new flow)
        val withAfter = current.filter { it.finalIntensity > 0 && it.isHard() }
        if (withAfter.size >= 3) {
            val avgDrop = withAfter.map { (it.intensity - it.finalIntensity).toDouble() }.average()
            if (avgDrop >= 0.5) out += Discovery("🌱", str(R.string.ins_d_relief, avgDrop), green)
        }

        // 6) Mindfulness days vs other days
        val mindfulDays = habits.filter { it.habitId == "mindfulness" }.map { dateAndHourOfMillis(it.timestamp) }.toSet()
        if (mindfulDays.isNotEmpty()) {
            val (on, off) = all.partition { dateAndHour(it).first in mindfulDays }
            if (on.size >= 3 && off.size >= 3) {
                val onPct = on.count { it.isHard() } * 100 / on.size
                val offPct = off.count { it.isHard() } * 100 / off.size
                if (onPct + 10 <= offPct) out += Discovery("🎧", str(R.string.ins_d_mindful, onPct, offPct), green)
            }
        }

        // 7) Trend vs previous period (only for 7/30-day views)
        if (previous.size >= 3 && current.size >= 3) {
            val now = current.count { it.isHard() } * 100 / current.size
            val before = previous.count { it.isHard() } * 100 / previous.size
            if (now + 10 <= before) out += Discovery("📉", str(R.string.ins_d_trend_down, before, now), green)
            else if (now >= before + 10) out += Discovery("📈", str(R.string.ins_d_trend_up, before, now), amber)
        }

        // 8) Consistency
        if (streak >= 3) out += Discovery("🔥", str(R.string.ins_d_streak, streak), amber)

        return out.take(5)
    }

    private fun dateAndHourOfMillis(ts: Long): LocalDate =
        Instant.ofEpochMilli(ts).atZone(ZoneId.systemDefault()).toLocalDate()

    private fun dayName(dow: DayOfWeek): String = str(
        when (dow) {
            DayOfWeek.MONDAY -> R.string.day_mondays
            DayOfWeek.TUESDAY -> R.string.day_tuesdays
            DayOfWeek.WEDNESDAY -> R.string.day_wednesdays
            DayOfWeek.THURSDAY -> R.string.day_thursdays
            DayOfWeek.FRIDAY -> R.string.day_fridays
            DayOfWeek.SATURDAY -> R.string.day_saturdays
            DayOfWeek.SUNDAY -> R.string.day_sundays
        }
    )

    private enum class TimeBucket(val resId: Int) {
        MORNING(R.string.ins_tod_morning), AFTERNOON(R.string.ins_tod_afternoon),
        EVENING(R.string.ins_tod_evening), NIGHT(R.string.ins_tod_night)
    }

    private fun timeBucket(hour: Int) = when (hour) {
        in 5..11 -> TimeBucket.MORNING
        in 12..17 -> TimeBucket.AFTERNOON
        in 18..22 -> TimeBucket.EVENING
        else -> TimeBucket.NIGHT
    }
}
