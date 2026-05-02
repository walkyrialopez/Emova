package com.emotionalintelligence.emova.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val checkInId: Int,
    val emotionId: String,
    val emotionName: String,
    val emotionFamily: String,

    // 🎯 NUEVO: Los campos que el ViewModel estaba buscando
    val conversationHistory: String,
    val initialBrainDump: String = "",

    val timestamp: Long = System.currentTimeMillis()
)