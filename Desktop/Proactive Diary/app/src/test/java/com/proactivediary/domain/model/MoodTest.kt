package com.proactivediary.domain.model

import org.junit.Assert.*
import org.junit.Test

class MoodTest {

    @Test
    fun `fromString returns correct mood for each key`() {
        assertEquals(Mood.GREAT, Mood.fromString("great"))
        assertEquals(Mood.GOOD, Mood.fromString("good"))
        assertEquals(Mood.NEUTRAL, Mood.fromString("neutral"))
        assertEquals(Mood.BAD, Mood.fromString("bad"))
        assertEquals(Mood.TERRIBLE, Mood.fromString("terrible"))
    }

    @Test
    fun `fromString is case insensitive`() {
        assertEquals(Mood.GREAT, Mood.fromString("GREAT"))
        assertEquals(Mood.GOOD, Mood.fromString("Good"))
        assertEquals(Mood.NEUTRAL, Mood.fromString("NEUTRAL"))
    }

    @Test
    fun `fromString returns null for null input`() {
        assertNull(Mood.fromString(null))
    }

    @Test
    fun `fromString returns null for unknown key`() {
        assertNull(Mood.fromString("amazing"))
        assertNull(Mood.fromString(""))
        assertNull(Mood.fromString("unknown"))
    }

    @Test
    fun `each mood has unique key`() {
        val keys = Mood.entries.map { it.key }
        assertEquals(keys.size, keys.toSet().size)
    }

    @Test
    fun `each mood has non-empty label`() {
        Mood.entries.forEach { mood ->
            assertTrue("Mood ${mood.name} should have a non-empty label", mood.label.isNotEmpty())
        }
    }
}
