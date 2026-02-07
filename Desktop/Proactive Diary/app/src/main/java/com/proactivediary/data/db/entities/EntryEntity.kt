package com.proactivediary.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "entries",
    indices = [Index("created_at")]
)
data class EntryEntity(
    @PrimaryKey val id: String,
    val title: String = "",
    val content: String,
    val mood: String? = null,
    val tags: String = "[]",
    @ColumnInfo(name = "word_count") val wordCount: Int = 0,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long
)
