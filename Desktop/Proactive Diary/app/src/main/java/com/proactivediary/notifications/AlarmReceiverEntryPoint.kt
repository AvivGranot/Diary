package com.proactivediary.notifications

import com.proactivediary.analytics.ExperimentService
import com.proactivediary.data.repository.StreakRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AlarmReceiverEntryPoint {
    fun streakRepository(): StreakRepository
    fun experimentService(): ExperimentService
}
