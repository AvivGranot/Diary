package com.proactivediary.data.repository

import com.proactivediary.data.db.dao.WritingReminderDao
import com.proactivediary.data.db.entities.WritingReminderEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ReminderRepositoryImpl @Inject constructor(
    private val reminderDao: WritingReminderDao
) : ReminderRepository {

    override fun getActiveReminders(): Flow<List<WritingReminderEntity>> =
        reminderDao.getActiveReminders()

    override fun getAllReminders(): Flow<List<WritingReminderEntity>> =
        reminderDao.getAllReminders()

    override fun getActiveCount(): Flow<Int> =
        reminderDao.getActiveCount()

    override suspend fun getActiveRemindersSync(): List<WritingReminderEntity> =
        reminderDao.getActiveRemindersSync()

    override suspend fun insert(reminder: WritingReminderEntity) =
        reminderDao.insert(reminder)

    override suspend fun update(reminder: WritingReminderEntity) =
        reminderDao.update(reminder)

    override suspend fun delete(reminderId: String) =
        reminderDao.deleteById(reminderId)

    override suspend fun deleteAll() =
        reminderDao.deleteAll()
}
