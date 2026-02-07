package com.proactivediary.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.proactivediary.data.db.entities.GoalCheckInEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalCheckInDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(checkIn: GoalCheckInEntity)

    @Query("DELETE FROM goal_checkins WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM goal_checkins")
    suspend fun deleteAll()

    @Query("SELECT * FROM goal_checkins WHERE goal_id = :goalId ORDER BY date DESC")
    fun getCheckInsForGoal(goalId: String): Flow<List<GoalCheckInEntity>>

    @Query("SELECT * FROM goal_checkins WHERE goal_id = :goalId ORDER BY date DESC")
    suspend fun getCheckInsForGoalSync(goalId: String): List<GoalCheckInEntity>

    @Query("SELECT * FROM goal_checkins WHERE goal_id = :goalId AND date = :date LIMIT 1")
    suspend fun getCheckInForDate(goalId: String, date: String): GoalCheckInEntity?

    @Query("SELECT * FROM goal_checkins WHERE goal_id = :goalId AND completed = 1 ORDER BY date DESC")
    suspend fun getCompletedCheckIns(goalId: String): List<GoalCheckInEntity>

    @Query("SELECT COUNT(*) FROM goal_checkins WHERE goal_id = :goalId AND completed = 1 AND date BETWEEN :startDate AND :endDate")
    suspend fun countCompletedInRange(goalId: String, startDate: String, endDate: String): Int
}
