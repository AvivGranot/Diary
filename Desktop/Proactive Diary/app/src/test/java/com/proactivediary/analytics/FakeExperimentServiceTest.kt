package com.proactivediary.analytics

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Verify that [FakeExperimentService] behaves correctly so we can
 * trust it in all ViewModel tests.
 */
class FakeExperimentServiceTest {

    private lateinit var fake: FakeExperimentService

    @Before
    fun setUp() {
        fake = FakeExperimentService()
    }

    @Test
    fun `returns default variant when no override is set`() {
        Experiment.entries.forEach { exp ->
            assertEquals(exp.defaultVariant, fake.getVariant(exp))
        }
    }

    @Test
    fun `returns overridden variant after setVariant`() {
        fake.setVariant(Experiment.BLANK_PAGE_VS_PROMPT, "blank_page")
        assertEquals("blank_page", fake.getVariant(Experiment.BLANK_PAGE_VS_PROMPT))
    }

    @Test
    fun `isVariant checks against overrides`() {
        fake.setVariant(Experiment.PAYWALL_TIMING, "early")
        assertTrue(fake.isVariant(Experiment.PAYWALL_TIMING, "early"))
        assertFalse(fake.isVariant(Experiment.PAYWALL_TIMING, "control"))
        assertFalse(fake.isVariant(Experiment.PAYWALL_TIMING, "late"))
    }

    @Test
    fun `logExposure records to exposuresLogged`() {
        assertTrue(fake.exposuresLogged.isEmpty())
        fake.logExposure(Experiment.FIRST_WORDS)
        fake.logExposure(Experiment.STREAK_CELEBRATION)
        assertEquals(2, fake.exposuresLogged.size)
        assertEquals(Experiment.FIRST_WORDS, fake.exposuresLogged[0])
        assertEquals(Experiment.STREAK_CELEBRATION, fake.exposuresLogged[1])
    }

    @Test
    fun `reset clears all state`() {
        fake.setVariant(Experiment.DARK_MODE_DEFAULT, "system")
        fake.logExposure(Experiment.DARK_MODE_DEFAULT)

        fake.reset()

        assertEquals("control", fake.getVariant(Experiment.DARK_MODE_DEFAULT))
        assertTrue(fake.exposuresLogged.isEmpty())
        assertTrue(fake.variantOverrides.isEmpty())
    }

    @Test
    fun `initialize sets isInitialized to true`() = runTest {
        assertFalse(fake.isInitialized.value)
        fake.initialize()
        assertTrue(fake.isInitialized.value)
    }

    @Test
    fun `observeVariant returns flow with current value`() {
        fake.setVariant(Experiment.REMINDER_TONE, "gentle")
        val flow = fake.observeVariant(Experiment.REMINDER_TONE)
        assertEquals("gentle", flow.value)
    }
}
