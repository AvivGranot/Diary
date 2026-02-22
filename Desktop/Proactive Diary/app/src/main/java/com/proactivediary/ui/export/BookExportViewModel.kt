package com.proactivediary.ui.export

import android.content.ContentValues
import android.content.Context
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proactivediary.analytics.AnalyticsService
import com.proactivediary.data.db.dao.EntryDao
import com.proactivediary.data.db.dao.PreferenceDao
import com.proactivediary.data.db.entities.EntryEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

sealed class ExportState {
    object Idle : ExportState()
    data class Generating(val progress: Float) : ExportState()
    data class Success(val message: String) : ExportState()
    data class Error(val message: String) : ExportState()
}

data class BookExportUiState(
    val year: Int = LocalDate.now().year,
    val availableYears: List<Int> = listOf(LocalDate.now().year),
    val totalEntries: Int = 0,
    val totalWords: Int = 0,
    val longestStreak: Int = 0,
    val averageWordsPerEntry: Int = 0,
    val monthsWithEntries: Int = 0,
    val exportState: ExportState = ExportState.Idle,
    val isLoading: Boolean = true
)

@HiltViewModel
class BookExportViewModel @Inject constructor(
    private val entryDao: EntryDao,
    private val preferenceDao: PreferenceDao,
    private val analyticsService: AnalyticsService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookExportUiState())
    val uiState: StateFlow<BookExportUiState> = _uiState

    init {
        loadYearStats(_uiState.value.year)
    }

    fun selectYear(year: Int) {
        _uiState.update { it.copy(year = year, isLoading = true) }
        loadYearStats(year)
    }

    private fun loadYearStats(year: Int) {
        viewModelScope.launch {
            val allEntries = withContext(Dispatchers.IO) { entryDao.getAllSync() }

            // Find all available years
            val availableYears = allEntries
                .map { entryYear(it) }
                .distinct()
                .sorted()
                .reversed()
                .ifEmpty { listOf(LocalDate.now().year) }

            // Filter to selected year
            val yearEntries = allEntries.filter { entryYear(it) == year }

            val totalEntries = yearEntries.size
            val totalWords = yearEntries.sumOf { it.wordCount }
            val averageWords = if (totalEntries > 0) totalWords / totalEntries else 0
            val monthsWithEntries = yearEntries
                .map { entryMonth(it) }
                .distinct()
                .size

            val longestStreak = calculateLongestStreak(yearEntries)

            _uiState.update {
                it.copy(
                    year = year,
                    availableYears = availableYears,
                    totalEntries = totalEntries,
                    totalWords = totalWords,
                    longestStreak = longestStreak,
                    averageWordsPerEntry = averageWords,
                    monthsWithEntries = monthsWithEntries,
                    isLoading = false
                )
            }
        }
    }

    fun generateBook() {
        val state = _uiState.value
        if (state.totalEntries == 0) return

        _uiState.update { it.copy(exportState = ExportState.Generating(0f)) }

        viewModelScope.launch {
            try {
                val allEntries = withContext(Dispatchers.IO) { entryDao.getAllSync() }
                val yearEntries = allEntries
                    .filter { entryYear(it) == state.year }
                    .sortedBy { it.createdAt }

                val colorKey = withContext(Dispatchers.IO) {
                    preferenceDao.get("diary_color")?.value ?: "sky"
                }
                val userName = withContext(Dispatchers.IO) {
                    preferenceDao.get("diary_mark_text")?.value ?: ""
                }

                val config = BookConfig(
                    year = state.year,
                    userName = userName,
                    colorKey = colorKey,
                    entries = yearEntries,
                    totalWords = state.totalWords,
                    totalEntries = state.totalEntries,
                    longestStreak = state.longestStreak,
                    onProgress = { progress ->
                        _uiState.update { it.copy(exportState = ExportState.Generating(progress)) }
                    }
                )

                val pdfDocument = withContext(Dispatchers.IO) {
                    BookGenerator(context, config).generate()
                }

                val filename = "Proactive_Diary_${state.year}.pdf"
                withContext(Dispatchers.IO) {
                    savePdfToDownloads(pdfDocument, filename)
                    pdfDocument.close()
                }

                analyticsService.logBookExported(state.year, state.totalEntries)
                _uiState.update { it.copy(exportState = ExportState.Success("Book saved to Downloads")) }
            } catch (e: Exception) {
                _uiState.update { it.copy(exportState = ExportState.Error("Export failed: ${e.message}")) }
            }
        }
    }

    private fun savePdfToDownloads(pdfDoc: PdfDocument, filename: String) {
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, filename)
            put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val uri = context.contentResolver.insert(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            values
        ) ?: throw Exception("Could not create file in Downloads")

        context.contentResolver.openOutputStream(uri)?.use { os ->
            pdfDoc.writeTo(os)
        } ?: throw Exception("Could not open output stream")
    }

    private fun calculateLongestStreak(entries: List<EntryEntity>): Int {
        if (entries.isEmpty()) return 0

        val dates = entries
            .map { Instant.ofEpochMilli(it.createdAt).atZone(ZoneId.systemDefault()).toLocalDate() }
            .distinct()
            .sorted()

        var longest = 1
        var current = 1
        for (i in 1 until dates.size) {
            if (dates[i] == dates[i - 1].plusDays(1)) {
                current++
                if (current > longest) longest = current
            } else {
                current = 1
            }
        }
        return longest
    }

    private fun entryYear(entry: EntryEntity): Int {
        return Instant.ofEpochMilli(entry.createdAt)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .year
    }

    private fun entryMonth(entry: EntryEntity): Int {
        return Instant.ofEpochMilli(entry.createdAt)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .monthValue
    }
}
