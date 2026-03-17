package com.emotionalintelligence.emova.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "checkins")
data class CheckIn(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val emotionId: String,
    val emotionName: String,
    val emotionFamily: String,
    val intensity: Int,           // 1–10
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
