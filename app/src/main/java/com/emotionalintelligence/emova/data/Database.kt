package com.emotionalintelligence.emova.data

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    fun getAll(): Flow<List<GratitudeEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: GratitudeEntry)

    @Query("SELECT * FROM gratitude_entries ORDER BY timestamp DESC")
    fun getAllFlow(): Flow<List<GratitudeEntry>>

    @Query("SELECT * FROM gratitude_entries WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: String): GratitudeEntry?
}

// ─── HabitRecord DAO ──────────────────────────────────────────────────────────

@Dao
interface HabitRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: HabitRecord)

    @Query("SELECT * FROM habit_records ORDER BY timestamp DESC")
    fun getAllFlow(): Flow<List<HabitRecord>>
}

// ─── Migrations ───────────────────────────────────────────────────────────────
//
// Rule: never use fallbackToDestructiveMigration() in production — it silently
// deletes all user data. Write a Migration for every version bump instead.
//
// Version history:
//   1 → 2: (unknown — add retroactively if you need to support upgrades from v1)
//   2 → 3: (unknown)
//   3 → 4: added habit_records table

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `habit_records` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `habitId` TEXT NOT NULL,
                `timestamp` INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}

// Template for your next migration — fill in the ALTER/CREATE statements:
//
// val MIGRATION_4_5 = object : Migration(4, 5) {
//     override fun migrate(database: SupportSQLiteDatabase) {
//         database.execSQL("ALTER TABLE checkins ADD COLUMN newField TEXT NOT NULL DEFAULT ''")
//     }
// }

// ─── Database ─────────────────────────────────────────────────────────────────

@Database(
    entities = [CheckIn::class, JournalEntry::class, GratitudeEntry::class, HabitRecord::class],
    version = 4,
    exportSchema = true   // set to true so schema diffs are tracked in version control
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
                    .addMigrations(MIGRATION_3_4)
                    // Add future migrations here: .addMigrations(MIGRATION_3_4, MIGRATION_4_5)
                    // DO NOT add fallbackToDestructiveMigration() — it deletes user data.
                    .build()
                    .also { INSTANCE = it }
            }
    }
}