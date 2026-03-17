package com.emotionalintelligence.emova.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val checkInId: Int,           // links to the CheckIn
    val emotionId: String,
    val emotionName: String,
    val emotionFamily: String,
    val prompt: String,
    val answer: String,
    val timestamp: Long = System.currentTimeMillis()
)
