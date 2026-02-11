package com.proactivediary.data.repository

import com.proactivediary.data.db.entities.GoalCheckInEntity
import com.proactivediary.data.db.entities.GoalEntity
import kotlinx.coroutines.flow.Flow

interface GoalRepository {
    fun getActiveGoals(): Flow<List<GoalEntity>>
    fun getAllGoals(): Flow<List<GoalEntity>>
    fun getCheckInsForGoal(goalId: String): Flow<List<GoalCheckInEntity>>
    suspend fun getByIdSync(id: String): GoalEntity?
    suspend fun getActiveGoalsSync(): List<GoalEntity>
    suspend fun getCheckInForDate(goalId: String, date: String): GoalCheckInEntity?
    suspend fun getCompletedCheckIns(goalId: String): List<GoalCheckInEntity>
    suspend fun countCompletedInRange(goalId: String, startDate: String, endDate: String): Int
    suspend fun insertGoal(goal: GoalEntity)
    suspend fun updateGoal(goal: GoalEntity)
    suspend fun deleteGoal(goalId: String)
    suspend fun insertCheckIn(checkIn: GoalCheckInEntity)
    suspend fun deleteAllGoals()
    suspend fun deleteAllCheckIns()
    suspend fun getActiveGoalCount(): Int
}
