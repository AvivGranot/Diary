package com.proactivediary.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery
import com.proactivediary.data.db.entities.EntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: EntryEntity)

    @Update
    suspend fun update(entry: EntryEntity)

    @Query("DELETE FROM entries WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM entries")
    suspend fun deleteAll()

    @Query("SELECT * FROM entries WHERE id = :id")
    fun getById(id: String): Flow<EntryEntity?>

    @Query("SELECT * FROM entries WHERE id = :id")
    suspend fun getByIdSync(id: String): EntryEntity?

    @Query("SELECT * FROM entries WHERE sync_status != 2 ORDER BY created_at DESC")
    fun getAllOrderByCreatedAtDesc(): Flow<List<EntryEntity>>

    @Query("SELECT * FROM entries WHERE sync_status != 2 ORDER BY created_at DESC")
    suspend fun getAllSync(): List<EntryEntity>

    @Query("SELECT * FROM entries WHERE sync_status != 2 AND created_at BETWEEN :startOfDay AND :endOfDay ORDER BY created_at DESC LIMIT 1")
    fun getByDateRange(startOfDay: Long, endOfDay: Long): Flow<EntryEntity?>

    @Query("SELECT * FROM entries WHERE sync_status != 2 AND created_at BETWEEN :startOfDay AND :endOfDay ORDER BY created_at DESC LIMIT 1")
    suspend fun getByDateRangeSync(startOfDay: Long, endOfDay: Long): EntryEntity?

    @Query("SELECT * FROM entries WHERE sync_status != 2 AND created_at BETWEEN :start AND :end ORDER BY created_at DESC")
    fun getEntriesByDateRange(start: Long, end: Long): Flow<List<EntryEntity>>

    @RawQuery(observedEntities = [EntryEntity::class])
    fun searchFts(query: SupportSQLiteQuery): Flow<List<EntryEntity>>

    @Query("SELECT COUNT(*) FROM entries WHERE sync_status != 2 AND created_at BETWEEN :startOfDay AND :endOfDay")
    suspend fun countEntriesForDay(startOfDay: Long, endOfDay: Long): Int

    @Query("SELECT * FROM entries WHERE sync_status != 2 ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    suspend fun getPage(limit: Int, offset: Int): List<EntryEntity>

    @Query("SELECT COUNT(*) FROM entries WHERE sync_status != 2")
    suspend fun getTotalCount(): Int

    @Query("SELECT COUNT(*) FROM entries WHERE sync_status != 2")
    fun getTotalCountSync(): Int

    @Query("SELECT COALESCE(SUM(word_count), 0) FROM entries WHERE sync_status != 2")
    fun getTotalWordCountSync(): Int

    @Query("SELECT * FROM entries WHERE sync_status != 2 AND created_at BETWEEN :startOfDay AND :endOfDay ORDER BY created_at DESC LIMIT 1")
    suspend fun getEntryForDay(startOfDay: Long, endOfDay: Long): EntryEntity?

    @Query("SELECT created_at FROM entries WHERE sync_status != 2 ORDER BY created_at DESC LIMIT 1")
    suspend fun getLastEntryTimestamp(): Long?

    @RawQuery
    suspend fun rawExec(query: SupportSQLiteQuery): Int

    @Query("SELECT created_at FROM entries WHERE sync_status != 2 AND created_at BETWEEN :startMs AND :endMs ORDER BY created_at ASC")
    fun getEntryDatesInRange(startMs: Long, endMs: Long): Flow<List<Long>>

    @Query("SELECT * FROM entries WHERE sync_status != 2 AND images != '[]' ORDER BY created_at DESC")
    fun getEntriesWithImages(): Flow<List<EntryEntity>>

    @Query("SELECT * FROM entries WHERE sync_status != 2 AND created_at BETWEEN :startMs AND :endMs ORDER BY created_at ASC")
    suspend fun getEntriesBetween(startMs: Long, endMs: Long): List<EntryEntity>

    // ── Sync methods ──

    @Query("SELECT * FROM entries WHERE sync_status != 0")
    suspend fun getPendingSyncEntries(): List<EntryEntity>

    @Query("UPDATE entries SET sync_status = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: Int)

    @Query("UPDATE entries SET sync_status = 0")
    suspend fun markAllSynced()

    @Query("DELETE FROM entries WHERE sync_status = 2 AND id = :id")
    suspend fun hardDelete(id: String)
}
