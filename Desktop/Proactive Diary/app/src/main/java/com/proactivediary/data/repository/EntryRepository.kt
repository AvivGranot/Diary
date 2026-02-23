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
    suspend fun getTotalEntryCount(): Int
    fun getTotalEntryCountSync(): Int
    fun getTotalWordCountSync(): Int
    suspend fun getEntryForDate(date: java.time.LocalDate): EntryEntity?
    suspend fun getLastEntryTimestamp(): Long?
    suspend fun getPage(limit: Int, offset: Int): List<EntryEntity>
    suspend fun getTotalCount(): Int
    fun getEntryDatesInRange(startMs: Long, endMs: Long): Flow<List<Long>>
    fun getEntriesWithImages(): Flow<List<EntryEntity>>
    // Phase 1: Soft delete & bookmarks
    suspend fun softDelete(entryId: String)
    suspend fun restore(entryId: String)
    suspend fun permanentDelete(entryId: String)
    suspend fun purgeExpired()
    fun getRecentlyDeleted(): Flow<List<EntryEntity>>
    suspend fun toggleBookmark(entryId: String, bookmarked: Boolean)
    fun getBookmarked(): Flow<List<EntryEntity>>
    suspend fun updateEntryDate(entryId: String, entryDate: Long?)
    // Phase 6: Location entries
    fun getEntriesWithLocation(): Flow<List<EntryEntity>>
}
