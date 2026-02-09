package com.proactivediary.ui.write

import org.junit.Assert.*
import org.junit.Test

class StreakMilestoneTest {

    @Test
    fun `milestone days are recognized`() {
        val milestones = listOf(7, 14, 21, 30, 50, 100, 365)
        milestones.forEach { day ->
            assertTrue("Day $day should be a milestone", isMilestone(day))
        }
    }

    @Test
    fun `non-milestone days are not recognized`() {
        val nonMilestones = listOf(0, 1, 2, 5, 8, 10, 15, 20, 25, 31, 49, 99, 200, 364, 366)
        nonMilestones.forEach { day ->
            assertFalse("Day $day should not be a milestone", isMilestone(day))
        }
    }
}
