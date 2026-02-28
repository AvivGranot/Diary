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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Provides
    @Singleton
    fun provideEntryRepository(
        entryDao: EntryDao,
        imageStorageManager: ImageStorageManager,
        syncService: SyncService,
        @ApplicationScope appScope: CoroutineScope
    ): EntryRepository = EntryRepositoryImpl(entryDao, imageStorageManager, syncService, appScope)

    @Provides
    @Singleton
    fun provideGoalRepository(
        goalDao: GoalDao,
        goalCheckInDao: GoalCheckInDao,
        syncService: SyncService,
        @ApplicationScope appScope: CoroutineScope
    ): GoalRepository = GoalRepositoryImpl(goalDao, goalCheckInDao, syncService, appScope)

    @Provides
    @Singleton
    fun provideReminderRepository(
        reminderDao: WritingReminderDao,
        syncService: SyncService,
        @ApplicationScope appScope: CoroutineScope
    ): ReminderRepository = ReminderRepositoryImpl(reminderDao, syncService, appScope)

    @Provides
    @Singleton
    fun provideSearchEngine(): SearchEngine = SearchEngineImpl()
}
