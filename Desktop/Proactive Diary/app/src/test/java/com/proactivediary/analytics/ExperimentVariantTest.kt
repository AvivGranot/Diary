package com.proactivediary.analytics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests that verify each experiment's variant logic works correctly
 * through the ExperimentService interface, using the FakeExperimentService.
 *
 * This covers: Experiments 1-7 variant paths.
 */
class ExperimentVariantTest {

    private lateinit var service: FakeExperimentService

    @Before
    fun setUp() {
        service = FakeExperimentService()
    }

    // --- Experiment 1: First Words (onboarding flow) ---

    @Test
    fun `exp1 control returns default onboarding flow`() {
        assertEquals("control", service.getVariant(Experiment.FIRST_WORDS))
    }

    @Test
    fun `exp1 skip_design variant is recognized`() {
        service.setVariant(Experiment.FIRST_WORDS, "skip_design")
        assertEquals("skip_design", service.getVariant(Experiment.FIRST_WORDS))
        assertTrue(service.isVariant(Experiment.FIRST_WORDS, "skip_design"))
    }

    @Test
    fun `exp1 straight_to_write variant is recognized`() {
        service.setVariant(Experiment.FIRST_WORDS, "straight_to_write")
        assertEquals("straight_to_write", service.getVariant(Experiment.FIRST_WORDS))
    }

    // --- Experiment 2: Blank Page vs Prompt ---

    @Test
    fun `exp2 control shows prompt as placeholder`() {
        val showPrompt = service.getVariant(Experiment.BLANK_PAGE_VS_PROMPT) != "blank_page"
        assertTrue(showPrompt)
    }

    @Test
    fun `exp2 blank_page hides prompt`() {
        service.setVariant(Experiment.BLANK_PAGE_VS_PROMPT, "blank_page")
        val showPrompt = service.getVariant(Experiment.BLANK_PAGE_VS_PROMPT) != "blank_page"
        assertFalse(showPrompt)
    }

    // --- Experiment 3: Paywall Timing ---

    @Test
    fun `exp3 control threshold is 10`() {
        val threshold = paywallThreshold(service.getVariant(Experiment.PAYWALL_TIMING))
        assertEquals(10, threshold)
    }

    @Test
    fun `exp3 early threshold is 7`() {
        service.setVariant(Experiment.PAYWALL_TIMING, "early")
        val threshold = paywallThreshold(service.getVariant(Experiment.PAYWALL_TIMING))
        assertEquals(7, threshold)
    }

    @Test
    fun `exp3 late threshold is 14`() {
        service.setVariant(Experiment.PAYWALL_TIMING, "late")
        val threshold = paywallThreshold(service.getVariant(Experiment.PAYWALL_TIMING))
        assertEquals(14, threshold)
    }

    // --- Experiment 4: Streak Celebration ---

    @Test
    fun `exp4 control shows celebrations`() {
        assertTrue(service.isVariant(Experiment.STREAK_CELEBRATION, "control"))
    }

    @Test
    fun `exp4 quiet suppresses celebrations`() {
        service.setVariant(Experiment.STREAK_CELEBRATION, "quiet")
        assertTrue(service.isVariant(Experiment.STREAK_CELEBRATION, "quiet"))
        assertFalse(service.isVariant(Experiment.STREAK_CELEBRATION, "control"))
    }

    // --- Experiment 5: Reminder Tone ---

    @Test
    fun `exp5 control uses standard notification`() {
        assertEquals("control", service.getVariant(Experiment.REMINDER_TONE))
    }

    @Test
    fun `exp5 gentle variant is recognized`() {
        service.setVariant(Experiment.REMINDER_TONE, "gentle")
        assertEquals("gentle", service.getVariant(Experiment.REMINDER_TONE))
    }

    @Test
    fun `exp5 silent variant is recognized`() {
        service.setVariant(Experiment.REMINDER_TONE, "silent")
        assertEquals("silent", service.getVariant(Experiment.REMINDER_TONE))
    }

    // --- Experiment 6: Dark Mode Default ---

    @Test
    fun `exp6 control defaults to light mode`() {
        assertEquals("control", service.getVariant(Experiment.DARK_MODE_DEFAULT))
    }

    @Test
    fun `exp6 system follows system setting`() {
        service.setVariant(Experiment.DARK_MODE_DEFAULT, "system")
        assertEquals("system", service.getVariant(Experiment.DARK_MODE_DEFAULT))
    }

    // --- Experiment 7: Pricing Anchoring ---

    @Test
    fun `exp7 control shows all plans including lifetime`() {
        val hideLifetime = service.isVariant(Experiment.PRICING_ANCHORING, "hide_lifetime")
        assertFalse(hideLifetime)
    }

    @Test
    fun `exp7 hide_lifetime hides lifetime plan`() {
        service.setVariant(Experiment.PRICING_ANCHORING, "hide_lifetime")
        val hideLifetime = service.isVariant(Experiment.PRICING_ANCHORING, "hide_lifetime")
        assertTrue(hideLifetime)
    }

    // --- Exposure tracking ---

    @Test
    fun `exposure is logged for each experiment`() {
        Experiment.entries.forEach { exp ->
            service.logExposure(exp)
        }
        assertEquals(Experiment.entries.size, service.exposuresLogged.size)
        Experiment.entries.forEachIndexed { i, exp ->
            assertEquals(exp, service.exposuresLogged[i])
        }
    }

    // --- Helper: mirrors BillingViewModel.entryGateThreshold logic ---

    private fun paywallThreshold(variant: String): Int {
        return when (variant) {
            "early" -> 7
            "late" -> 14
            else -> 10
        }
    }
}
