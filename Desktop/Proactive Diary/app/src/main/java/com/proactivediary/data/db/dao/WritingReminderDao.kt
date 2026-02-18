package com.proactivediary.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.proactivediary.data.db.entities.WritingReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WritingReminderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: WritingReminderEntity)

    @Update
    suspend fun update(reminder: WritingReminderEntity)

    @Query("DELETE FROM writing_reminders WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM writing_reminders")
    suspend fun deleteAll()

    @Query("SELECT * FROM writing_reminders WHERE sync_status != 2 AND is_active = 1")
    fun getActiveReminders(): Flow<List<WritingReminderEntity>>

    @Query("SELECT * FROM writing_reminders WHERE sync_status != 2 AND is_active = 1")
    suspend fun getActiveRemindersSync(): List<WritingReminderEntity>

    @Query("SELECT * FROM writing_reminders WHERE sync_status != 2 ORDER BY time ASC")
    fun getAllReminders(): Flow<List<WritingReminderEntity>>

    @Query("SELECT COUNT(*) FROM writing_reminders WHERE sync_status != 2 AND is_active = 1")
    fun getActiveCount(): Flow<Int>

    // ── Sync methods ──

    @Query("SELECT * FROM writing_reminders WHERE sync_status != 0")
    suspend fun getPendingSyncReminders(): List<WritingReminderEntity>

    @Query("UPDATE writing_reminders SET sync_status = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: Int)

    @Query("UPDATE writing_reminders SET sync_status = 0")
    suspend fun markAllSynced()

    @Query("DELETE FROM writing_reminders WHERE sync_status = 2 AND id = :id")
    suspend fun hardDelete(id: String)
}
