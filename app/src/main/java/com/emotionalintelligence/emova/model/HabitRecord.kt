package com.emotionalintelligence.emova.model
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habit_records")
data class HabitRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val habitId: String, // Aquí es donde guardaremos "reflection" o "appreciation"
    val timestamp: Long = System.currentTimeMillis()
)