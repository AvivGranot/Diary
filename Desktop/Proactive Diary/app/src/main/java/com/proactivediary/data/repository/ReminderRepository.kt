package com.proactivediary.data.repository

import com.proactivediary.data.db.entities.WritingReminderEntity
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun getActiveReminders(): Flow<List<WritingReminderEntity>>
    fun getAllReminders(): Flow<List<WritingReminderEntity>>
    fun getActiveCount(): Flow<Int>
    suspend fun getActiveRemindersSync(): List<WritingReminderEntity>
    suspend fun insert(reminder: WritingReminderEntity)
    suspend fun update(reminder: WritingReminderEntity)
    suspend fun delete(reminderId: String)
    suspend fun deleteAll()
}
