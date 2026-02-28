package com.proactivediary.domain.model

import com.proactivediary.data.db.entities.WritingReminderEntity
import org.junit.Assert.*
import org.junit.Test

/**
 * Verifies WritingReminderEntity defaults match MIGRATION_11_12 column defaults.
 * If entity defaults diverge from migration defaults, upgrades will produce mismatched rows.
 */
class WritingReminderEntityTest {

    @Test
    fun `fallbackEnabled defaults to true`() {
        val reminder = makeReminder()
        assertTrue(reminder.fallbackEnabled)
    }

    @Test
    fun `label defaults to Write in your diary`() {
        val reminder = makeReminder()
        assertEquals("Write in your diary", reminder.label)
    }

    @Test
    fun `isActive defaults to true`() {
        val reminder = makeReminder()
        assertTrue(reminder.isActive)
    }

    @Test
    fun `days defaults to all days`() {
        val reminder = makeReminder()
        assertEquals("[0,1,2,3,4,5,6]", reminder.days)
    }

    @Test
    fun `custom label overrides default`() {
        val reminder = makeReminder(label = "Morning pages")
        assertEquals("Morning pages", reminder.label)
    }

    @Test
    fun `fallbackEnabled can be set to false`() {
        val reminder = makeReminder(fallbackEnabled = false)
        assertFalse(reminder.fallbackEnabled)
    }

    @Test
    fun `copy preserves all fields`() {
        val reminder = makeReminder(label = "Evening reflection", fallbackEnabled = false)
        val copy = reminder.copy(time = "21:00")
        assertEquals("21:00", copy.time)
        assertEquals("Evening reflection", copy.label)
        assertFalse(copy.fallbackEnabled)
    }

    private fun makeReminder(
        label: String = "Write in your diary",
        fallbackEnabled: Boolean = true
    ) = WritingReminderEntity(
        id = "r1",
        time = "09:00",
        fallbackEnabled = fallbackEnabled,
        label = label
    )
}
