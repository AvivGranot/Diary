package com.proactivediary.data.repository

import com.proactivediary.data.db.dao.WritingReminderDao
import com.proactivediary.data.db.entities.WritingReminderEntity
import com.proactivediary.data.sync.SyncService
import com.proactivediary.data.sync.SyncStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class ReminderRepositoryImpl @Inject constructor(
    private val reminderDao: WritingReminderDao,
    private val syncService: SyncService
) : ReminderRepository {

    override fun getActiveReminders(): Flow<List<WritingReminderEntity>> =
        reminderDao.getActiveReminders()

    override fun getAllReminders(): Flow<List<WritingReminderEntity>> =
        reminderDao.getAllReminders()

    override fun getActiveCount(): Flow<Int> =
        reminderDao.getActiveCount()

    override suspend fun getActiveRemindersSync(): List<WritingReminderEntity> =
        reminderDao.getActiveRemindersSync()

    override suspend fun insert(reminder: WritingReminderEntity) {
        reminderDao.insert(reminder)
        CoroutineScope(Dispatchers.IO).launch {
            try { syncService.pushReminder(reminder) } catch (_: Exception) { }
        }
    }

    override suspend fun update(reminder: WritingReminderEntity) {
        reminderDao.update(reminder)
        CoroutineScope(Dispatchers.IO).launch {
            try { syncService.pushReminder(reminder) } catch (_: Exception) { }
        }
    }

    override suspend fun delete(reminderId: String) {
        reminderDao.updateSyncStatus(reminderId, SyncStatus.PENDING_DELETE)
        CoroutineScope(Dispatchers.IO).launch {
            try { syncService.pushReminderDeletion(reminderId) } catch (_: Exception) { }
        }
    }

    override suspend fun deleteAll() =
        reminderDao.deleteAll()
}
