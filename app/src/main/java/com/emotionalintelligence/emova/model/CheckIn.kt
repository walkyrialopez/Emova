package com.emotionalintelligence.emova.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "checkins")
data class CheckIn(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val emotionId: String,
    val emotionName: String,
    val emotionFamily: String,
    val intensity: Int,           // 1–10, at the moment of the check-in
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),

    // ── Added in DB v5 ────────────────────────────────────────────────────
    /** Comma-separated [Trigger] ids, e.g. "work,sleep". Locale independent. */
    @ColumnInfo(defaultValue = "''") val triggers: String = "",
    /** Worksheet step 5, field 3: "Haré esto" / "I will do this". */
    @ColumnInfo(defaultValue = "''") val plan: String = "",
    /** Intensity after the reflection (0 = not answered). */
    @ColumnInfo(defaultValue = "0") val finalIntensity: Int = 0,
    /** The AI note shown at the end of the check-in. */
    @ColumnInfo(defaultValue = "''") val aiComment: String = "",

    // ── Added in DB v6 (fields from the "Ponlo por escrito" worksheet) ────
    /** Step 3, question 2: "¿Qué podrías haber hecho para controlarte?" */
    @ColumnInfo(defaultValue = "''") val couldHaveDone: String = "",
    /** Step 5, field 1: "La próxima vez que sienta esto:" */
    @ColumnInfo(defaultValue = "''") val planCue: String = "",
    /** Step 5, field 2: "En lugar de hacer esto:" */
    @ColumnInfo(defaultValue = "''") val planInstead: String = "",
    /** Step 1/2: "¿Es una emoción que te cuesta controlar?" */
    @ColumnInfo(defaultValue = "0") val hardToControl: Boolean = false
) {
    val triggerList: List<String>
        get() = if (triggers.isBlank()) emptyList() else triggers.split(",").filter { it.isNotBlank() }
}
