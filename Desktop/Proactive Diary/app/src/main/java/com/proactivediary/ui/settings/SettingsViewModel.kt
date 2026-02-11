package com.proactivediary.ui.settings

import android.content.ContentValues
import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proactivediary.R
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

    // Writing streak toggle
    private val _isStreakEnabled = MutableStateFlow(true)
    val isStreakEnabled: StateFlow<Boolean> = _isStreakEnabled

    // Delete confirmation state
    private val _deleteStep = MutableStateFlow(0) // 0=none, 1=first dialog, 2=type DELETE
    val deleteStep: StateFlow<Int> = _deleteStep

    init {
        loadStreakPref()
    }

    private fun loadStreakPref() {
        viewModelScope.launch {
            val pref = preferenceDao.get("streak_enabled")
            _isStreakEnabled.value = pref?.value != "false"
        }
    }

    fun setFontSize(size: String) {
        viewModelScope.launch {
            preferenceDao.insert(PreferenceEntity("font_size", size))
        }
    }

    fun toggleStreak(enabled: Boolean) {
        _isStreakEnabled.value = enabled
        viewModelScope.launch {
            preferenceDao.insert(PreferenceEntity("streak_enabled", if (enabled) "true" else "false"))
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
            if (entries.isEmpty()) {
                _exportMessage.value = "Nothing to export yet. Write your first entry!"
                return@launch
            }
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
            if (entries.isEmpty()) {
                _exportMessage.value = "Nothing to export yet. Write your first entry!"
                return@launch
            }
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

    fun exportPdf() {
        viewModelScope.launch {
            val entries = withContext(Dispatchers.IO) { entryDao.getAllSync() }
            if (entries.isEmpty()) {
                _exportMessage.value = "Nothing to export yet. Write your first entry!"
                return@launch
            }
            withContext(Dispatchers.IO) {
                try {
                    val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US)
                    val gson = Gson()
                    val tagsType = object : TypeToken<List<String>>() {}.type

                    val pageWidth = 595
                    val pageHeight = 842
                    val marginLeft = 56f
                    val marginTop = 56f
                    val marginBottom = 56f
                    val marginRight = 56f
                    val contentWidth = (pageWidth - marginLeft - marginRight).toInt()
                    val maxY = pageHeight - marginBottom

                    val serifFont = ResourcesCompat.getFont(context, R.font.cormorant_garamond_regular) ?: Typeface.SERIF
                    val serifItalic = ResourcesCompat.getFont(context, R.font.cormorant_garamond_italic) ?: Typeface.create(Typeface.SERIF, Typeface.ITALIC)
                    val sansFont = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)

                    val datePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                        typeface = serifItalic; textSize = 12f; color = 0xFF585858.toInt()
                    }
                    val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                        typeface = serifFont; textSize = 18f; color = 0xFF313131.toInt()
                    }
                    val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                        typeface = sansFont; textSize = 11f; color = 0xFF313131.toInt()
                    }
                    val tagPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                        typeface = sansFont; textSize = 9f; color = 0xFF888888.toInt()
                    }
                    val pageNumPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        typeface = serifFont; textSize = 10f; color = 0xFF585858.toInt()
                        textAlign = Paint.Align.CENTER
                    }

                    val doc = PdfDocument()
                    var pageNum = 0
                    var page: PdfDocument.Page? = null
                    var canvas: android.graphics.Canvas? = null
                    var y = maxY

                    fun startNewPage() {
                        page?.let { doc.finishPage(it) }
                        pageNum++
                        val info = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
                        page = doc.startPage(info)
                        canvas = page!!.canvas
                        val bg = Paint().apply { color = 0xFFFAF9F5.toInt() }
                        canvas!!.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), bg)
                        y = marginTop
                    }

                    fun drawPageNumber() {
                        canvas?.drawText(pageNum.toString(), pageWidth / 2f, pageHeight - 28f, pageNumPaint)
                    }

                    for ((index, entry) in entries.withIndex()) {
                        val dateStr = dateFormat.format(Date(entry.createdAt))
                        val tags: List<String> = try { gson.fromJson(entry.tags, tagsType) } catch (_: Exception) { emptyList() }

                        if (y + 100f > maxY) {
                            if (page != null) drawPageNumber()
                            startNewPage()
                        }

                        canvas!!.drawText(dateStr, marginLeft, y + 12f, datePaint)
                        y += 20f

                        if (entry.title.isNotBlank()) {
                            val titleLayout = StaticLayout.Builder.obtain(entry.title, 0, entry.title.length, titlePaint, contentWidth)
                                .setLineSpacing(0f, 1.2f).build()
                            canvas!!.save()
                            canvas!!.translate(marginLeft, y)
                            titleLayout.draw(canvas!!)
                            canvas!!.restore()
                            y += titleLayout.height + 4f
                        }

                        val bodyLayout = StaticLayout.Builder.obtain(entry.content, 0, entry.content.length, bodyPaint, contentWidth)
                            .setLineSpacing(0f, 1.4f).build()
                        val totalLines = bodyLayout.lineCount
                        var lineIndex = 0
                        while (lineIndex < totalLines) {
                            if (y + 16f > maxY) {
                                drawPageNumber()
                                startNewPage()
                            }
                            val lineTop = bodyLayout.getLineTop(lineIndex).toFloat()
                            val lineBottom = bodyLayout.getLineBottom(lineIndex).toFloat()
                            val lineHeight = lineBottom - lineTop
                            canvas!!.save()
                            canvas!!.translate(marginLeft, y - lineTop)
                            canvas!!.clipRect(0f, lineTop, contentWidth.toFloat(), lineBottom)
                            bodyLayout.draw(canvas!!)
                            canvas!!.restore()
                            y += lineHeight
                            lineIndex++
                        }

                        if (tags.isNotEmpty()) {
                            y += 4f
                            if (y + 14f > maxY) { drawPageNumber(); startNewPage() }
                            val tagStr = tags.joinToString("  ") { "#$it" }
                            canvas!!.drawText(tagStr, marginLeft, y + 10f, tagPaint)
                            y += 16f
                        }

                        if (index < entries.lastIndex) {
                            y += 12f
                            if (y + 20f > maxY) { drawPageNumber(); startNewPage() }
                            val divPaint = Paint().apply { color = 0xFFD0D0D0.toInt(); strokeWidth = 0.5f }
                            canvas!!.drawLine(marginLeft, y, marginLeft + contentWidth, y, divPaint)
                            y += 16f
                        }
                    }

                    if (page != null) {
                        drawPageNumber()
                        doc.finishPage(page!!)
                    }

                    val filename = "proactive_diary_export.pdf"
                    val values = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, filename)
                        put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                        put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }
                    val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    uri?.let {
                        context.contentResolver.openOutputStream(it)?.use { os ->
                            doc.writeTo(os)
                        }
                    }
                    doc.close()
                    _exportMessage.value = "Exported to Downloads/$filename"
                } catch (e: Exception) {
                    _exportMessage.value = "Export failed: ${e.message}"
                }
            }
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
                _exportMessage.value = "Exported to Downloads/$filename"
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
