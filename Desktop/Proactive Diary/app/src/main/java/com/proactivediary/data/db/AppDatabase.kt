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
import com.proactivediary.data.db.dao.TemplateDao
import com.proactivediary.data.db.dao.WritingReminderDao
import com.proactivediary.data.db.dao.InsightDao
import com.proactivediary.data.db.entities.EntryEntity
import com.proactivediary.data.db.entities.GoalCheckInEntity
import com.proactivediary.data.db.entities.GoalEntity
import com.proactivediary.data.db.entities.InsightEntity
import com.proactivediary.data.db.entities.PreferenceEntity
import com.proactivediary.data.db.entities.TemplateEntity
import com.proactivediary.data.db.entities.WritingReminderEntity

@Database(
    entities = [
        EntryEntity::class,
        GoalEntity::class,
        GoalCheckInEntity::class,
        WritingReminderEntity::class,
        PreferenceEntity::class,
        TemplateEntity::class,
        InsightEntity::class
    ],
    version = 9,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun entryDao(): EntryDao
    abstract fun goalDao(): GoalDao
    abstract fun goalCheckInDao(): GoalCheckInDao
    abstract fun writingReminderDao(): WritingReminderDao
    abstract fun preferenceDao(): PreferenceDao
    abstract fun templateDao(): TemplateDao
    abstract fun insightDao(): InsightDao

    companion object {
        const val DATABASE_NAME = "proactive_diary.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE entries ADD COLUMN tagged_contacts TEXT NOT NULL DEFAULT '[]'")

                // Create FTS table + triggers for existing v1 users (onCreate only runs on fresh install)
                db.execSQL("""
                    CREATE VIRTUAL TABLE IF NOT EXISTS entries_fts
                    USING fts4(title, content, tags, content='entries')
                """)
                db.execSQL("""
                    CREATE TRIGGER IF NOT EXISTS entries_ai AFTER INSERT ON entries BEGIN
                        INSERT INTO entries_fts(docid, title, content, tags)
                        VALUES (new.rowid, new.title, new.content, new.tags);
                    END
                """)
                db.execSQL("""
                    CREATE TRIGGER IF NOT EXISTS entries_ad AFTER DELETE ON entries BEGIN
                        INSERT INTO entries_fts(entries_fts, docid, title, content, tags)
                        VALUES('delete', old.rowid, old.title, old.content, old.tags);
                    END
                """)
                db.execSQL("""
                    CREATE TRIGGER IF NOT EXISTS entries_au AFTER UPDATE ON entries BEGIN
                        INSERT INTO entries_fts(entries_fts, docid, title, content, tags)
                        VALUES('delete', old.rowid, old.title, old.content, old.tags);
                        INSERT INTO entries_fts(docid, title, content, tags)
                        VALUES (new.rowid, new.title, new.content, new.tags);
                    END
                """)
                // Backfill existing entries into FTS index
                db.execSQL("INSERT INTO entries_fts(docid, title, content, tags) SELECT rowid, title, content, tags FROM entries")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE entries ADD COLUMN images TEXT NOT NULL DEFAULT '[]'")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE entries ADD COLUMN latitude REAL DEFAULT NULL")
                db.execSQL("ALTER TABLE entries ADD COLUMN longitude REAL DEFAULT NULL")
                db.execSQL("ALTER TABLE entries ADD COLUMN location_name TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE entries ADD COLUMN weather_temp REAL DEFAULT NULL")
                db.execSQL("ALTER TABLE entries ADD COLUMN weather_condition TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE entries ADD COLUMN weather_icon TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE entries ADD COLUMN template_id TEXT DEFAULT NULL")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS templates (
                        id TEXT NOT NULL PRIMARY KEY,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        prompts TEXT NOT NULL DEFAULT '[]',
                        category TEXT NOT NULL,
                        is_built_in INTEGER NOT NULL DEFAULT 1,
                        created_at INTEGER NOT NULL DEFAULT 0
                    )
                """)
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE entries ADD COLUMN audio_path TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS insights (
                        id TEXT NOT NULL PRIMARY KEY,
                        week_start INTEGER NOT NULL,
                        summary TEXT NOT NULL,
                        themes TEXT NOT NULL DEFAULT '[]',
                        mood_trend TEXT NOT NULL DEFAULT 'stable',
                        prompt_suggestions TEXT NOT NULL DEFAULT '[]',
                        generated_at INTEGER NOT NULL
                    )
                """)
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE entries ADD COLUMN content_html TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE entries ADD COLUMN content_plain TEXT DEFAULT NULL")
                db.execSQL("UPDATE entries SET content_plain = content WHERE content_plain IS NULL")

                db.execSQL("DROP TRIGGER IF EXISTS entries_ai")
                db.execSQL("DROP TRIGGER IF EXISTS entries_ad")
                db.execSQL("DROP TRIGGER IF EXISTS entries_au")

                db.execSQL("""
                    CREATE TRIGGER entries_ai AFTER INSERT ON entries BEGIN
                        INSERT INTO entries_fts(docid, title, content, tags)
                        VALUES (new.rowid, new.title, COALESCE(new.content_plain, new.content), new.tags);
                    END
                """)
                db.execSQL("""
                    CREATE TRIGGER entries_ad AFTER DELETE ON entries BEGIN
                        INSERT INTO entries_fts(entries_fts, docid, title, content, tags)
                        VALUES('delete', old.rowid, old.title, COALESCE(old.content_plain, old.content), old.tags);
                    END
                """)
                db.execSQL("""
                    CREATE TRIGGER entries_au AFTER UPDATE ON entries BEGIN
                        INSERT INTO entries_fts(entries_fts, docid, title, content, tags)
                        VALUES('delete', old.rowid, old.title, COALESCE(old.content_plain, old.content), old.tags);
                        INSERT INTO entries_fts(docid, title, content, tags)
                        VALUES (new.rowid, new.title, COALESCE(new.content_plain, new.content), new.tags);
                    END
                """)

                db.execSQL("INSERT INTO entries_fts(entries_fts) VALUES('rebuild')")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add sync_status to all syncable entities (default 1 = PENDING_UPLOAD)
                db.execSQL("ALTER TABLE entries ADD COLUMN sync_status INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE goals ADD COLUMN sync_status INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE goal_checkins ADD COLUMN sync_status INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE writing_reminders ADD COLUMN sync_status INTEGER NOT NULL DEFAULT 1")
                // WritingReminderEntity was missing updatedAt — add it
                db.execSQL("ALTER TABLE writing_reminders ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0")
                db.execSQL("UPDATE writing_reminders SET updated_at = ${System.currentTimeMillis()}")
            }
        }

        val MIGRATIONS: Array<Migration> = arrayOf(
            MIGRATION_1_2,
            MIGRATION_2_3,
            MIGRATION_3_4,
            MIGRATION_4_5,
            MIGRATION_5_6,
            MIGRATION_6_7,
            MIGRATION_7_8,
            MIGRATION_8_9,
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

                    ensureFtsTriggers(db)
                }

                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    // Self-heal: ensure FTS table and triggers exist on every open.
                    // Protects against corrupt state from a previously failed delete-all.
                    try {
                        db.execSQL("""
                            CREATE VIRTUAL TABLE IF NOT EXISTS entries_fts
                            USING fts4(title, content, tags, content='entries')
                        """)
                        ensureFtsTriggers(db)
                    } catch (_: Exception) {
                        // Non-fatal — search may not work but app won't crash
                    }
                }
            }
        }

        private fun ensureFtsTriggers(db: SupportSQLiteDatabase) {
            // Trigger: keep FTS in sync after INSERT
            db.execSQL("""
                CREATE TRIGGER IF NOT EXISTS entries_ai AFTER INSERT ON entries BEGIN
                    INSERT INTO entries_fts(docid, title, content, tags)
                    VALUES (new.rowid, new.title, COALESCE(new.content_plain, new.content), new.tags);
                END
            """)

            // Trigger: keep FTS in sync after DELETE
            db.execSQL("""
                CREATE TRIGGER IF NOT EXISTS entries_ad AFTER DELETE ON entries BEGIN
                    INSERT INTO entries_fts(entries_fts, docid, title, content, tags)
                    VALUES('delete', old.rowid, old.title, COALESCE(old.content_plain, old.content), old.tags);
                END
            """)

            // Trigger: keep FTS in sync after UPDATE
            db.execSQL("""
                CREATE TRIGGER IF NOT EXISTS entries_au AFTER UPDATE ON entries BEGIN
                    INSERT INTO entries_fts(entries_fts, docid, title, content, tags)
                    VALUES('delete', old.rowid, old.title, COALESCE(old.content_plain, old.content), old.tags);
                    INSERT INTO entries_fts(docid, title, content, tags)
                    VALUES (new.rowid, new.title, COALESCE(new.content_plain, new.content), new.tags);
                END
            """)
        }
    }
}
