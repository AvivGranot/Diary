package com.proactivediary.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.proactivediary.data.db.entities.InsightEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InsightDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(insight: InsightEntity)

    @Query("SELECT * FROM insights ORDER BY week_start DESC LIMIT 1")
    fun getLatestInsight(): Flow<InsightEntity?>

    @Query("SELECT * FROM insights WHERE week_start = :weekStart LIMIT 1")
    suspend fun getInsightForWeek(weekStart: Long): InsightEntity?

    @Query("SELECT * FROM insights ORDER BY week_start DESC")
    fun getAllInsights(): Flow<List<InsightEntity>>

    @Query("DELETE FROM insights WHERE week_start < :beforeTimestamp")
    suspend fun deleteOlderThan(beforeTimestamp: Long)
}
