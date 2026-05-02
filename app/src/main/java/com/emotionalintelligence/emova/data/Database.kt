package com.emotionalintelligence.emova.data

import android.content.Context
import androidx.room.*
import com.emotionalintelligence.emova.model.CheckIn
import com.emotionalintelligence.emova.model.JournalEntry
import com.emotionalintelligence.emova.model.GratitudeEntry
import com.emotionalintelligence.emova.model.HabitRecord
import kotlinx.coroutines.flow.Flow

// ─── CheckIn DAO ──────────────────────────────────────────────────────────────

@Dao
interface CheckInDao {
    @Insert
    suspend fun insert(checkIn: CheckIn): Long

    @Query("SELECT * FROM checkins ORDER BY timestamp DESC")
    fun getAllFlow(): Flow<List<CheckIn>>

    @Query("DELETE FROM checkins WHERE id = :id")
    suspend fun deleteById(id: Int)
}

// ─── JournalEntry DAO ─────────────────────────────────────────────────────────

@Dao
interface JournalEntryDao {
    @Insert
    suspend fun insert(entry: JournalEntry)

    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC")
    fun getAllFlow(): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries WHERE checkInId = :checkInId")
    fun getByCheckIn(checkInId: Int): Flow<List<JournalEntry>>
}

// ─── GratitudeEntry DAO ───────────────────────────────────────────────────────

@Dao
interface GratitudeEntryDao {
    @Query("SELECT * FROM gratitude_entries ORDER BY date DESC")
    fun getAll(): kotlinx.coroutines.flow.Flow<List<GratitudeEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: GratitudeEntry)

    @Query("SELECT * FROM gratitude_entries ORDER BY timestamp DESC")
    fun getAllFlow(): Flow<List<GratitudeEntry>>

    @Query("SELECT * FROM gratitude_entries WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: String): GratitudeEntry?
}

// ─── HabitRecord DAO ──────────────────────────────────────────────────────────
// 🎯 2. BLOQUE DAO FALTANTE
@Dao
interface HabitRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: HabitRecord)

    @Query("SELECT * FROM habit_records ORDER BY timestamp DESC")
    fun getAllFlow(): Flow<List<HabitRecord>>
}

// ─── Database ─────────────────────────────────────────────────────────────────

@Database(
    // 🎯 3. AGREGADO HabitRecord::class Y VERSIÓN SUBIDA A 4
    entities = [CheckIn::class, JournalEntry::class, GratitudeEntry::class, HabitRecord::class],
    version = 4,
    exportSchema = false
)
abstract class EmováDatabase : RoomDatabase() {
    abstract fun checkInDao(): CheckInDao
    abstract fun journalEntryDao(): JournalEntryDao
    abstract fun gratitudeEntryDao(): GratitudeEntryDao
    abstract fun habitRecordDao(): HabitRecordDao

    companion object {
        @Volatile private var INSTANCE: EmováDatabase? = null

        fun getInstance(context: Context): EmováDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    EmováDatabase::class.java,
                    "emova_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}