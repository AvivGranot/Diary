package com.proactivediary.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.proactivediary.data.db.entities.GoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: GoalEntity)

    @Update
    suspend fun update(goal: GoalEntity)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM goals")
    suspend fun deleteAll()

    @Query("SELECT * FROM goals WHERE id = :id")
    fun getById(id: String): Flow<GoalEntity?>

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getByIdSync(id: String): GoalEntity?

    @Query("SELECT * FROM goals WHERE is_active = 1 ORDER BY created_at DESC")
    fun getActiveGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals ORDER BY created_at DESC")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE is_active = 1")
    suspend fun getActiveGoalsSync(): List<GoalEntity>
}
