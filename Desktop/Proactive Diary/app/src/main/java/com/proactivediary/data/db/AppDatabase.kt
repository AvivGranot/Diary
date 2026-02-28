package com.proactivediary.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.proactivediary.data.db.converters.Converters
import com.proactivediary.data.db.dao.ActivitySignalDao
import com.proactivediary.data.db.dao.EntryDao
import com.proactivediary.data.db.dao.GoalCheckInDao
import com.proactivediary.data.db.dao.GoalDao
import com.proactivediary.data.db.dao.JournalDao
import com.proactivediary.data.db.dao.MoodCheckInDao
import com.proactivediary.data.db.dao.PreferenceDao
import com.proactivediary.data.db.dao.TemplateDao
import com.proactivediary.data.db.dao.WritingReminderDao
import com.proactivediary.data.db.dao.InsightDao
import com.proactivediary.data.db.entities.ActivitySignalEntity
import com.proactivediary.data.db.entities.EntryEntity
import com.proactivediary.data.db.entities.GoalCheckInEntity
import com.proactivediary.data.db.entities.GoalEntity
import com.proactivediary.data.db.entities.InsightEntity
import com.proactivediary.data.db.entities.JournalEntity
import com.proactivediary.data.db.entities.JournalEntryJoin
import com.proactivediary.data.db.entities.MoodCheckInEntity
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
        InsightEntity::class,
        MoodCheckInEntity::class,
        JournalEntity::class,
        JournalEntryJoin::class,
        ActivitySignalEntity::class
    ],
    version = 12,
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
    abstract fun moodCheckInDao(): MoodCheckInDao
    abstract fun journalDao(): JournalDao
    abstract fun activitySignalDao(): ActivitySignalDao

    companion object {
        const val DATABASE_NAME = "proactive_diary.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE entries ADD COLUMN tagged_contacts TEXT NOT NULL DEFAULT '[]'")

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
                db.execSQL("ALTER TABLE entries ADD COLUMN sync_status INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE goals ADD COLUMN sync_status INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE goal_checkins ADD COLUMN sync_status INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE writing_reminders ADD COLUMN sync_status INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE writing_reminders ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0")
                db.execSQL("UPDATE writing_reminders SET updated_at = ${System.currentTimeMillis()}")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Phase 1: Entry Management
                db.execSQL("ALTER TABLE entries ADD COLUMN deleted_at INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE entries ADD COLUMN is_bookmarked INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE entries ADD COLUMN entry_date INTEGER DEFAULT NULL")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_entries_deleted_at ON entries(deleted_at)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_entries_bookmarked ON entries(is_bookmarked)")

                // Phase 3: Mood Check-ins
                db.execSQL("""CREATE TABLE IF NOT EXISTS mood_checkins (
                    id TEXT NOT NULL PRIMARY KEY,
                    rating INTEGER NOT NULL,
                    tags TEXT NOT NULL DEFAULT '[]',
                    note TEXT DEFAULT NULL,
                    entry_id TEXT DEFAULT NULL,
                    created_at INTEGER NOT NULL,
                    sync_status INTEGER NOT NULL DEFAULT 1
                )""")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_mood_checkins_created_at ON mood_checkins(created_at)")

                // Phase 4: Multi-Journal
                db.execSQL("""CREATE TABLE IF NOT EXISTS journals (
                    id TEXT NOT NULL PRIMARY KEY,
                    title TEXT NOT NULL,
                    emoji TEXT DEFAULT NULL,
                    color_key TEXT NOT NULL DEFAULT 'sky',
                    sort_order INTEGER NOT NULL DEFAULT 0,
                    is_locked INTEGER NOT NULL DEFAULT 0,
                    created_at INTEGER NOT NULL,
                    updated_at INTEGER NOT NULL,
                    sync_status INTEGER NOT NULL DEFAULT 1
                )""")
                db.execSQL("""CREATE TABLE IF NOT EXISTS journal_entry_join (
                    journal_id TEXT NOT NULL,
                    entry_id TEXT NOT NULL,
                    PRIMARY KEY(journal_id, entry_id),
                    FOREIGN KEY(journal_id) REFERENCES journals(id) ON DELETE CASCADE,
                    FOREIGN KEY(entry_id) REFERENCES entries(id) ON DELETE CASCADE
                )""")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_journal_entry_join_entry_id ON journal_entry_join(entry_id)")

                // Phase 7: Activity Signals
                db.execSQL("""CREATE TABLE IF NOT EXISTS activity_signals (
                    id TEXT NOT NULL PRIMARY KEY,
                    type TEXT NOT NULL,
                    data TEXT NOT NULL,
                    timestamp INTEGER NOT NULL,
                    consumed INTEGER NOT NULL DEFAULT 0,
                    created_at INTEGER NOT NULL
                )""")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_activity_signals_timestamp ON activity_signals(timestamp)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_activity_signals_type ON activity_signals(type)")

                // Phase 9: Font color
                db.execSQL("ALTER TABLE entries ADD COLUMN font_color TEXT DEFAULT NULL")

                // Rebuild FTS triggers to exclude soft-deleted entries
                db.execSQL("DROP TRIGGER IF EXISTS entries_ai")
                db.execSQL("DROP TRIGGER IF EXISTS entries_ad")
                db.execSQL("DROP TRIGGER IF EXISTS entries_au")
                db.execSQL("""CREATE TRIGGER entries_ai AFTER INSERT ON entries
                    WHEN new.deleted_at IS NULL BEGIN
                        INSERT INTO entries_fts(docid, title, content, tags)
                        VALUES (new.rowid, new.title, COALESCE(new.content_plain, new.content), new.tags);
                    END""")
                db.execSQL("""CREATE TRIGGER entries_ad AFTER DELETE ON entries BEGIN
                        INSERT INTO entries_fts(entries_fts, docid, title, content, tags)
                        VALUES('delete', old.rowid, old.title, COALESCE(old.content_plain, old.content), old.tags);
                    END""")
                db.execSQL("""CREATE TRIGGER entries_au AFTER UPDATE ON entries BEGIN
                        INSERT INTO entries_fts(entries_fts, docid, title, content, tags)
                        VALUES('delete', old.rowid, old.title, COALESCE(old.content_plain, old.content), old.tags);
                        INSERT INTO entries_fts(docid, title, content, tags)
                        SELECT new.rowid, new.title, COALESCE(new.content_plain, new.content), new.tags
                        WHERE new.deleted_at IS NULL;
                    END""")
                db.execSQL("INSERT INTO entries_fts(entries_fts) VALUES('rebuild')")
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE entries ADD COLUMN capsule_open_date INTEGER DEFAULT NULL")
            }
        }

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE entries ADD COLUMN word_count INTEGER NOT NULL DEFAULT 0")
                } catch (_: Exception) { /* column may already exist */ }
                try {
                    db.execSQL("ALTER TABLE writing_reminders ADD COLUMN fallback_enabled INTEGER NOT NULL DEFAULT 1")
                } catch (_: Exception) { /* column may already exist */ }
                try {
                    db.execSQL("ALTER TABLE writing_reminders ADD COLUMN label TEXT NOT NULL DEFAULT 'Write in your diary'")
                } catch (_: Exception) { /* column may already exist */ }
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
            MIGRATION_9_10,
            MIGRATION_10_11,
            MIGRATION_11_12,
        )

        fun createCallback(): Callback {
            return object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)

                    db.execSQL("""
                        CREATE VIRTUAL TABLE IF NOT EXISTS entries_fts
                        USING fts4(title, content, tags, content='entries')
                    """)

                    ensureFtsTriggers(db)
                }

                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
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
            db.execSQL("""
                CREATE TRIGGER IF NOT EXISTS entries_ai AFTER INSERT ON entries
                WHEN new.deleted_at IS NULL BEGIN
                    INSERT INTO entries_fts(docid, title, content, tags)
                    VALUES (new.rowid, new.title, COALESCE(new.content_plain, new.content), new.tags);
                END
            """)

            db.execSQL("""
                CREATE TRIGGER IF NOT EXISTS entries_ad AFTER DELETE ON entries BEGIN
                    INSERT INTO entries_fts(entries_fts, docid, title, content, tags)
                    VALUES('delete', old.rowid, old.title, COALESCE(old.content_plain, old.content), old.tags);
                END
            """)

            db.execSQL("""
                CREATE TRIGGER IF NOT EXISTS entries_au AFTER UPDATE ON entries BEGIN
                    INSERT INTO entries_fts(entries_fts, docid, title, content, tags)
                    VALUES('delete', old.rowid, old.title, COALESCE(old.content_plain, old.content), old.tags);
                    INSERT INTO entries_fts(docid, title, content, tags)
                    SELECT new.rowid, new.title, COALESCE(new.content_plain, new.content), new.tags
                    WHERE new.deleted_at IS NULL;
                END
            """)
        }
    }
}
