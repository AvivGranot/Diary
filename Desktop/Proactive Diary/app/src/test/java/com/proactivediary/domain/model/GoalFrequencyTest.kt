package com.proactivediary.domain.model

import org.junit.Assert.*
import org.junit.Test

class GoalFrequencyTest {

    @Test
    fun `fromString returns correct frequency for each value`() {
        assertEquals(GoalFrequency.DAILY, GoalFrequency.fromString("daily"))
        assertEquals(GoalFrequency.WEEKLY, GoalFrequency.fromString("weekly"))
        assertEquals(GoalFrequency.MONTHLY, GoalFrequency.fromString("monthly"))
    }

    @Test
    fun `fromString is case insensitive`() {
        assertEquals(GoalFrequency.DAILY, GoalFrequency.fromString("DAILY"))
        assertEquals(GoalFrequency.WEEKLY, GoalFrequency.fromString("Weekly"))
        assertEquals(GoalFrequency.MONTHLY, GoalFrequency.fromString("MONTHLY"))
    }

    @Test
    fun `fromString defaults to DAILY for null`() {
        assertEquals(GoalFrequency.DAILY, GoalFrequency.fromString(null))
    }

    @Test
    fun `fromString defaults to DAILY for unknown value`() {
        assertEquals(GoalFrequency.DAILY, GoalFrequency.fromString("yearly"))
        assertEquals(GoalFrequency.DAILY, GoalFrequency.fromString(""))
    }

    @Test
    fun `each frequency has non-empty label`() {
        GoalFrequency.entries.forEach { freq ->
            assertTrue("Frequency ${freq.name} should have non-empty label", freq.label.isNotEmpty())
        }
    }
}
