package com.proactivediary.domain.model

import com.proactivediary.data.db.entities.EntryEntity
import com.proactivediary.data.db.entities.GoalEntity
import com.proactivediary.data.db.entities.GoalCheckInEntity
import org.junit.Assert.*
import org.junit.Test

class EntryEntityTest {

    @Test
    fun `EntryEntity defaults are correct`() {
        val entry = EntryEntity(
            id = "e1",
            content = "Hello world",
            createdAt = 1000L,
            updatedAt = 2000L
        )
        assertEquals("e1", entry.id)
        assertEquals("", entry.title)
        assertEquals("Hello world", entry.content)
        assertEquals("[]", entry.tags)
        assertEquals("[]", entry.taggedContacts)
        assertEquals(0, entry.wordCount)
    }

    @Test
    fun `EntryEntity copy updates correctly`() {
        val entry = EntryEntity(
            id = "e1", content = "Hello", createdAt = 1000L, updatedAt = 2000L
        )
        val updated = entry.copy(title = "New Title", wordCount = 5)
        assertEquals("New Title", updated.title)
        assertEquals(5, updated.wordCount)
        assertEquals("Hello", updated.content) // unchanged
    }

    @Test
    fun `GoalEntity defaults are correct`() {
        val goal = GoalEntity(
            id = "g1", title = "Write daily",
            reminderTime = "09:00", createdAt = 1000L, updatedAt = 2000L
        )
        assertEquals("daily", goal.frequency)
        assertTrue(goal.isActive)
        assertNull(goal.description)
        assertEquals("[0,1,2,3,4,5,6]", goal.reminderDays)
    }

    @Test
    fun `GoalCheckInEntity defaults are correct`() {
        val checkIn = GoalCheckInEntity(
            id = "ci1", goalId = "g1", date = "2024-01-15", createdAt = 1000L
        )
        assertFalse(checkIn.completed)
        assertNull(checkIn.note)
    }

    @Test
    fun `EntryEntity with all fields set`() {
        val entry = EntryEntity(
            id = "e2",
            title = "My Day",
            content = "It was a great day",
            tags = "[\"personal\",\"gratitude\"]",
            taggedContacts = "[{\"name\":\"John\"}]",
            wordCount = 5,
            createdAt = 1000L,
            updatedAt = 2000L
        )
        assertEquals("My Day", entry.title)
        assertTrue(entry.tags.contains("personal"))
    }
}
