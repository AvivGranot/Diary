package com.proactivediary.navigation

import org.junit.Assert.*
import org.junit.Test

class RoutesTest {

    // ─── Route string uniqueness ───

    @Test
    fun `all route strings are unique`() {
        val routes = listOf(
            Routes.Typewriter.route,
            Routes.OnboardingGoals.route,
            Routes.NotificationPermission.route,
            Routes.Main.route,
            Routes.Write.route,
            Routes.Journal.route,
            Routes.Goals.route,
            Routes.Reminders.route,
            Routes.Settings.route,
            Routes.EntryDetail.route,
            Routes.YearInReview.route,
            Routes.OnThisDay.route,
            Routes.ThemeEvolution.route,
            Routes.ContactSupport.route,
            Routes.Layout.route,
            Routes.ExportData.route,
            Routes.Quotes.route,
            Routes.QuoteDetail.route,
            Routes.PhoneAuth.route,
            Routes.OtpVerification.route,
            Routes.NotificationFallback.route,
            Routes.QuickAuth.route,
            Routes.WriteFirstNote.route,
            Routes.QuotesPreview.route,
            Routes.RecentlyDeleted.route,
            Routes.WellbeingDashboard.route,
            Routes.SecuritySettings.route,
            Routes.EnhancedExport.route,
            Routes.PlacesMap.route,
            Routes.SuggestionsFeed.route,
            Routes.PrivacyControls.route,
            Routes.StoryGenerator.route
        )
        assertEquals("Route strings must be unique", routes.size, routes.toSet().size)
    }

    // ─── Parameterized route creation ───

    @Test
    fun `Write create with entryId returns correct route`() {
        assertEquals("write?entryId=abc123", Routes.Write.create("abc123"))
    }

    @Test
    fun `Write create without entryId returns base route`() {
        assertEquals("write", Routes.Write.create())
    }

    @Test
    fun `Write create with null entryId returns base route`() {
        assertEquals("write", Routes.Write.create(null))
    }

    @Test
    fun `EntryDetail createRoute formats correctly`() {
        assertEquals("entry/abc123", Routes.EntryDetail.createRoute("abc123"))
    }

    @Test
    fun `EntryDetail createRoute with UUID`() {
        val uuid = "550e8400-e29b-41d4-a716-446655440000"
        assertEquals("entry/$uuid", Routes.EntryDetail.createRoute(uuid))
    }

    @Test
    fun `ContactSupport createRoute with default category`() {
        assertEquals("contact_support?category=support", Routes.ContactSupport.createRoute())
    }

    @Test
    fun `ContactSupport createRoute with custom category`() {
        assertEquals("contact_support?category=bug", Routes.ContactSupport.createRoute("bug"))
    }

    @Test
    fun `WriteFirstNote create with default next`() {
        assertEquals("write_first_note?next=quotes_preview", Routes.WriteFirstNote.create())
    }

    @Test
    fun `WriteFirstNote create with custom next`() {
        assertEquals(
            "write_first_note?next=onboarding_goals",
            Routes.WriteFirstNote.create("onboarding_goals")
        )
    }

    @Test
    fun `QuoteDetail createRoute formats correctly`() {
        assertEquals("quote/q42", Routes.QuoteDetail.createRoute("q42"))
    }

    @Test
    fun `StoryGenerator createRoute formats correctly`() {
        assertEquals("story_generator/entry99", Routes.StoryGenerator.createRoute("entry99"))
    }

    // ─── Onboarding route values ───

    @Test
    fun `onboarding routes exist for full flow`() {
        // Verify the onboarding chain: Typewriter → PhoneAuth → WriteFirstNote → OnboardingGoals → NotificationPermission
        assertNotNull(Routes.Typewriter.route)
        assertNotNull(Routes.PhoneAuth.route)
        assertNotNull(Routes.WriteFirstNote.route)
        assertNotNull(Routes.OnboardingGoals.route)
        assertNotNull(Routes.NotificationPermission.route)
        assertNotNull(Routes.NotificationFallback.route)
    }

    // ─── Route string format ───

    @Test
    fun `parameterized routes contain placeholder braces`() {
        assertTrue(Routes.Write.route.contains("{entryId}"))
        assertTrue(Routes.EntryDetail.route.contains("{entryId}"))
        assertTrue(Routes.ContactSupport.route.contains("{category}"))
        assertTrue(Routes.WriteFirstNote.route.contains("{next}"))
        assertTrue(Routes.QuoteDetail.route.contains("{quoteId}"))
        assertTrue(Routes.StoryGenerator.route.contains("{entryId}"))
    }

    @Test
    fun `simple routes do not contain braces`() {
        val simpleRoutes = listOf(
            Routes.Typewriter, Routes.Main, Routes.Journal,
            Routes.Goals, Routes.Settings, Routes.NotificationPermission
        )
        simpleRoutes.forEach { route ->
            assertFalse(
                "${route.route} should not contain {",
                route.route.contains("{")
            )
        }
    }
}
