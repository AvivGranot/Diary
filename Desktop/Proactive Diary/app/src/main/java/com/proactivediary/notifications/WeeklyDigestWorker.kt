package com.proactivediary.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.proactivediary.MainActivity
import com.proactivediary.R
import com.proactivediary.data.db.dao.EntryDao
import com.proactivediary.data.repository.StreakRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

@HiltWorker
class WeeklyDigestWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val entryDao: EntryDao,
    private val streakRepository: StreakRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "weekly_digest_notification"
        private const val NOTIFICATION_ID = 9002
    }

    override suspend fun doWork(): Result {
        val digest = buildDigest() ?: return Result.success()
        showNotification(digest)
        return Result.success()
    }

    private suspend fun buildDigest(): WeeklyDigest? {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now()
        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weekStartMs = weekStart.atStartOfDay(zone).toInstant().toEpochMilli()
        val weekEndMs = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

        val entries = entryDao.getEntriesBetween(weekStartMs, weekEndMs)
        if (entries.isEmpty()) return null

        val totalEntries = entries.size
        val totalWords = entries.sumOf { it.wordCount }
        val streak = streakRepository.calculateWritingStreak()

        // Most written day
        val dayGroups = entries.groupBy { entry ->
            Instant.ofEpochMilli(entry.createdAt)
                .atZone(zone)
                .toLocalDate()
                .dayOfWeek
        }
        val busiestDay = dayGroups.maxByOrNull { it.value.size }
            ?.key
            ?.getDisplayName(TextStyle.FULL, Locale.getDefault())

        // Top location
        val topLocation = entries
            .mapNotNull { it.locationName }
            .filter { it.isNotBlank() }
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key

        return WeeklyDigest(
            entryCount = totalEntries,
            wordCount = totalWords,
            streak = streak,
            dominantMood = null,
            busiestDay = busiestDay,
            topLocation = topLocation
        )
    }

    private fun showNotification(digest: WeeklyDigest) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            putExtra("destination", "journal")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            300_000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = buildTitle(digest)
        val body = buildBody(digest)

        val notification = NotificationCompat.Builder(
            applicationContext,
            NotificationChannels.CHANNEL_DIGEST
        )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // Notification permission not granted
        }
    }

    private fun buildTitle(digest: WeeklyDigest): String {
        return when {
            digest.streak >= 7 -> "Your week \u2014 ${digest.streak}-day streak \u2728"
            digest.entryCount >= 5 -> "Beautiful week of writing"
            digest.entryCount >= 3 -> "Your week in words"
            else -> "Your weekly reflection"
        }
    }

    private fun buildBody(digest: WeeklyDigest): String {
        val parts = mutableListOf<String>()

        // Core stat line
        val wordLabel = if (digest.wordCount == 1) "word" else "words"
        val entryLabel = if (digest.entryCount == 1) "entry" else "entries"
        parts.add("${digest.entryCount} $entryLabel \u00B7 ${formatNumber(digest.wordCount)} $wordLabel")

        // Streak
        if (digest.streak > 0) {
            parts.add("Writing streak: ${digest.streak} days")
        }

        // Location color
        digest.topLocation?.let {
            parts.add("Most written at $it")
        }

        // Busiest day
        digest.busiestDay?.let {
            parts.add("Most active on $it")
        }

        return parts.joinToString("\n")
    }

    private fun formatNumber(num: Int): String {
        return when {
            num >= 10_000 -> "${num / 1000}k"
            num >= 1_000 -> String.format("%.1fk", num / 1000.0)
            else -> num.toString()
        }
    }

    private data class WeeklyDigest(
        val entryCount: Int,
        val wordCount: Int,
        val streak: Int,
        val dominantMood: String?,
        val busiestDay: String?,
        val topLocation: String?
    )
}
