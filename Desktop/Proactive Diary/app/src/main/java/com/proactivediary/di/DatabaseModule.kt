package com.proactivediary.di

import android.content.Context
import androidx.room.Room
import com.proactivediary.data.db.AppDatabase
import com.proactivediary.data.db.dao.EntryDao
import com.proactivediary.data.db.dao.GoalCheckInDao
import com.proactivediary.data.db.dao.GoalDao
import com.proactivediary.data.db.dao.PreferenceDao
import com.proactivediary.data.db.dao.WritingReminderDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .addCallback(AppDatabase.createCallback())
            .build()
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
}
