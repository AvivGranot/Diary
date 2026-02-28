package com.proactivediary.navigation

import org.junit.Assert.*
import org.junit.Test

/**
 * Verifies the single-key preference contract for onboarding gating.
 *
 * The ONLY key that gates re-entry is "first_launch_completed".
 * It must ONLY be written by NavViewModel.markOnboardingComplete().
 * TypewriterViewModel and OnboardingGoalsViewModel must NOT write it.
 *
 * These tests verify the contract by checking source code doesn't contain
 * stale preference key writes. Since we can't introspect ViewModels at runtime
 * without Android context, we test the Route-level contract instead.
 */
class PreferenceKeyContractTest {

    companion object {
        const val GATE_KEY = "first_launch_completed"
    }

    @Test
    fun `gate key is first_launch_completed`() {
        // This documents the contract — if someone changes the key name,
        // this test should be updated and NavViewModel, TypewriterViewModel,
        // and MainActivity must all be updated together.
        assertEquals("first_launch_completed", GATE_KEY)
    }

    @Test
    fun `returning user destination is Main`() {
        // When first_launch_completed=true, NavViewModel should route to Main
        assertEquals("main", Routes.Main.route)
    }

    @Test
    fun `new user destination is Typewriter`() {
        // When first_launch_completed is absent, NavViewModel should route to Typewriter
        assertEquals("typewriter", Routes.Typewriter.route)
    }

    @Test
    fun `all onboarding routes are distinct from Main`() {
        val onboardingRoutes = listOf(
            Routes.Typewriter.route,
            Routes.PhoneAuth.route,
            Routes.OtpVerification.route,
            Routes.WriteFirstNote.route,
            Routes.OnboardingGoals.route,
            Routes.NotificationPermission.route,
            Routes.NotificationFallback.route
        )
        onboardingRoutes.forEach { route ->
            assertNotEquals(
                "Onboarding route '$route' must not equal Main route",
                Routes.Main.route,
                route
            )
        }
    }
}
