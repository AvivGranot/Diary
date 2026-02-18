package com.proactivediary.di

import com.proactivediary.data.db.dao.EntryDao
import com.proactivediary.data.db.dao.GoalCheckInDao
import com.proactivediary.data.db.dao.GoalDao
import com.proactivediary.data.db.dao.WritingReminderDao
import com.proactivediary.data.media.ImageStorageManager
import com.proactivediary.data.repository.EntryRepository
import com.proactivediary.data.repository.EntryRepositoryImpl
import com.proactivediary.data.repository.GoalRepository
import com.proactivediary.data.repository.GoalRepositoryImpl
import com.proactivediary.data.repository.ReminderRepository
import com.proactivediary.data.repository.ReminderRepositoryImpl
import com.proactivediary.data.sync.SyncService
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
        entryDao: EntryDao,
        imageStorageManager: ImageStorageManager,
        syncService: SyncService
    ): EntryRepository = EntryRepositoryImpl(entryDao, imageStorageManager, syncService)

    @Provides
    @Singleton
    fun provideGoalRepository(
        goalDao: GoalDao,
        goalCheckInDao: GoalCheckInDao,
        syncService: SyncService
    ): GoalRepository = GoalRepositoryImpl(goalDao, goalCheckInDao, syncService)

    @Provides
    @Singleton
    fun provideReminderRepository(
        reminderDao: WritingReminderDao,
        syncService: SyncService
    ): ReminderRepository = ReminderRepositoryImpl(reminderDao, syncService)

    @Provides
    @Singleton
    fun provideSearchEngine(): SearchEngine = SearchEngineImpl()
}
