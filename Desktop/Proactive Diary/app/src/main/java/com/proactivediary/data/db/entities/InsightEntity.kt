package com.proactivediary.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "insights")
data class InsightEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "week_start") val weekStart: Long,
    val summary: String,
    val themes: String = "[]",
    @ColumnInfo(name = "mood_trend") val moodTrend: String = "stable",
    @ColumnInfo(name = "prompt_suggestions") val promptSuggestions: String = "[]",
    @ColumnInfo(name = "generated_at") val generatedAt: Long
)
