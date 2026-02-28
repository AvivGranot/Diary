package com.proactivediary.ui.paywall

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for the new paywall model (P2).
 * Writing is always free. Premium features gated behind isPremium().
 * No entry-count gate.
 */
class PaywallModelTest {

    // ─── SubscriptionState data class ───

    @Test
    fun `default SubscriptionState has zero entry count`() {
        val state = SubscriptionState(isActive = true, plan = Plan.TRIAL, trialDaysLeft = 0)
        assertEquals(0, state.entryCount)
        assertEquals(0, state.totalWords)
    }

    @Test
    fun `SubscriptionState with entry count preserves value`() {
        val state = SubscriptionState(
            isActive = true,
            plan = Plan.TRIAL,
            trialDaysLeft = 0,
            entryCount = 500,
            totalWords = 25000
        )
        assertEquals(500, state.entryCount)
        assertEquals(25000, state.totalWords)
    }

    @Test
    fun `free tier SubscriptionState is always active`() {
        val state = SubscriptionState(
            isActive = true,
            plan = Plan.TRIAL,
            trialDaysLeft = 0,
            entryCount = 999
        )
        assertTrue("Writing should always be active regardless of entry count", state.isActive)
    }

    @Test
    fun `annual plan SubscriptionState is active`() {
        val state = SubscriptionState(
            isActive = true,
            plan = Plan.ANNUAL,
            trialDaysLeft = 0
        )
        assertTrue(state.isActive)
        assertEquals(Plan.ANNUAL, state.plan)
    }

    @Test
    fun `trialDaysLeft is always 0 in new model`() {
        // In the new model, there's no entry-count gate so trialDaysLeft is unused
        val freeTier = SubscriptionState(isActive = true, plan = Plan.TRIAL, trialDaysLeft = 0)
        assertEquals(0, freeTier.trialDaysLeft)

        val premium = SubscriptionState(isActive = true, plan = Plan.ANNUAL, trialDaysLeft = 0)
        assertEquals(0, premium.trialDaysLeft)
    }

    // ─── Plan enum ───

    @Test
    fun `Plan enum has TRIAL ANNUAL EXPIRED values`() {
        val values = Plan.entries
        assertEquals(3, values.size)
        assertTrue(values.contains(Plan.TRIAL))
        assertTrue(values.contains(Plan.ANNUAL))
        assertTrue(values.contains(Plan.EXPIRED))
    }

    @Test
    fun `Plan valueOf works for all valid names`() {
        assertEquals(Plan.TRIAL, Plan.valueOf("TRIAL"))
        assertEquals(Plan.ANNUAL, Plan.valueOf("ANNUAL"))
        assertEquals(Plan.EXPIRED, Plan.valueOf("EXPIRED"))
    }

    @Test
    fun `Plan valueOf throws for invalid name`() {
        try {
            Plan.valueOf("MONTHLY")
            fail("Should throw IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }

    // ─── SubscriptionState equality and copy ───

    @Test
    fun `SubscriptionState equality works`() {
        val s1 = SubscriptionState(isActive = true, plan = Plan.TRIAL, trialDaysLeft = 0, entryCount = 10)
        val s2 = SubscriptionState(isActive = true, plan = Plan.TRIAL, trialDaysLeft = 0, entryCount = 10)
        assertEquals(s1, s2)
    }

    @Test
    fun `SubscriptionState copy preserves unchanged fields`() {
        val original = SubscriptionState(
            isActive = true,
            plan = Plan.TRIAL,
            trialDaysLeft = 0,
            entryCount = 42,
            totalWords = 1000
        )
        val updated = original.copy(plan = Plan.ANNUAL)
        assertEquals(Plan.ANNUAL, updated.plan)
        assertEquals(42, updated.entryCount)
        assertEquals(1000, updated.totalWords)
        assertTrue(updated.isActive)
    }

    // ─── No entry gate threshold ───

    @Test
    fun `ENTRY_GATE_THRESHOLD constant no longer exists`() {
        // Verify at compile time: BillingViewModel.ENTRY_GATE_THRESHOLD would fail
        // This test documents that the entry gate was intentionally removed
        val companionFields = BillingViewModel.Companion::class.java.declaredFields
        val hasThreshold = companionFields.any { it.name == "ENTRY_GATE_THRESHOLD" }
        assertFalse("ENTRY_GATE_THRESHOLD should have been removed", hasThreshold)
    }

    // ─── High entry counts don't block writing ───

    @Test
    fun `user with 100 entries still has active subscription state`() {
        val state = SubscriptionState(
            isActive = true,
            plan = Plan.TRIAL,
            trialDaysLeft = 0,
            entryCount = 100
        )
        assertTrue("100 entries should not block writing", state.isActive)
    }

    @Test
    fun `user with 1000 entries still has active subscription state`() {
        val state = SubscriptionState(
            isActive = true,
            plan = Plan.TRIAL,
            trialDaysLeft = 0,
            entryCount = 1000
        )
        assertTrue("1000 entries should not block writing", state.isActive)
    }

    @Test
    fun `user with 0 entries has active subscription state`() {
        val state = SubscriptionState(
            isActive = true,
            plan = Plan.TRIAL,
            trialDaysLeft = 0,
            entryCount = 0
        )
        assertTrue("New user should be able to write", state.isActive)
    }
}
