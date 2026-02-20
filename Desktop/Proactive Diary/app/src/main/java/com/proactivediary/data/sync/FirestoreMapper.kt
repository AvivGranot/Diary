package com.proactivediary.data.sync

import com.google.firebase.firestore.DocumentSnapshot
import com.proactivediary.data.db.entities.EntryEntity
import com.proactivediary.data.db.entities.GoalCheckInEntity
import com.proactivediary.data.db.entities.GoalEntity
import com.proactivediary.data.db.entities.WritingReminderEntity

/**
 * Maps between Room entities and Firestore document maps.
 * Handles the conversion in both directions for cloud sync.
 */
object FirestoreMapper {

    // ── Entries ──

    fun entryToMap(entry: EntryEntity): Map<String, Any?> = mapOf(
        "title" to entry.title,
        "content" to entry.content,
        // "mood" removed — mood feature deprecated
        "tags" to entry.tags,
        "taggedContacts" to entry.taggedContacts,
        "images" to entry.images,
        "latitude" to entry.latitude,
        "longitude" to entry.longitude,
        "locationName" to entry.locationName,
        "weatherTemp" to entry.weatherTemp,
        "weatherCondition" to entry.weatherCondition,
        "weatherIcon" to entry.weatherIcon,
        "templateId" to entry.templateId,
        "audioPath" to entry.audioPath,
        "contentHtml" to entry.contentHtml,
        "contentPlain" to entry.contentPlain,
        "wordCount" to entry.wordCount,
        "createdAt" to entry.createdAt,
        "updatedAt" to entry.updatedAt,
        "_deleted" to false
    )

    fun documentToEntry(doc: DocumentSnapshot): EntryEntity? {
        return try {
            EntryEntity(
                id = doc.id,
                title = doc.getString("title") ?: "",
                content = doc.getString("content") ?: "",
                mood = null, // mood feature deprecated — stop reading from Firestore
                tags = doc.getString("tags") ?: "[]",
                taggedContacts = doc.getString("taggedContacts") ?: "[]",
                images = doc.getString("images") ?: "[]",
                latitude = doc.getDouble("latitude"),
                longitude = doc.getDouble("longitude"),
                locationName = doc.getString("locationName"),
                weatherTemp = doc.getDouble("weatherTemp"),
                weatherCondition = doc.getString("weatherCondition"),
                weatherIcon = doc.getString("weatherIcon"),
                templateId = doc.getString("templateId"),
                audioPath = doc.getString("audioPath"),
                contentHtml = doc.getString("contentHtml"),
                contentPlain = doc.getString("contentPlain"),
                wordCount = doc.getLong("wordCount")?.toInt() ?: 0,
                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis(),
                syncStatus = SyncStatus.SYNCED
            )
        } catch (e: Exception) {
            null
        }
    }

    // ── Goals ──

    fun goalToMap(goal: GoalEntity): Map<String, Any?> = mapOf(
        "title" to goal.title,
        "description" to goal.description,
        "frequency" to goal.frequency,
        "reminderTime" to goal.reminderTime,
        "reminderDays" to goal.reminderDays,
        "isActive" to goal.isActive,
        "createdAt" to goal.createdAt,
        "updatedAt" to goal.updatedAt,
        "_deleted" to false
    )

    fun documentToGoal(doc: DocumentSnapshot): GoalEntity? {
        return try {
            GoalEntity(
                id = doc.id,
                title = doc.getString("title") ?: "",
                description = doc.getString("description"),
                frequency = doc.getString("frequency") ?: "daily",
                reminderTime = doc.getString("reminderTime") ?: "09:00",
                reminderDays = doc.getString("reminderDays") ?: "[0,1,2,3,4,5,6]",
                isActive = doc.getBoolean("isActive") ?: true,
                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis(),
                syncStatus = SyncStatus.SYNCED
            )
        } catch (e: Exception) {
            null
        }
    }

    // ── Goal Check-Ins ──

    fun checkInToMap(checkIn: GoalCheckInEntity): Map<String, Any?> = mapOf(
        "goalId" to checkIn.goalId,
        "date" to checkIn.date,
        "completed" to checkIn.completed,
        "note" to checkIn.note,
        "createdAt" to checkIn.createdAt,
        "_deleted" to false
    )

    fun documentToCheckIn(doc: DocumentSnapshot): GoalCheckInEntity? {
        return try {
            GoalCheckInEntity(
                id = doc.id,
                goalId = doc.getString("goalId") ?: return null,
                date = doc.getString("date") ?: return null,
                completed = doc.getBoolean("completed") ?: false,
                note = doc.getString("note"),
                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                syncStatus = SyncStatus.SYNCED
            )
        } catch (e: Exception) {
            null
        }
    }

    // ── Writing Reminders ──

    fun reminderToMap(reminder: WritingReminderEntity): Map<String, Any?> = mapOf(
        "time" to reminder.time,
        "days" to reminder.days,
        "isActive" to reminder.isActive,
        "fallbackEnabled" to reminder.fallbackEnabled,
        "label" to reminder.label,
        "updatedAt" to reminder.updatedAt,
        "_deleted" to false
    )

    fun documentToReminder(doc: DocumentSnapshot): WritingReminderEntity? {
        return try {
            WritingReminderEntity(
                id = doc.id,
                time = doc.getString("time") ?: "09:00",
                days = doc.getString("days") ?: "[0,1,2,3,4,5,6]",
                isActive = doc.getBoolean("isActive") ?: true,
                fallbackEnabled = doc.getBoolean("fallbackEnabled") ?: true,
                label = doc.getString("label") ?: "Write in your diary",
                updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis(),
                syncStatus = SyncStatus.SYNCED
            )
        } catch (e: Exception) {
            null
        }
    }

    // ── Preferences ──

    fun preferenceToMap(value: String): Map<String, Any> = mapOf(
        "value" to value,
        "updatedAt" to System.currentTimeMillis()
    )
}
