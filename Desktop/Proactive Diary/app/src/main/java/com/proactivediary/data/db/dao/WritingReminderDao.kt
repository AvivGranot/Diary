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

    @Query("SELECT * FROM writing_reminders WHERE is_active = 1")
    fun getActiveReminders(): Flow<List<WritingReminderEntity>>

    @Query("SELECT * FROM writing_reminders WHERE is_active = 1")
    suspend fun getActiveRemindersSync(): List<WritingReminderEntity>

    @Query("SELECT * FROM writing_reminders ORDER BY time ASC")
    fun getAllReminders(): Flow<List<WritingReminderEntity>>

    @Query("SELECT COUNT(*) FROM writing_reminders WHERE is_active = 1")
    fun getActiveCount(): Flow<Int>
}
