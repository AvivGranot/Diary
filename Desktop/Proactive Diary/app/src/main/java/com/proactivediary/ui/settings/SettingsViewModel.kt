package com.proactivediary.ui.settings

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.proactivediary.data.db.dao.EntryDao
import com.proactivediary.data.db.dao.GoalCheckInDao
import com.proactivediary.data.db.dao.GoalDao
import com.proactivediary.data.db.dao.PreferenceDao
import com.proactivediary.data.db.dao.WritingReminderDao
import com.proactivediary.data.db.entities.EntryEntity
import com.proactivediary.data.db.entities.PreferenceEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferenceDao: PreferenceDao,
    private val entryDao: EntryDao,
    private val goalDao: GoalDao,
    private val goalCheckInDao: GoalCheckInDao,
    private val writingReminderDao: WritingReminderDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // Dark mode
    val isDarkMode = preferenceDao.observe("dark_mode")
        .map { it?.value == "true" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Font size
    val fontSize = preferenceDao.observe("font_size")
        .map { it?.value ?: "medium" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "medium")

    // Diary design summary
    val diaryDesignSummary = preferenceDao.observe("diary_color")
        .map { pref ->
            val color = pref?.value?.replace("_", " ")?.replaceFirstChar { it.uppercase() } ?: "Cream"
            color
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Cream")

    // Active reminder count
    val activeReminderCount = writingReminderDao.getActiveCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Active goal count
    val activeGoalCount = goalDao.getActiveGoals()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Export status
    private val _exportMessage = MutableStateFlow<String?>(null)
    val exportMessage: StateFlow<String?> = _exportMessage

    // Delete confirmation state
    private val _deleteStep = MutableStateFlow(0) // 0=none, 1=first dialog, 2=type DELETE
    val deleteStep: StateFlow<Int> = _deleteStep

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            preferenceDao.insert(PreferenceEntity("dark_mode", if (enabled) "true" else "false"))
        }
    }

    fun setFontSize(size: String) {
        viewModelScope.launch {
            preferenceDao.insert(PreferenceEntity("font_size", size))
        }
    }

    fun startDeleteFlow() {
        _deleteStep.value = 1
    }

    fun confirmDeleteStep1() {
        _deleteStep.value = 2
    }

    fun cancelDelete() {
        _deleteStep.value = 0
    }

    fun executeDeleteAll(onComplete: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                entryDao.deleteAll()
                goalDao.deleteAll()
                goalCheckInDao.deleteAll()
                writingReminderDao.deleteAll()
                preferenceDao.deleteAll()
            }
            _deleteStep.value = 0
            onComplete()
        }
    }

    fun exportJson() {
        viewModelScope.launch {
            val entries = withContext(Dispatchers.IO) { entryDao.getAllSync() }
            val gson = Gson()
            val exportEntries = entries.map { entry ->
                val tagsType = object : TypeToken<List<String>>() {}.type
                val tags: List<String> = try {
                    gson.fromJson(entry.tags, tagsType)
                } catch (e: Exception) {
                    emptyList()
                }
                mapOf(
                    "id" to entry.id,
                    "title" to entry.title,
                    "content" to entry.content,
                    "mood" to entry.mood,
                    "tags" to tags,
                    "wordCount" to entry.wordCount,
                    "createdAt" to formatIso8601(entry.createdAt),
                    "updatedAt" to formatIso8601(entry.updatedAt)
                )
            }
            val json = gson.toJson(exportEntries)
            saveToDownloads("proactive_diary_export.json", json, "application/json")
        }
    }

    fun exportText() {
        viewModelScope.launch {
            val entries = withContext(Dispatchers.IO) { entryDao.getAllSync() }
            val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US)
            val gson = Gson()
            val tagsType = object : TypeToken<List<String>>() {}.type

            val text = entries.joinToString("\n\n---\n\n") { entry ->
                val date = dateFormat.format(Date(entry.createdAt))
                val tags: List<String> = try {
                    gson.fromJson(entry.tags, tagsType)
                } catch (e: Exception) {
                    emptyList()
                }
                val moodDisplay = entry.mood?.replaceFirstChar { it.uppercase() } ?: "None"
                val tagsDisplay = if (tags.isNotEmpty()) tags.joinToString(", ") else "None"

                buildString {
                    appendLine(date)
                    if (entry.title.isNotBlank()) appendLine(entry.title)
                    appendLine()
                    appendLine(entry.content)
                    appendLine()
                    append("Mood: $moodDisplay | Words: ${entry.wordCount} | Tags: $tagsDisplay")
                }
            }
            saveToDownloads("proactive_diary_export.txt", text, "text/plain")
        }
    }

    private suspend fun saveToDownloads(filename: String, content: String, mimeType: String) {
        withContext(Dispatchers.IO) {
            try {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, filename)
                    put(MediaStore.Downloads.MIME_TYPE, mimeType)
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                val uri = context.contentResolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    values
                )

                uri?.let {
                    context.contentResolver.openOutputStream(it)?.use { os ->
                        os.write(content.toByteArray())
                    }
                }
                _exportMessage.value = "Diary exported to Downloads"
            } catch (e: Exception) {
                _exportMessage.value = "Export failed: ${e.message}"
            }
        }
    }

    fun clearExportMessage() {
        _exportMessage.value = null
    }

    private fun formatIso8601(millis: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
        return sdf.format(Date(millis))
    }
}
