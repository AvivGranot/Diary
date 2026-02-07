package com.proactivediary.data.repository

import com.proactivediary.data.db.entities.EntryEntity
import kotlinx.coroutines.flow.Flow

interface EntryRepository {
    fun getAllEntries(): Flow<List<EntryEntity>>
    fun getEntryById(id: String): Flow<EntryEntity?>
    fun getTodayEntry(): Flow<EntryEntity?>
    fun getEntriesByDateRange(start: Long, end: Long): Flow<List<EntryEntity>>
    fun searchContent(ftsQuery: String): Flow<List<EntryEntity>>
    suspend fun getByIdSync(id: String): EntryEntity?
    suspend fun getTodayEntrySync(): EntryEntity?
    suspend fun insert(entry: EntryEntity)
    suspend fun update(entry: EntryEntity)
    suspend fun delete(entryId: String)
    suspend fun deleteAll()
    suspend fun getAllSync(): List<EntryEntity>
    suspend fun hasEntryToday(): Boolean
}
