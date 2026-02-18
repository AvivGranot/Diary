package com.proactivediary.data.repository

import com.proactivediary.data.db.dao.GoalCheckInDao
import com.proactivediary.data.db.dao.GoalDao
import com.proactivediary.data.db.entities.GoalCheckInEntity
import com.proactivediary.data.db.entities.GoalEntity
import com.proactivediary.data.sync.SyncService
import com.proactivediary.data.sync.SyncStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoalRepositoryImpl @Inject constructor(
    private val goalDao: GoalDao,
    private val goalCheckInDao: GoalCheckInDao,
    private val syncService: SyncService
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

    override suspend fun insertGoal(goal: GoalEntity) {
        goalDao.insert(goal)
        CoroutineScope(Dispatchers.IO).launch {
            try { syncService.pushGoal(goal) } catch (_: Exception) { }
        }
    }

    override suspend fun updateGoal(goal: GoalEntity) {
        goalDao.update(goal)
        CoroutineScope(Dispatchers.IO).launch {
            try { syncService.pushGoal(goal) } catch (_: Exception) { }
        }
    }

    override suspend fun deleteGoal(goalId: String) {
        goalDao.updateSyncStatus(goalId, SyncStatus.PENDING_DELETE)
        CoroutineScope(Dispatchers.IO).launch {
            try { syncService.pushGoalDeletion(goalId) } catch (_: Exception) { }
        }
    }

    override suspend fun insertCheckIn(checkIn: GoalCheckInEntity) {
        goalCheckInDao.insert(checkIn)
        CoroutineScope(Dispatchers.IO).launch {
            try { syncService.pushCheckIn(checkIn) } catch (_: Exception) { }
        }
    }

    override suspend fun deleteAllGoals() =
        goalDao.deleteAll()

    override suspend fun deleteAllCheckIns() =
        goalCheckInDao.deleteAll()

    override suspend fun getActiveGoalCount(): Int =
        goalDao.getActiveGoalsSync().size
}
