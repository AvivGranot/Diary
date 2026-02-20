package com.proactivediary.data.ai

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.proactivediary.data.db.dao.EntryDao
import com.proactivediary.data.db.dao.InsightDao
import com.proactivediary.data.db.entities.InsightEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.UUID

@HiltWorker
class AIInsightWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val entryDao: EntryDao,
    private val insightDao: InsightDao,
    private val aiInsightService: AIInsightService
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "ai_insight_weekly"
    }

    private val gson = Gson()

    override suspend fun doWork(): Result {
        return try {
            if (!aiInsightService.isEnabled()) {
                Log.d("AIInsightWorker", "AI insights disabled, skipping")
                return Result.success()
            }

            if (aiInsightService.getApiKey() == null) {
                Log.d("AIInsightWorker", "No API key configured, skipping")
                return Result.success()
            }

            val zone = ZoneId.systemDefault()
            val today = LocalDate.now()

            // Get the start of the current week (Monday)
            val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val weekStartMs = weekStart.atStartOfDay(zone).toInstant().toEpochMilli()

            // Check if we already have an insight for this week
            val existing = insightDao.getInsightForWeek(weekStartMs)
            if (existing != null) {
                Log.d("AIInsightWorker", "Insight already exists for this week")
                return Result.success()
            }

            // Get entries from the past week
            val weekEndMs = weekStart.plusDays(7).atStartOfDay(zone).toInstant().toEpochMilli()
            val entries = entryDao.getEntriesBetween(weekStartMs, weekEndMs)

            if (entries.isEmpty()) {
                Log.d("AIInsightWorker", "No entries this week, skipping")
                return Result.success()
            }

            val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d")
            val entriesForAI = entries.map { entity ->
                val date = Instant.ofEpochMilli(entity.createdAt)
                    .atZone(zone)
                    .toLocalDate()
                    .format(formatter)
                EntryForAI(
                    date = date,
                    title = entity.title,
                    content = entity.content
                )
            }

            val result = aiInsightService.generateInsight(entriesForAI)
            if (result != null) {
                @Suppress("DEPRECATION")
                val insight = InsightEntity(
                    id = UUID.randomUUID().toString(),
                    weekStart = weekStartMs,
                    summary = result.summary,
                    themes = gson.toJson(result.themes),
                    moodTrend = "stable",
                    promptSuggestions = gson.toJson(result.promptSuggestions),
                    generatedAt = System.currentTimeMillis()
                )
                insightDao.insert(insight)
                Log.d("AIInsightWorker", "Insight generated and saved")
            } else {
                Log.w("AIInsightWorker", "AI service returned null")
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("AIInsightWorker", "Failed to generate insight", e)
            Result.retry()
        }
    }
}
