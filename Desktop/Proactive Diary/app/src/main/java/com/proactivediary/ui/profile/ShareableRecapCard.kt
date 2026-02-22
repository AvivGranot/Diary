package com.proactivediary.ui.profile

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/**
 * Renders a shareable Instagram Stories–style card (1080×1920) from profile recap stats.
 * Pure Android Canvas drawing — no Compose dependency — so it works off-screen.
 */
object ShareableRecapCard {

    private const val W = 1080
    private const val H = 1920

    data class RecapData(
        val displayName: String,
        val entriesThisYear: Int,
        val totalLikesReceived: Int,
        val totalWords: Int,
        val bestWritingDay: String,
        val bestWritingDayPercent: Int
    )

    /**
     * Draw the recap card to a Bitmap, save to cache, and launch a share intent.
     */
    fun shareRecap(context: Context, data: RecapData) {
        val bitmap = renderCard(context, data)
        val file = saveBitmapToCache(context, bitmap)
        launchShareIntent(context, file)
    }

    // ── Card renderer ────────────────────────────────────────────────────

    private fun renderCard(context: Context, data: RecapData): Bitmap {
        val bmp = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)

        // Background gradient (dark navy → blue)
        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, H.toFloat(),
                intArrayOf(0xFF0F172A.toInt(), 0xFF1E3A5F.toInt(), 0xFF3B82F6.toInt()),
                floatArrayOf(0f, 0.6f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        c.drawRect(0f, 0f, W.toFloat(), H.toFloat(), bgPaint)

        // Serif typeface (fallback to default serif)
        val serifTf = try {
            Typeface.create("serif", Typeface.NORMAL)
        } catch (_: Exception) {
            Typeface.SERIF
        }
        val sansTf = Typeface.create("sans-serif-medium", Typeface.NORMAL)

        // ── Top header ──
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xB3FFFFFF.toInt() // 70% white
            textSize = 40f
            typeface = sansTf
        }
        c.drawText("My Year in Writing", 80f, 180f, headerPaint)

        // App name small
        val appNamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x66FFFFFF.toInt() // 40% white
            textSize = 32f
            typeface = sansTf
        }
        c.drawText("Proactive Diary", 80f, 230f, appNamePaint)

        // Display name
        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            textSize = 64f
            typeface = Typeface.create(serifTf, Typeface.NORMAL)
        }
        c.drawText(data.displayName, 80f, 360f, namePaint)

        // ── Divider line ──
        val divPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x33FFFFFF.toInt()
            strokeWidth = 2f
        }
        c.drawLine(80f, 420f, W - 80f, 420f, divPaint)

        // ── 2×2 Gradient cards ──
        val cardW = (W - 80 * 2 - 24) / 2f  // gap between cards = 24
        val cardH = cardW  // square cards
        val startY = 480f
        val gap = 24f

        // Card data
        val cards = listOf(
            CardInfo(
                bigText = "${data.entriesThisYear}",
                label = "Entries this year",
                subtitle = when {
                    data.entriesThisYear > 20 -> "Top 5% of writers"
                    data.entriesThisYear > 5 -> "Top 20% of writers"
                    else -> "Keep going!"
                },
                gradientColors = intArrayOf(0xFF065F46.toInt(), 0xFF059669.toInt(), 0xFF34D399.toInt())
            ),
            CardInfo(
                bigText = "${data.totalLikesReceived}",
                label = "Total likes received",
                subtitle = if (data.totalLikesReceived > 0) "People love your words" else "Share your first quote",
                gradientColors = intArrayOf(0xFF9F1239.toInt(), 0xFFE11D48.toInt(), 0xFFFB7185.toInt())
            ),
            CardInfo(
                bigText = formatWords(data.totalWords),
                label = "Words written",
                subtitle = when {
                    data.totalWords >= 50_000 -> "That's a short novel"
                    data.totalWords >= 10_000 -> "That's a novella"
                    data.totalWords >= 1_000 -> "That's a short story"
                    else -> "Every word counts"
                },
                gradientColors = intArrayOf(0xFF78350F.toInt(), 0xFFD97706.toInt(), 0xFFFBBF24.toInt())
            ),
            CardInfo(
                bigText = data.bestWritingDay.take(3).ifBlank { "--" },
                label = "Best writing day",
                subtitle = if (data.bestWritingDay.isNotBlank())
                    "${data.bestWritingDayPercent}% of entries on ${data.bestWritingDay}s"
                else "Write more to discover",
                gradientColors = intArrayOf(0xFF1E3A5F.toInt(), 0xFF3B82F6.toInt(), 0xFF93C5FD.toInt())
            )
        )

        for (i in cards.indices) {
            val col = i % 2
            val row = i / 2
            val x = 80f + col * (cardW + gap)
            val y = startY + row * (cardH + gap)
            drawCard(c, x, y, cardW, cardH, cards[i], serifTf, sansTf)
        }

        // ── Bottom watermark ──
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x99FFFFFF.toInt()
            textSize = 36f
            typeface = sansTf
            textAlign = Paint.Align.CENTER
        }
        c.drawText("proactivediary.app", W / 2f, H - 120f, footerPaint)

        val taglinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x66FFFFFF.toInt()
            textSize = 28f
            typeface = serifTf
            textAlign = Paint.Align.CENTER
        }
        c.drawText("Your thoughts, beautifully captured", W / 2f, H - 70f, taglinePaint)

        return bmp
    }

    private data class CardInfo(
        val bigText: String,
        val label: String,
        val subtitle: String,
        val gradientColors: IntArray
    )

    private fun drawCard(
        c: Canvas, x: Float, y: Float, w: Float, h: Float,
        info: CardInfo, serifTf: Typeface, sansTf: Typeface
    ) {
        val rect = RectF(x, y, x + w, y + h)
        val radius = 36f

        // Gradient background
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                x, y, x + w, y + h,
                info.gradientColors,
                null,
                Shader.TileMode.CLAMP
            )
        }
        c.drawRoundRect(rect, radius, radius, cardPaint)

        // Big text (top)
        val bigPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            textSize = 80f
            typeface = serifTf
        }
        c.drawText(info.bigText, x + 32f, y + 90f, bigPaint)

        // Label (bottom)
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xE6FFFFFF.toInt()
            textSize = 30f
            typeface = Typeface.create(sansTf, Typeface.BOLD)
        }
        c.drawText(info.label, x + 32f, y + h - 70f, labelPaint)

        // Subtitle
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xB3FFFFFF.toInt()
            textSize = 24f
            typeface = sansTf
        }
        c.drawText(info.subtitle, x + 32f, y + h - 32f, subPaint)
    }

    // ── Save & share ─────────────────────────────────────────────────────

    private fun saveBitmapToCache(context: Context, bitmap: Bitmap): File {
        val dir = File(context.cacheDir, "share_cards")
        dir.mkdirs()
        val file = File(dir, "my_year_recap.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        bitmap.recycle()
        return file
    }

    private fun launchShareIntent(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "My year in writing \u2728 #ProactiveDiary")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Prefer Instagram Stories if available, otherwise generic share sheet
        val chooser = Intent.createChooser(shareIntent, "Share your recap")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    private fun formatWords(words: Int): String = when {
        words >= 1_000_000 -> "${words / 1_000_000}.${(words % 1_000_000) / 100_000}M"
        words >= 1_000 -> "${words / 1_000}.${(words % 1_000) / 100}k"
        else -> "$words"
    }
}
