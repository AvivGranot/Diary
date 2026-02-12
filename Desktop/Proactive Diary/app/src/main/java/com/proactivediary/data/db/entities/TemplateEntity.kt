package com.proactivediary.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "templates")
data class TemplateEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val prompts: String = "[]",  // JSON array of prompt strings
    val category: String,        // "reflection", "gratitude", "growth", "wellness", "creative"
    @ColumnInfo(name = "is_built_in") val isBuiltIn: Boolean = true,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)
