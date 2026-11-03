package com.emotionalintelligence.emova.util

import android.content.Context
import com.emotionalintelligence.emova.model.Lesson
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale
import java.util.Random

/**
 * Keeps track of which lessons the person has completed and what "today's lesson" is.
 *
 * Rules:
 *  - Today's lesson is the first lesson (in path order) that hasn't been completed.
 *  - It is fixed for the whole day: finishing it doesn't instantly swap it for tomorrow's.
 *  - When every lesson is completed, the day gets a short "practice" prompt instead,
 *    walking through a fixed shuffled list so it doesn't repeat until the list is used up.
 */
class LessonProgress(context: Context) {

    private val prefs = context.getSharedPreferences("emova_lessons", Context.MODE_PRIVATE)
    private val todayPrefs = context.getSharedPreferences("emova_today", Context.MODE_PRIVATE)

    private fun todayString(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    fun completedIds(): Set<String> = prefs.getStringSet(KEY_COMPLETED, emptySet())?.toSet() ?: emptySet()

    /** Marks a lesson done and flips the home-screen "lesson" habit for today. */
    fun markCompleted(lessonId: String) {
        val updated = completedIds() + lessonId
        prefs.edit().putStringSet(KEY_COMPLETED, updated).apply()
        todayPrefs.edit().putString("lesson_date", todayString()).apply()
    }

    /** @return the id of today's lesson, or null when every lesson has been completed. */
    fun todaysLessonId(path: List<Lesson>): String? {
        val today = todayString()
        val savedId = prefs.getString(KEY_TODAY_ID, "") ?: ""
        if (prefs.getString(KEY_TODAY_DATE, "") == today && path.any { it.id == savedId }) return savedId

        val completed = completedIds()
        val next = path.firstOrNull { it.id !in completed }?.id
        prefs.edit().putString(KEY_TODAY_DATE, today).putString(KEY_TODAY_ID, next ?: "").apply()
        return next
    }

    fun isPracticeDoneToday(): Boolean = prefs.getString(KEY_PRACTICE_DATE, "") == todayString()

    fun markPracticeDone() {
        prefs.edit().putString(KEY_PRACTICE_DATE, todayString()).apply()
        todayPrefs.edit().putString("lesson_date", todayString()).apply()
    }

    companion object {
        private const val KEY_COMPLETED = "completed"
        private const val KEY_TODAY_DATE = "today_date"
        private const val KEY_TODAY_ID = "today_id"
        private const val KEY_PRACTICE_DATE = "practice_date"

        /** Index into the practice list for today (no repeats until the whole list is used). */
        fun practiceIndexForToday(size: Int): Int {
            if (size <= 0) return 0
            val order = (0 until size).toList().shuffled(Random(77))
            return order[(LocalDate.now().toEpochDay() % size).toInt()]
        }
    }
}
