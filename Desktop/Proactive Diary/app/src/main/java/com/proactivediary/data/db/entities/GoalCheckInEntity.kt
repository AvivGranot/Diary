package com.proactivediary.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.proactivediary.data.sync.SyncStatus

@Entity(
    tableName = "goal_checkins",
    indices = [
        Index("goal_id"),
        Index("date"),
        Index(value = ["goal_id", "date"], unique = true)
    ],
    foreignKeys = [ForeignKey(
        entity = GoalEntity::class,
        parentColumns = ["id"],
        childColumns = ["goal_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class GoalCheckInEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "goal_id") val goalId: String,
    val date: String,
    val completed: Boolean = false,
    val note: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "sync_status") val syncStatus: Int = SyncStatus.PENDING_UPLOAD
)
