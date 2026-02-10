package com.proactivediary.ui.export

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.StaticLayout
import android.text.TextPaint
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.res.ResourcesCompat
import com.proactivediary.R
import com.proactivediary.data.db.entities.EntryEntity
import com.proactivediary.domain.model.DiaryThemeConfig
import com.proactivediary.domain.model.Mood
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

data class BookConfig(
    val year: Int,
    val userName: String,
    val colorKey: String,
    val entries: List<EntryEntity>,
    val totalWords: Int,
    val totalEntries: Int,
    val longestStreak: Int,
    val mostCommonMood: String?,
    val onProgress: (Float) -> Unit
)

class BookGenerator(
    private val context: Context,
    private val config: BookConfig
) {
    companion object {
        // 5.5 x 8.5 inches at 72 dpi
        private const val PAGE_WIDTH = 396
        private const val PAGE_HEIGHT = 612
        private const val MARGIN_TOP = 72f
        private const val MARGIN_BOTTOM = 72f
        private const val MARGIN_LEFT = 54f
        private const val MARGIN_RIGHT = 54f
        private val CONTENT_WIDTH = PAGE_WIDTH - MARGIN_LEFT - MARGIN_RIGHT // 288
    }

    private val accentColor: Int = DiaryThemeConfig.colorForKey(config.colorKey).toArgb()
    private val textColor: Int = DiaryThemeConfig.textColorFor(config.colorKey).toArgb()
    private val secondaryTextColor: Int = DiaryThemeConfig.secondaryTextColorFor(config.colorKey).toArgb()
    private val isDark: Boolean = DiaryThemeConfig.isDarkColor(config.colorKey)

    // Fonts
    private val cormorantRegular: Typeface = ResourcesCompat.getFont(context, R.font.cormorant_garamond_regular)
        ?: Typeface.SERIF
    private val cormorantItalic: Typeface = ResourcesCompat.getFont(context, R.font.cormorant_garamond_italic)
        ?: Typeface.create(Typeface.SERIF, Typeface.ITALIC)
    private val sansSerif: Typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)

    // Paints
    private val coverTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = cormorantRegular
        textSize = 24f
        color = textColor
        textAlign = Paint.Align.CENTER
    }

    private val monogramPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = cormorantRegular
        textSize = 120f
        color = textColor
        textAlign = Paint.Align.CENTER
    }

    private val yearPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = cormorantRegular
        textSize = 18f
        color = secondaryTextColor
        textAlign = Paint.Align.CENTER
    }

    private val tocHeadingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = cormorantRegular
        textSize = 28f
        color = 0xFF313131.toInt()
    }

    private val tocEntryPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = cormorantRegular
        textSize = 14f
        color = 0xFF585858.toInt()
    }

    private val tocPageNumPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = cormorantRegular
        textSize = 14f
        color = 0xFF585858.toInt()
        textAlign = Paint.Align.RIGHT
    }

    private val dividerMonthPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = cormorantRegular
        textSize = 36f
        color = textColor
        textAlign = Paint.Align.CENTER
    }

    private val entryDatePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = cormorantItalic
        textSize = 14f
        color = 0xFF585858.toInt()
    }

    private val entryTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = cormorantRegular
        textSize = 20f
        color = 0xFF313131.toInt()
    }

    private val bodyTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = sansSerif
        textSize = 11f
        color = 0xFF313131.toInt()
    }

    private val pageNumberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = cormorantRegular
        textSize = 10f
        color = 0xFF585858.toInt()
        textAlign = Paint.Align.CENTER
    }

    private val statsHeadingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = cormorantRegular
        textSize = 28f
        color = 0xFF313131.toInt()
        textAlign = Paint.Align.CENTER
    }

    private val statsNumberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = cormorantRegular
        textSize = 48f
        color = 0xFF313131.toInt()
        textAlign = Paint.Align.CENTER
    }

    private val statsLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = sansSerif
        textSize = 11f
        color = 0xFF585858.toInt()
        textAlign = Paint.Align.CENTER
    }

    private val colophonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = cormorantItalic
        textSize = 12f
        color = 0xFF585858.toInt()
        textAlign = Paint.Align.CENTER
    }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = secondaryTextColor
        strokeWidth = 0.5f
    }

    // Group entries by month
    private val entriesByMonth: Map<Int, List<EntryEntity>> by lazy {
        config.entries.groupBy { entry ->
            Instant.ofEpochMilli(entry.createdAt)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .monthValue
        }.toSortedMap()
    }

    private val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.US)

    /**
     * Two-pass architecture:
     * Pass 1 — Measure all pages to determine page numbers for TOC.
     * Pass 2 — Render with correct page numbers.
     */
    fun generate(): PdfDocument {
        // Pass 1: measure page counts
        val monthPageNumbers = measurePageNumbers()

        // Pass 2: render
        val doc = PdfDocument()
        var pageNum = 0

        // Cover page
        pageNum++
        renderCoverPage(doc, pageNum)

        // Table of Contents
        pageNum++
        renderTocPage(doc, pageNum, monthPageNumbers)

        val totalMonths = entriesByMonth.size
        var monthsRendered = 0

        // Per-month content
        for ((month, entries) in entriesByMonth) {
            // Monthly divider page
            pageNum++
            renderMonthDividerPage(doc, pageNum, month)

            // Entry pages
            for (entry in entries) {
                pageNum = renderEntryPages(doc, pageNum, entry)
            }

            monthsRendered++
            config.onProgress(monthsRendered.toFloat() / totalMonths.coerceAtLeast(1) * 0.9f)
        }

        // Stats page
        pageNum++
        renderStatsPage(doc, pageNum)

        // Colophon
        pageNum++
        renderColophonPage(doc, pageNum)

        config.onProgress(1f)
        return doc
    }

    /**
     * Pass 1: Simulate page layout to determine which page each month starts on.
     * Returns a map of month number (1–12) to page number.
     */
    private fun measurePageNumbers(): Map<Int, Int> {
        val result = mutableMapOf<Int, Int>()
        var pageNum = 2 // page 1 = cover, page 2 = TOC

        for ((month, entries) in entriesByMonth) {
            pageNum++ // month divider page
            result[month] = pageNum

            // Count entry pages
            for (entry in entries) {
                pageNum += measureEntryPageCount(entry)
            }
        }
        return result
    }

    /**
     * Measures how many pages a single entry will occupy.
     */
    private fun measureEntryPageCount(entry: EntryEntity): Int {
        var pages = 1
        val availableHeight = PAGE_HEIGHT - MARGIN_TOP - MARGIN_BOTTOM - 20f // page number space

        // Header area
        var yOffset = 0f
        // Date line
        yOffset += 18f

        // Title
        if (entry.title.isNotBlank()) {
            val titleLayout = createStaticLayout(entry.title, entryTitleTextPaint(), CONTENT_WIDTH.toInt())
            yOffset += titleLayout.height + 8f
        }

        yOffset += 8f // spacing before body

        // Body text
        val bodyLayout = createStaticLayout(entry.content, bodyTextPaint, CONTENT_WIDTH.toInt())
        val bodyHeight = bodyLayout.height.toFloat()
        val remainingOnFirstPage = availableHeight - yOffset

        if (bodyHeight <= remainingOnFirstPage) {
            return 1
        }

        // Multi-page: figure out how many overflow pages
        var consumed = remainingOnFirstPage
        pages++
        while (consumed < bodyHeight) {
            consumed += availableHeight - 10f // subsequent pages have less header overhead
            if (consumed < bodyHeight) pages++
        }
        return pages
    }

    // ─── Page renderers ────────────────────────────────────

    private fun renderCoverPage(doc: PdfDocument, pageNum: Int) {
        val page = createPage(doc, pageNum)
        val canvas = page.canvas
        val centerX = PAGE_WIDTH / 2f

        // Accent color background
        val bgPaint = Paint().apply { color = accentColor }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), bgPaint)

        // Monogram letter
        val monogram = if (config.userName.isNotBlank()) config.userName.first().uppercaseChar().toString() else "P"
        canvas.drawText(monogram, centerX, 260f, monogramPaint)

        // Decorative line
        canvas.drawLine(centerX - 40f, 290f, centerX + 40f, 290f, Paint(linePaint).apply { color = textColor })

        // Title
        canvas.drawText("My Year in Writing", centerX, 340f, coverTitlePaint)

        // Year
        canvas.drawText(config.year.toString(), centerX, 380f, yearPaint)

        doc.finishPage(page)
    }

    private fun renderTocPage(doc: PdfDocument, pageNum: Int, monthPageNumbers: Map<Int, Int>) {
        val page = createPage(doc, pageNum)
        val canvas = page.canvas

        // White background
        val bgPaint = Paint().apply { color = 0xFFFAF9F5.toInt() }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), bgPaint)

        // "Contents" heading
        canvas.drawText("Contents", MARGIN_LEFT, MARGIN_TOP + 30f, tocHeadingPaint)

        var y = MARGIN_TOP + 70f
        val dotLeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 14f
            color = 0xFFA0A09E.toInt()
        }

        for ((month, startPage) in monthPageNumbers) {
            val monthName = java.time.Month.of(month).getDisplayName(TextStyle.FULL, Locale.US)
            val entryCount = entriesByMonth[month]?.size ?: 0
            val label = "$monthName ($entryCount ${if (entryCount == 1) "entry" else "entries"})"

            canvas.drawText(label, MARGIN_LEFT, y, tocEntryPaint)

            // Dot leader
            val labelWidth = tocEntryPaint.measureText(label)
            val pageNumStr = startPage.toString()
            val pageNumWidth = tocPageNumPaint.measureText(pageNumStr)
            val dotsStart = MARGIN_LEFT + labelWidth + 8f
            val dotsEnd = PAGE_WIDTH - MARGIN_RIGHT - pageNumWidth - 8f
            var dotX = dotsStart
            while (dotX < dotsEnd) {
                canvas.drawText(".", dotX, y, dotLeaderPaint)
                dotX += 6f
            }

            // Page number
            canvas.drawText(pageNumStr, PAGE_WIDTH - MARGIN_RIGHT, y, tocPageNumPaint)

            y += 26f
        }

        doc.finishPage(page)
    }

    private fun renderMonthDividerPage(doc: PdfDocument, pageNum: Int, month: Int) {
        val page = createPage(doc, pageNum)
        val canvas = page.canvas

        // Accent color background
        val bgPaint = Paint().apply { color = accentColor }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), bgPaint)

        // Month name centered
        val monthName = java.time.Month.of(month).getDisplayName(TextStyle.FULL, Locale.US)
        val centerX = PAGE_WIDTH / 2f
        val centerY = PAGE_HEIGHT / 2f
        canvas.drawText(monthName, centerX, centerY, dividerMonthPaint)

        doc.finishPage(page)
    }

    /**
     * Renders one entry across potentially multiple pages.
     * Returns the last page number used.
     */
    private fun renderEntryPages(doc: PdfDocument, startPageNum: Int, entry: EntryEntity): Int {
        var currentPageNum = startPageNum + 1
        val availableHeight = PAGE_HEIGHT - MARGIN_TOP - MARGIN_BOTTOM - 20f

        // ── First page ──
        var page = createPage(doc, currentPageNum)
        var canvas = page.canvas
        drawPageBackground(canvas)

        var y = MARGIN_TOP

        // Date header
        val date = Instant.ofEpochMilli(entry.createdAt)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        canvas.drawText(date.format(dateFormatter), MARGIN_LEFT, y + 12f, entryDatePaint)
        y += 18f

        // Mood dot
        val moodObj = Mood.fromString(entry.mood)
        if (moodObj != null) {
            val moodDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = moodObj.color.toArgb()
            }
            val dateWidth = entryDatePaint.measureText(date.format(dateFormatter))
            canvas.drawCircle(MARGIN_LEFT + dateWidth + 10f, y - 6f, 4f, moodDotPaint)
        }

        // Title
        if (entry.title.isNotBlank()) {
            y += 4f
            val titlePaint = entryTitleTextPaint()
            val titleLayout = createStaticLayout(entry.title, titlePaint, CONTENT_WIDTH.toInt())
            canvas.save()
            canvas.translate(MARGIN_LEFT, y)
            titleLayout.draw(canvas)
            canvas.restore()
            y += titleLayout.height + 8f
        }

        y += 8f // spacing before body

        // Body text with multi-page overflow
        val bodyLayout = createStaticLayout(entry.content, bodyTextPaint, CONTENT_WIDTH.toInt())
        val totalBodyHeight = bodyLayout.height
        var bodyOffset = 0

        // Draw as much body as fits on first page
        val firstPageRemaining = (MARGIN_TOP + availableHeight - y).toInt()
        if (totalBodyHeight <= firstPageRemaining) {
            // Fits on one page
            canvas.save()
            canvas.translate(MARGIN_LEFT, y)
            bodyLayout.draw(canvas)
            canvas.restore()
        } else {
            // Render partial on first page
            val lastLine = bodyLayout.getLineForVertical(firstPageRemaining)
            if (lastLine > 0) {
                canvas.save()
                canvas.translate(MARGIN_LEFT, y)
                canvas.clipRect(0f, 0f, CONTENT_WIDTH, firstPageRemaining.toFloat())
                bodyLayout.draw(canvas)
                canvas.restore()
                bodyOffset = bodyLayout.getLineEnd(lastLine - 1)
            }

            // Overflow pages
            while (bodyOffset < entry.content.length) {
                drawPageNumber(canvas, currentPageNum)
                doc.finishPage(page)

                currentPageNum++
                page = createPage(doc, currentPageNum)
                canvas = page.canvas
                drawPageBackground(canvas)

                val remainingText = entry.content.substring(bodyOffset)
                val overflowLayout = createStaticLayout(remainingText, bodyTextPaint, CONTENT_WIDTH.toInt())
                val pageAvailable = (availableHeight - 10f).toInt()

                if (overflowLayout.height <= pageAvailable) {
                    // Last page for this entry
                    canvas.save()
                    canvas.translate(MARGIN_LEFT, MARGIN_TOP)
                    overflowLayout.draw(canvas)
                    canvas.restore()
                    bodyOffset = entry.content.length
                } else {
                    val lastOverflowLine = overflowLayout.getLineForVertical(pageAvailable)
                    if (lastOverflowLine > 0) {
                        canvas.save()
                        canvas.translate(MARGIN_LEFT, MARGIN_TOP)
                        canvas.clipRect(0f, 0f, CONTENT_WIDTH, pageAvailable.toFloat())
                        overflowLayout.draw(canvas)
                        canvas.restore()
                        bodyOffset += overflowLayout.getLineEnd(lastOverflowLine - 1)
                    } else {
                        // Can't fit even one line — break out to avoid infinite loop
                        bodyOffset = entry.content.length
                    }
                }
            }
        }

        drawPageNumber(canvas, currentPageNum)
        doc.finishPage(page)
        return currentPageNum
    }

    private fun renderStatsPage(doc: PdfDocument, pageNum: Int) {
        val page = createPage(doc, pageNum)
        val canvas = page.canvas

        val bgPaint = Paint().apply { color = 0xFFFAF9F5.toInt() }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), bgPaint)

        val centerX = PAGE_WIDTH / 2f
        var y = MARGIN_TOP + 40f

        // "Your Year" heading
        canvas.drawText("Your Year", centerX, y, statsHeadingPaint)
        y += 20f

        // Decorative line
        canvas.drawLine(centerX - 30f, y, centerX + 30f, y, Paint(linePaint).apply { color = 0xFFA0A09E.toInt() })
        y += 50f

        // Total entries
        canvas.drawText(config.totalEntries.toString(), centerX, y, statsNumberPaint)
        y += 18f
        canvas.drawText("entries", centerX, y, statsLabelPaint)
        y += 50f

        // Total words
        canvas.drawText(formatNumber(config.totalWords), centerX, y, statsNumberPaint)
        y += 18f
        canvas.drawText("words written", centerX, y, statsLabelPaint)
        y += 50f

        // Longest streak
        canvas.drawText(config.longestStreak.toString(), centerX, y, statsNumberPaint)
        y += 18f
        canvas.drawText("day longest streak", centerX, y, statsLabelPaint)
        y += 50f

        // Most common mood
        val moodDisplay = config.mostCommonMood ?: "\u2014"
        val moodLabelPaint = Paint(statsNumberPaint).apply { textSize = 32f }
        canvas.drawText(moodDisplay, centerX, y, moodLabelPaint)
        y += 18f
        canvas.drawText("most common mood", centerX, y, statsLabelPaint)

        drawPageNumber(canvas, pageNum)
        doc.finishPage(page)
    }

    private fun renderColophonPage(doc: PdfDocument, pageNum: Int) {
        val page = createPage(doc, pageNum)
        val canvas = page.canvas

        val bgPaint = Paint().apply { color = 0xFFFAF9F5.toInt() }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), bgPaint)

        val centerX = PAGE_WIDTH / 2f
        val centerY = PAGE_HEIGHT / 2f

        canvas.drawText("Written in Proactive Diary", centerX, centerY, colophonPaint)

        doc.finishPage(page)
    }

    // ─── Helpers ───────────────────────────────────────────

    private fun createPage(doc: PdfDocument, pageNum: Int): PdfDocument.Page {
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create()
        return doc.startPage(pageInfo)
    }

    private fun drawPageBackground(canvas: Canvas) {
        val bgPaint = Paint().apply { color = 0xFFFAF9F5.toInt() }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), bgPaint)
    }

    private fun drawPageNumber(canvas: Canvas, pageNum: Int) {
        canvas.drawText(
            pageNum.toString(),
            PAGE_WIDTH / 2f,
            PAGE_HEIGHT - MARGIN_BOTTOM / 2f,
            pageNumberPaint
        )
    }

    private fun entryTitleTextPaint(): TextPaint {
        return TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = cormorantRegular
            textSize = 20f
            color = 0xFF313131.toInt()
        }
    }

    @Suppress("DEPRECATION")
    private fun createStaticLayout(text: String, paint: TextPaint, width: Int): StaticLayout {
        return StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setLineSpacing(0f, 1.3f)
            .build()
    }

    private fun formatNumber(n: Int): String {
        return String.format(Locale.US, "%,d", n)
    }
}
