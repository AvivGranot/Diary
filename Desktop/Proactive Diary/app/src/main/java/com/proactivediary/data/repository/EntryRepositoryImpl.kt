package com.proactivediary.data.repository

import android.database.sqlite.SQLiteException
import androidx.sqlite.db.SimpleSQLiteQuery
import com.proactivediary.data.db.dao.EntryDao
import com.proactivediary.data.db.entities.EntryEntity
import com.proactivediary.data.media.ImageStorageManager
import com.proactivediary.data.sync.SyncService
import com.proactivediary.data.sync.SyncStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

class EntryRepositoryImpl @Inject constructor(
    private val entryDao: EntryDao,
    private val imageStorageManager: ImageStorageManager,
    private val syncService: SyncService
) : EntryRepository {

    override fun getAllEntries(): Flow<List<EntryEntity>> =
        entryDao.getAllOrderByCreatedAtDesc()

    override fun getEntryById(id: String): Flow<EntryEntity?> =
        entryDao.getById(id)

    override fun getTodayEntry(): Flow<EntryEntity?> {
        val (start, end) = todayRange()
        return entryDao.getByDateRange(start, end)
    }

    override fun getEntriesByDateRange(start: Long, end: Long): Flow<List<EntryEntity>> =
        entryDao.getEntriesByDateRange(start, end)

    override fun searchContent(ftsQuery: String): Flow<List<EntryEntity>> {
        val query = SimpleSQLiteQuery(
            "SELECT entries.* FROM entries JOIN entries_fts ON entries.rowid = entries_fts.docid WHERE entries_fts MATCH ? AND entries.sync_status != 2 ORDER BY entries.created_at DESC LIMIT 50",
            arrayOf(ftsQuery)
        )
        return entryDao.searchFts(query)
    }

    override suspend fun getByIdSync(id: String): EntryEntity? =
        entryDao.getByIdSync(id)

    override suspend fun getTodayEntrySync(): EntryEntity? {
        val (start, end) = todayRange()
        return entryDao.getByDateRangeSync(start, end)
    }

    override suspend fun insert(entry: EntryEntity) {
        entryDao.insert(entry)
        // Fire-and-forget cloud push
        CoroutineScope(Dispatchers.IO).launch {
            try { syncService.pushEntry(entry) } catch (_: Exception) { }
        }
    }

    override suspend fun update(entry: EntryEntity) {
        entryDao.update(entry)
        // Fire-and-forget cloud push
        CoroutineScope(Dispatchers.IO).launch {
            try { syncService.pushEntry(entry) } catch (_: Exception) { }
        }
    }

    override suspend fun delete(entryId: String) {
        // Clean up associated image files
        try { imageStorageManager.deleteAllImages(entryId) } catch (_: Exception) { }

        try {
            // Soft-delete: mark for deletion, push to cloud, then hard-delete
            entryDao.updateSyncStatus(entryId, SyncStatus.PENDING_DELETE)
            CoroutineScope(Dispatchers.IO).launch {
                try { syncService.pushEntryDeletion(entryId) } catch (_: Exception) { }
            }
        } catch (_: SQLiteException) {
            // FTS delete trigger fails (encoding/tokenization mismatch) — drop it and retry
            entryDao.rawExec(SimpleSQLiteQuery("DROP TRIGGER IF EXISTS entries_ad"))
            entryDao.deleteById(entryId)
            // Rebuild FTS to clean up stale entries
            try {
                entryDao.rawExec(SimpleSQLiteQuery("INSERT INTO entries_fts(entries_fts) VALUES('rebuild')"))
            } catch (_: Exception) { }
        }
    }

    override suspend fun deleteAll() =
        entryDao.deleteAll()

    override suspend fun getAllSync(): List<EntryEntity> =
        entryDao.getAllSync()

    override suspend fun hasEntryToday(): Boolean {
        val (start, end) = todayRange()
        return entryDao.countEntriesForDay(start, end) > 0
    }

    override suspend fun getTotalEntryCount(): Int =
        entryDao.getTotalCount()

    override fun getTotalEntryCountSync(): Int =
        entryDao.getTotalCountSync()

    override fun getTotalWordCountSync(): Int =
        entryDao.getTotalWordCountSync()

    override suspend fun getEntryForDate(date: LocalDate): EntryEntity? {
        val zone = ZoneId.systemDefault()
        val start = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
        return entryDao.getEntryForDay(start, end)
    }

    override suspend fun getLastEntryTimestamp(): Long? =
        entryDao.getLastEntryTimestamp()

    override suspend fun getPage(limit: Int, offset: Int): List<EntryEntity> =
        entryDao.getPage(limit, offset)

    override suspend fun getTotalCount(): Int =
        entryDao.getTotalCount()

    override fun getEntryDatesInRange(startMs: Long, endMs: Long): Flow<List<Long>> =
        entryDao.getEntryDatesInRange(startMs, endMs)

    override fun getEntriesWithImages(): Flow<List<EntryEntity>> =
        entryDao.getEntriesWithImages()

    private fun todayRange(): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now()
        val start = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = today.atTime(LocalTime.MAX).atZone(zone).toInstant().toEpochMilli()
        return Pair(start, end)
    }
}
