package com.proactivediary.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.proactivediary.data.sync.SyncStatus

@Entity(tableName = "writing_reminders")
data class WritingReminderEntity(
    @PrimaryKey val id: String,
    val time: String,
    val days: String = "[0,1,2,3,4,5,6]",
    @ColumnInfo(name = "is_active") val isActive: Boolean = true,
    @ColumnInfo(name = "fallback_enabled") val fallbackEnabled: Boolean = true,
    val label: String = "Write in your diary",
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "sync_status") val syncStatus: Int = SyncStatus.PENDING_UPLOAD
)
