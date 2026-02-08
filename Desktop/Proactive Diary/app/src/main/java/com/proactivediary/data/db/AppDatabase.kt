package com.proactivediary.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.proactivediary.data.db.converters.Converters
import com.proactivediary.data.db.dao.EntryDao
import com.proactivediary.data.db.dao.GoalCheckInDao
import com.proactivediary.data.db.dao.GoalDao
import com.proactivediary.data.db.dao.PreferenceDao
import com.proactivediary.data.db.dao.WritingReminderDao
import com.proactivediary.data.db.entities.EntryEntity
import com.proactivediary.data.db.entities.GoalCheckInEntity
import com.proactivediary.data.db.entities.GoalEntity
import com.proactivediary.data.db.entities.PreferenceEntity
import com.proactivediary.data.db.entities.WritingReminderEntity

@Database(
    entities = [
        EntryEntity::class,
        GoalEntity::class,
        GoalCheckInEntity::class,
        WritingReminderEntity::class,
        PreferenceEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun entryDao(): EntryDao
    abstract fun goalDao(): GoalDao
    abstract fun goalCheckInDao(): GoalCheckInDao
    abstract fun writingReminderDao(): WritingReminderDao
    abstract fun preferenceDao(): PreferenceDao

    companion object {
        const val DATABASE_NAME = "proactive_diary.db"

        // Add migrations here as schema evolves. Example:
        // val MIGRATION_1_2 = Migration(1, 2) { db ->
        //     db.execSQL("ALTER TABLE entries ADD COLUMN image_uri TEXT")
        // }
        val MIGRATIONS: Array<Migration> = arrayOf(
            // MIGRATION_1_2,
        )

        fun createCallback(): Callback {
            return object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)

                    // Create FTS4 virtual table for full-text search
                    db.execSQL("""
                        CREATE VIRTUAL TABLE IF NOT EXISTS entries_fts
                        USING fts4(title, content, tags, content='entries')
                    """)

                    // Trigger: keep FTS in sync after INSERT
                    db.execSQL("""
                        CREATE TRIGGER IF NOT EXISTS entries_ai AFTER INSERT ON entries BEGIN
                            INSERT INTO entries_fts(docid, title, content, tags)
                            VALUES (new.rowid, new.title, new.content, new.tags);
                        END
                    """)

                    // Trigger: keep FTS in sync after DELETE
                    db.execSQL("""
                        CREATE TRIGGER IF NOT EXISTS entries_ad AFTER DELETE ON entries BEGIN
                            INSERT INTO entries_fts(entries_fts, docid, title, content, tags)
                            VALUES('delete', old.rowid, old.title, old.content, old.tags);
                        END
                    """)

                    // Trigger: keep FTS in sync after UPDATE
                    db.execSQL("""
                        CREATE TRIGGER IF NOT EXISTS entries_au AFTER UPDATE ON entries BEGIN
                            INSERT INTO entries_fts(entries_fts, docid, title, content, tags)
                            VALUES('delete', old.rowid, old.title, old.content, old.tags);
                            INSERT INTO entries_fts(docid, title, content, tags)
                            VALUES (new.rowid, new.title, new.content, new.tags);
                        END
                    """)
                }
            }
        }
    }
}
