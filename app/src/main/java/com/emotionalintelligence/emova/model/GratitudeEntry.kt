package com.emotionalintelligence.emova.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gratitude_entries")
data class GratitudeEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: String,           // "2024-03-06" — one entry per day
    val items: String,          // JSON-encoded list of 10 items
    val timestamp: Long = System.currentTimeMillis()
)