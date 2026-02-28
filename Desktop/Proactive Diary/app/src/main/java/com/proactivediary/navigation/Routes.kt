package com.proactivediary.navigation

sealed class Routes(val route: String) {
    object Typewriter : Routes("typewriter")
    object OnboardingGoals : Routes("onboarding_goals")
    object NotificationPermission : Routes("notification_permission")
    object Main : Routes("main")
    object Write : Routes("write?entryId={entryId}") {
        fun create(entryId: String? = null): String =
            if (entryId != null) "write?entryId=$entryId" else "write"
    }
    object Journal : Routes("journal")
    object Goals : Routes("goals")
    object Reminders : Routes("reminders")
    object Settings : Routes("settings")
    object EntryDetail : Routes("entry/{entryId}") {
        fun createRoute(entryId: String) = "entry/$entryId"
    }
    object YearInReview : Routes("year_in_review")
    object OnThisDay : Routes("on_this_day")
    object ThemeEvolution : Routes("theme_evolution")
    object ContactSupport : Routes("contact_support?category={category}") {
        fun createRoute(category: String = "support") = "contact_support?category=$category"
    }

    // New screens (v3 redesign)
    object Layout : Routes("layout")
    object ExportData : Routes("export_data")

    // Social features
    object Quotes : Routes("quotes")
    object QuoteDetail : Routes("quote/{quoteId}") {
        fun createRoute(quoteId: String) = "quote/$quoteId"
    }

    // Phone auth onboarding
    object PhoneAuth : Routes("phone_auth")
    object OtpVerification : Routes("otp_verification")
    object NotificationFallback : Routes("notification_fallback")

    // Social onboarding (DesignStudio removed from flow)
    object QuickAuth : Routes("quick_auth")
    object WriteFirstNote : Routes("write_first_note?next={next}") {
        fun create(next: String = "quotes_preview"): String =
            "write_first_note?next=$next"
    }
    object QuotesPreview : Routes("quotes_preview")

    // Phase 1: Entry Management
    object RecentlyDeleted : Routes("recently_deleted")

    // Phase 3: Wellbeing
    object WellbeingDashboard : Routes("wellbeing_dashboard")

    // Phase 4: Multi-Journal
    object JournalManage : Routes("journal_manage")
    object CreateJournal : Routes("create_journal")

    // Phase 5: Security
    object SecuritySettings : Routes("security_settings")
    object LockScreen : Routes("lock_screen")
    object EnhancedExport : Routes("enhanced_export")

    // Phase 6: Places Map
    object PlacesMap : Routes("places_map")

    // Phase 7: Suggestions
    object SuggestionsFeed : Routes("suggestions_feed")
    object PrivacyControls : Routes("privacy_controls")

    // Phase 8: Sharing
    object ShareReceiver : Routes("share_receiver")

    // Visual Story
    object StoryGenerator : Routes("story_generator/{entryId}") {
        fun createRoute(entryId: String) = "story_generator/$entryId"
    }
}

