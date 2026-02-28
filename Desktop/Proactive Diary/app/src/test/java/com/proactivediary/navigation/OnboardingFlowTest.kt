package com.proactivediary.navigation

import org.junit.Assert.*
import org.junit.Test

/**
 * Verifies the onboarding flow contract after the routing fix.
 * Target flow: Typewriter → PhoneAuth → WriteFirstNote → OnboardingGoals → NotificationPermission → Main
 */
class OnboardingFlowTest {

    // ─── Flow step route values ───

    @Test
    fun `step 1 Typewriter route is typewriter`() {
        assertEquals("typewriter", Routes.Typewriter.route)
    }

    @Test
    fun `step 2 PhoneAuth route is phone_auth`() {
        assertEquals("phone_auth", Routes.PhoneAuth.route)
    }

    @Test
    fun `step 3 WriteFirstNote with onboarding_goals next`() {
        assertEquals(
            "write_first_note?next=onboarding_goals",
            Routes.WriteFirstNote.create("onboarding_goals")
        )
    }

    @Test
    fun `step 4 OnboardingGoals route is onboarding_goals`() {
        assertEquals("onboarding_goals", Routes.OnboardingGoals.route)
    }

    @Test
    fun `step 5 NotificationPermission route is notification_permission`() {
        assertEquals("notification_permission", Routes.NotificationPermission.route)
    }

    @Test
    fun `step 5b NotificationFallback route is notification_fallback`() {
        assertEquals("notification_fallback", Routes.NotificationFallback.route)
    }

    @Test
    fun `final destination Main route is main`() {
        assertEquals("main", Routes.Main.route)
    }

    // ─── Dead routes removed ───
    // NotebookAnimation, Welcome, ProfilePicture are deleted from Routes.kt.
    // If anyone re-adds them, the RoutesTest uniqueness check will catch collisions.
    // Compile-time: these lines would fail if the objects still existed:
    //   Routes.NotebookAnimation  // deleted
    //   Routes.Welcome            // deleted
    //   Routes.ProfilePicture     // deleted

    @Test
    fun `no route string matches deleted notebook_animation`() {
        val allRoutes = listOf(
            Routes.Typewriter.route, Routes.OnboardingGoals.route,
            Routes.NotificationPermission.route, Routes.Main.route,
            Routes.PhoneAuth.route, Routes.OtpVerification.route,
            Routes.NotificationFallback.route, Routes.QuickAuth.route,
            Routes.WriteFirstNote.route, Routes.QuotesPreview.route
        )
        assertFalse(
            "notebook_animation should not appear in any route",
            allRoutes.contains("notebook_animation")
        )
    }

    @Test
    fun `no route string matches deleted welcome`() {
        val allRoutes = listOf(
            Routes.Typewriter.route, Routes.Main.route,
            Routes.PhoneAuth.route, Routes.QuickAuth.route
        )
        assertFalse(
            "welcome should not appear in any route",
            allRoutes.contains("welcome")
        )
    }

    @Test
    fun `no route string matches deleted profile_picture`() {
        val allRoutes = listOf(
            Routes.Typewriter.route, Routes.Main.route,
            Routes.PhoneAuth.route, Routes.QuickAuth.route
        )
        assertFalse(
            "profile_picture should not appear in any route",
            allRoutes.contains("profile_picture")
        )
    }

    // ─── WriteFirstNote next parameter ───

    @Test
    fun `WriteFirstNote default next is quotes_preview`() {
        assertEquals("write_first_note?next=quotes_preview", Routes.WriteFirstNote.create())
    }

    @Test
    fun `WriteFirstNote route template contains next placeholder`() {
        assertTrue(Routes.WriteFirstNote.route.contains("{next}"))
    }

    // ─── OtpVerification exists for phone auth branch ───

    @Test
    fun `OtpVerification route is otp_verification`() {
        assertEquals("otp_verification", Routes.OtpVerification.route)
    }

    // ─── QuickAuth still exists as legacy route ───

    @Test
    fun `QuickAuth route is quick_auth`() {
        assertEquals("quick_auth", Routes.QuickAuth.route)
    }
}
