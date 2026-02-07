package com.proactivediary.data.repository

import com.proactivediary.data.db.dao.GoalCheckInDao
import com.proactivediary.data.db.dao.GoalDao
import com.proactivediary.data.db.entities.GoalCheckInEntity
import com.proactivediary.data.db.entities.GoalEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GoalRepositoryImpl @Inject constructor(
    private val goalDao: GoalDao,
    private val goalCheckInDao: GoalCheckInDao
) : GoalRepository {

    override fun getActiveGoals(): Flow<List<GoalEntity>> =
        goalDao.getActiveGoals()

    override fun getAllGoals(): Flow<List<GoalEntity>> =
        goalDao.getAllGoals()

    override fun getCheckInsForGoal(goalId: String): Flow<List<GoalCheckInEntity>> =
        goalCheckInDao.getCheckInsForGoal(goalId)

    override suspend fun getByIdSync(id: String): GoalEntity? =
        goalDao.getByIdSync(id)

    override suspend fun getActiveGoalsSync(): List<GoalEntity> =
        goalDao.getActiveGoalsSync()

    override suspend fun getCheckInForDate(goalId: String, date: String): GoalCheckInEntity? =
        goalCheckInDao.getCheckInForDate(goalId, date)

    override suspend fun getCompletedCheckIns(goalId: String): List<GoalCheckInEntity> =
        goalCheckInDao.getCompletedCheckIns(goalId)

    override suspend fun countCompletedInRange(goalId: String, startDate: String, endDate: String): Int =
        goalCheckInDao.countCompletedInRange(goalId, startDate, endDate)

    override suspend fun insertGoal(goal: GoalEntity) =
        goalDao.insert(goal)

    override suspend fun updateGoal(goal: GoalEntity) =
        goalDao.update(goal)

    override suspend fun deleteGoal(goalId: String) =
        goalDao.deleteById(goalId)

    override suspend fun insertCheckIn(checkIn: GoalCheckInEntity) =
        goalCheckInDao.insert(checkIn)

    override suspend fun deleteAllGoals() =
        goalDao.deleteAll()

    override suspend fun deleteAllCheckIns() =
        goalCheckInDao.deleteAll()
}
