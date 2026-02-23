package com.proactivediary.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.proactivediary.data.db.AppDatabase
import com.proactivediary.data.db.dao.EntryDao
import com.proactivediary.data.db.dao.GoalCheckInDao
import com.proactivediary.data.db.dao.GoalDao
import com.proactivediary.data.db.dao.PreferenceDao
import com.proactivediary.data.db.dao.ActivitySignalDao
import com.proactivediary.data.db.dao.InsightDao
import com.proactivediary.data.db.dao.JournalDao
import com.proactivediary.data.db.dao.MoodCheckInDao
import com.proactivediary.data.db.dao.TemplateDao
import com.proactivediary.data.db.dao.WritingReminderDao
import com.proactivediary.security.EncryptedDatabaseKeyManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SupportFactory
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val TAG = "DatabaseModule"
    private const val ENCRYPTED_DB_NAME = "proactive_diary.enc.db"

    @Provides
    @Singleton
    fun provideEncryptedDatabaseKeyManager(
        @ApplicationContext context: Context
    ): EncryptedDatabaseKeyManager = EncryptedDatabaseKeyManager(context)

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        keyManager: EncryptedDatabaseKeyManager
    ): AppDatabase {
        val passphrase = keyManager.getOrCreateDatabaseKey()
        val factory = SupportFactory(passphrase)

        // One-time migration: plain DB → encrypted DB
        migrateToEncryptedIfNeeded(context, passphrase)

        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            ENCRYPTED_DB_NAME
        )
            .openHelperFactory(factory)
            .addCallback(AppDatabase.createCallback())
            .addMigrations(*AppDatabase.MIGRATIONS)
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()
    }

    /**
     * If the old plain DB exists but the encrypted DB does not,
     * uses sqlcipher_export to migrate data to the encrypted database.
     */
    private fun migrateToEncryptedIfNeeded(context: Context, passphrase: ByteArray) {
        val plainDbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)
        val encryptedDbFile = context.getDatabasePath(ENCRYPTED_DB_NAME)

        if (!plainDbFile.exists() || encryptedDbFile.exists()) return

        Log.i(TAG, "Migrating plain database to encrypted...")

        try {
            // Open the plain DB with empty passphrase (no encryption)
            val plainDb = net.sqlcipher.database.SQLiteDatabase.openDatabase(
                plainDbFile.absolutePath,
                "",
                null,
                net.sqlcipher.database.SQLiteDatabase.OPEN_READWRITE
            )

            // Attach the new encrypted DB and export
            val passphraseStr = String(passphrase, Charsets.ISO_8859_1)
            plainDb.rawExecSQL(
                "ATTACH DATABASE '${encryptedDbFile.absolutePath}' AS encrypted KEY '${passphraseStr.replace("'", "''")}'"
            )
            plainDb.rawExecSQL("SELECT sqlcipher_export('encrypted')")
            plainDb.rawExecSQL("DETACH DATABASE encrypted")
            plainDb.close()

            // Delete old plain DB files
            plainDbFile.delete()
            File(plainDbFile.absolutePath + "-wal").delete()
            File(plainDbFile.absolutePath + "-shm").delete()
            File(plainDbFile.absolutePath + "-journal").delete()

            Log.i(TAG, "Migration to encrypted database complete.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to migrate to encrypted database: ${e.message}", e)
            // If migration fails, delete the possibly-corrupt encrypted file
            // so Room can create a fresh encrypted DB
            encryptedDbFile.delete()
        }
    }

    @Provides
    fun provideEntryDao(database: AppDatabase): EntryDao = database.entryDao()

    @Provides
    fun provideGoalDao(database: AppDatabase): GoalDao = database.goalDao()

    @Provides
    fun provideGoalCheckInDao(database: AppDatabase): GoalCheckInDao = database.goalCheckInDao()

    @Provides
    fun provideWritingReminderDao(database: AppDatabase): WritingReminderDao = database.writingReminderDao()

    @Provides
    fun providePreferenceDao(database: AppDatabase): PreferenceDao = database.preferenceDao()

    @Provides
    fun provideTemplateDao(database: AppDatabase): TemplateDao = database.templateDao()

    @Provides
    fun provideInsightDao(database: AppDatabase): InsightDao = database.insightDao()

    @Provides
    fun provideMoodCheckInDao(database: AppDatabase): MoodCheckInDao = database.moodCheckInDao()

    @Provides
    fun provideJournalDao(database: AppDatabase): JournalDao = database.journalDao()

    @Provides
    fun provideActivitySignalDao(database: AppDatabase): ActivitySignalDao = database.activitySignalDao()
}
