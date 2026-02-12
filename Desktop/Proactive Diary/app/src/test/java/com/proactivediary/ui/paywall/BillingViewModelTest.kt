package com.proactivediary.ui.paywall

import org.junit.Assert.*
import org.junit.Test

class BillingViewModelTest {

    // ─── SubscriptionState tests ───

    @Test
    fun `SubscriptionState defaults to active trial`() {
        val state = SubscriptionState(isActive = true, plan = Plan.TRIAL, trialDaysLeft = 10)
        assertTrue(state.isActive)
        assertEquals(Plan.TRIAL, state.plan)
        assertEquals(10, state.trialDaysLeft)
    }

    @Test
    fun `SubscriptionState expired state is not active`() {
        val state = SubscriptionState(isActive = false, plan = Plan.EXPIRED, trialDaysLeft = 0)
        assertFalse(state.isActive)
        assertEquals(Plan.EXPIRED, state.plan)
    }

    @Test
    fun `SubscriptionState paid plans are active`() {
        listOf(Plan.MONTHLY, Plan.ANNUAL).forEach { plan ->
            val state = SubscriptionState(isActive = true, plan = plan, trialDaysLeft = 0)
            assertTrue("Plan $plan should be active", state.isActive)
        }
    }

    @Test
    fun `SubscriptionState entry and word counts default to 0`() {
        val state = SubscriptionState(isActive = true, plan = Plan.TRIAL, trialDaysLeft = 5)
        assertEquals(0, state.entryCount)
        assertEquals(0, state.totalWords)
    }

    @Test
    fun `SubscriptionState with entry counts`() {
        val state = SubscriptionState(
            isActive = true, plan = Plan.TRIAL, trialDaysLeft = 3,
            entryCount = 7, totalWords = 1500
        )
        assertEquals(7, state.entryCount)
        assertEquals(1500, state.totalWords)
    }

    // ─── Plan enum tests ───

    @Test
    fun `Plan enum has all expected values`() {
        val plans = Plan.entries
        assertEquals(4, plans.size)
        assertTrue(plans.contains(Plan.TRIAL))
        assertTrue(plans.contains(Plan.MONTHLY))
        assertTrue(plans.contains(Plan.ANNUAL))
        assertTrue(plans.contains(Plan.EXPIRED))
    }

    // ─── Entry gate threshold ───

    @Test
    fun `entry gate threshold is 30`() {
        assertEquals(30, BillingViewModel.ENTRY_GATE_THRESHOLD)
    }

    @Test
    fun `trial entries left calculation is correct`() {
        val entryCount = 7
        val entriesLeft = (BillingViewModel.ENTRY_GATE_THRESHOLD - entryCount).coerceAtLeast(0)
        assertEquals(23, entriesLeft)
    }

    @Test
    fun `trial entries left does not go negative`() {
        val entryCount = 35
        val entriesLeft = (BillingViewModel.ENTRY_GATE_THRESHOLD - entryCount).coerceAtLeast(0)
        assertEquals(0, entriesLeft)
    }
}
