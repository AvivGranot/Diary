package com.proactivediary.di

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.proactivediary.analytics.ExperimentOverrideStore
import com.proactivediary.analytics.ExperimentService
import com.proactivediary.analytics.FirebaseExperimentService
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

    @Provides
    @Singleton
    fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig = Firebase.remoteConfig

    @Provides
    @Singleton
    fun provideFirebaseAnalytics(): FirebaseAnalytics = Firebase.analytics

    @Provides
    @Singleton
    fun provideExperimentService(
        remoteConfig: FirebaseRemoteConfig,
        analytics: FirebaseAnalytics,
        overrideStore: ExperimentOverrideStore
    ): ExperimentService = FirebaseExperimentService(remoteConfig, analytics, overrideStore)
}
