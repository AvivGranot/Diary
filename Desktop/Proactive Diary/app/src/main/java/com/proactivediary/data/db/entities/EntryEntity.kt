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
    @ColumnInfo(name = "tagged_contacts") val taggedContacts: String = "[]",
    val images: String = "[]",
    val latitude: Double? = null,
    val longitude: Double? = null,
    @ColumnInfo(name = "location_name") val locationName: String? = null,
    @ColumnInfo(name = "weather_temp") val weatherTemp: Double? = null,
    @ColumnInfo(name = "weather_condition") val weatherCondition: String? = null,
    @ColumnInfo(name = "weather_icon") val weatherIcon: String? = null,
    @ColumnInfo(name = "template_id") val templateId: String? = null,
    @ColumnInfo(name = "audio_path") val audioPath: String? = null,
    @ColumnInfo(name = "content_html") val contentHtml: String? = null,
    @ColumnInfo(name = "content_plain") val contentPlain: String? = null,
    @ColumnInfo(name = "word_count") val wordCount: Int = 0,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long
)
