package com.proactivediary.notifications

import androidx.core.app.NotificationCompat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for [buildReminderContent] — the pure function extracted
 * from AlarmReceiver that maps experiment variant to notification content.
 */
class ReminderNotificationContentTest {

    @Test
    fun `control variant returns Time to write title`() {
        val content = buildReminderContent("control", "Evening writing")
        assertEquals("Time to write", content.title)
        assertEquals("Evening writing", content.body)
        assertFalse(content.isSilent)
        assertEquals(NotificationCompat.PRIORITY_DEFAULT, content.priority)
    }

    @Test
    fun `gentle variant returns encouraging title`() {
        val content = buildReminderContent("gentle", "Morning pages")
        assertEquals("Your diary is here when you're ready", content.title)
        assertEquals("Morning pages", content.body)
        assertFalse(content.isSilent)
        assertEquals(NotificationCompat.PRIORITY_DEFAULT, content.priority)
    }

    @Test
    fun `silent variant returns null title and body, is silent`() {
        val content = buildReminderContent("silent", "Daily reminder")
        assertNull(content.title)
        assertNull(content.body)
        assertTrue(content.isSilent)
        assertEquals(NotificationCompat.PRIORITY_LOW, content.priority)
    }

    @Test
    fun `unknown variant falls back to control behavior`() {
        val content = buildReminderContent("unknown_variant", "Some label")
        assertEquals("Time to write", content.title)
        assertEquals("Some label", content.body)
        assertFalse(content.isSilent)
        assertEquals(NotificationCompat.PRIORITY_DEFAULT, content.priority)
    }

    @Test
    fun `empty variant string falls back to control behavior`() {
        val content = buildReminderContent("", "My reminder")
        assertEquals("Time to write", content.title)
        assertEquals("My reminder", content.body)
    }
}
