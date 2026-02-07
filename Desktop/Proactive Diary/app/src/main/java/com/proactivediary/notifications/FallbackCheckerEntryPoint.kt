package com.proactivediary.notifications

import com.proactivediary.data.db.dao.EntryDao
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface FallbackCheckerEntryPoint {
    fun entryDao(): EntryDao
}
