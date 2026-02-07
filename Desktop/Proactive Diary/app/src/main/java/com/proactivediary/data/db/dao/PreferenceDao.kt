package com.proactivediary.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.proactivediary.data.db.entities.PreferenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PreferenceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(preference: PreferenceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSync(preference: PreferenceEntity)

    @Query("SELECT * FROM preferences WHERE `key` = :key")
    suspend fun get(key: String): PreferenceEntity?

    @Query("SELECT * FROM preferences WHERE `key` = :key")
    fun getSync(key: String): PreferenceEntity?

    @Query("SELECT * FROM preferences WHERE `key` = :key")
    fun observe(key: String): Flow<PreferenceEntity?>

    @Query("DELETE FROM preferences WHERE `key` = :key")
    suspend fun delete(key: String)

    @Query("DELETE FROM preferences")
    suspend fun deleteAll()

    @Query("SELECT * FROM preferences")
    suspend fun getAll(): List<PreferenceEntity>
}
