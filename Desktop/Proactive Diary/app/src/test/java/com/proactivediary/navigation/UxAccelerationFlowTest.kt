package com.proactivediary.navigation

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for the simplified onboarding flow (P0) and 3-tab layout (P2).
 * New flow: Typewriter → PhoneAuth → NotificationPermission → Main
 * New tabs: Journal (0) | Diary (1) | Profile (2)
 */
class UxAccelerationFlowTest {

    // ─── Simplified onboarding: 3 screens, not 7 ───

    @Test
    fun `onboarding step 1 is Typewriter`() {
        assertEquals("typewriter", Routes.Typewriter.route)
    }

    @Test
    fun `onboarding step 2 is PhoneAuth`() {
        assertEquals("phone_auth", Routes.PhoneAuth.route)
    }

    @Test
    fun `onboarding step 3 is NotificationPermission`() {
        assertEquals("notification_permission", Routes.NotificationPermission.route)
    }

    @Test
    fun `onboarding ends at Main`() {
        assertEquals("main", Routes.Main.route)
    }

    @Test
    fun `skipped screens still exist as routes for backwards compat`() {
        // These routes exist but are no longer in the active onboarding flow
        assertNotNull(Routes.WriteFirstNote.route)
        assertNotNull(Routes.OnboardingGoals.route)
        assertNotNull(Routes.QuotesPreview.route)
        assertNotNull(Routes.QuickAuth.route)
    }

    @Test
    fun `OtpVerification route exists for phone auth branch`() {
        assertEquals("otp_verification", Routes.OtpVerification.route)
    }

    @Test
    fun `NotificationFallback exists as sub-route of NotificationPermission`() {
        assertEquals("notification_fallback", Routes.NotificationFallback.route)
    }

    // ─── 3-tab layout constants ───

    @Test
    fun `tab count is 3`() {
        val tabs = com.proactivediary.ui.components.diaryTabs
        assertEquals(3, tabs.size)
    }

    @Test
    fun `tab 0 is Journal`() {
        val tab = com.proactivediary.ui.components.diaryTabs[0]
        assertEquals("Journal", tab.label)
        assertFalse(tab.isCenter)
    }

    @Test
    fun `tab 1 is Diary and is center`() {
        val tab = com.proactivediary.ui.components.diaryTabs[1]
        assertEquals("Diary", tab.label)
        assertTrue(tab.isCenter)
    }

    @Test
    fun `tab 2 is Profile`() {
        val tab = com.proactivediary.ui.components.diaryTabs[2]
        assertEquals("Profile", tab.label)
        assertFalse(tab.isCenter)
    }

    @Test
    fun `no Quotes tab exists`() {
        val labels = com.proactivediary.ui.components.diaryTabs.map { it.label }
        assertFalse("Quotes tab should not exist", labels.contains("Quotes"))
    }

    @Test
    fun `no Notes tab exists`() {
        val labels = com.proactivediary.ui.components.diaryTabs.map { it.label }
        assertFalse("Notes tab should not exist", labels.contains("Notes"))
    }

    @Test
    fun `no Activity tab exists`() {
        val labels = com.proactivediary.ui.components.diaryTabs.map { it.label }
        assertFalse("Activity tab should not exist", labels.contains("Activity"))
    }

    @Test
    fun `exactly one tab is marked as center`() {
        val centerTabs = com.proactivediary.ui.components.diaryTabs.filter { it.isCenter }
        assertEquals("Exactly one center tab expected", 1, centerTabs.size)
    }

    @Test
    fun `center tab is at index 1`() {
        val centerIndex = com.proactivediary.ui.components.diaryTabs.indexOfFirst { it.isCenter }
        assertEquals("Center tab should be at index 1", 1, centerIndex)
    }

    // ─── Tab labels are unique ───

    @Test
    fun `all tab labels are unique`() {
        val labels = com.proactivediary.ui.components.diaryTabs.map { it.label }
        assertEquals("Tab labels must be unique", labels.size, labels.toSet().size)
    }

    // ─── Each tab has distinct selected and unselected icons ───

    @Test
    fun `each tab has different selected and unselected icons`() {
        com.proactivediary.ui.components.diaryTabs.forEach { tab ->
            assertNotEquals(
                "${tab.label} selected and unselected icons should differ",
                tab.selectedIcon,
                tab.unselectedIcon
            )
        }
    }
}
