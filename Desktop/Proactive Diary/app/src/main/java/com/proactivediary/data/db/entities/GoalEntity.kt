package com.proactivediary.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.proactivediary.data.sync.SyncStatus

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String? = null,
    val frequency: String = "daily",
    @ColumnInfo(name = "reminder_time") val reminderTime: String,
    @ColumnInfo(name = "reminder_days") val reminderDays: String = "[0,1,2,3,4,5,6]",
    @ColumnInfo(name = "is_active") val isActive: Boolean = true,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "sync_status") val syncStatus: Int = SyncStatus.PENDING_UPLOAD
)
