package com.proactivediary.di

import com.proactivediary.data.db.dao.EntryDao
import com.proactivediary.data.db.dao.GoalCheckInDao
import com.proactivediary.data.db.dao.GoalDao
import com.proactivediary.data.db.dao.WritingReminderDao
import com.proactivediary.data.repository.EntryRepository
import com.proactivediary.data.repository.EntryRepositoryImpl
import com.proactivediary.data.repository.GoalRepository
import com.proactivediary.data.repository.GoalRepositoryImpl
import com.proactivediary.data.repository.ReminderRepository
import com.proactivediary.data.repository.ReminderRepositoryImpl
import com.proactivediary.domain.search.SearchEngine
import com.proactivediary.domain.search.SearchEngineImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideEntryRepository(
        entryDao: EntryDao
    ): EntryRepository = EntryRepositoryImpl(entryDao)

    @Provides
    @Singleton
    fun provideGoalRepository(
        goalDao: GoalDao,
        goalCheckInDao: GoalCheckInDao
    ): GoalRepository = GoalRepositoryImpl(goalDao, goalCheckInDao)

    @Provides
    @Singleton
    fun provideReminderRepository(
        reminderDao: WritingReminderDao
    ): ReminderRepository = ReminderRepositoryImpl(reminderDao)

    @Provides
    @Singleton
    fun provideSearchEngine(): SearchEngine = SearchEngineImpl()
}
