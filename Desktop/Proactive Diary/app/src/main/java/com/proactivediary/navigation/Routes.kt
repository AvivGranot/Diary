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
    object ComposeNote : Routes("compose_note")
    object NoteInbox : Routes("note_inbox")
    object EnvelopeReveal : Routes("envelope/{noteId}") {
        fun createRoute(noteId: String) = "envelope/$noteId"
    }
    object Quotes : Routes("quotes")
    object QuoteDetail : Routes("quote/{quoteId}") {
        fun createRoute(quoteId: String) = "quote/$quoteId"
    }

    // Social onboarding (DesignStudio removed from flow)
    object QuickAuth : Routes("quick_auth")
    object Welcome : Routes("welcome")
    object ProfilePicture : Routes("profile_picture")
    object WriteFirstNote : Routes("write_first_note?next={next}&channel={channel}&contactName={contactName}&contactPhone={contactPhone}&contactEmail={contactEmail}") {
        fun create(
            next: String = "quotes_preview",
            channel: String = "contacts",
            contactName: String? = null,
            contactPhone: String? = null,
            contactEmail: String? = null
        ): String {
            var route = "write_first_note?next=$next&channel=$channel"
            if (contactName != null) route += "&contactName=${android.net.Uri.encode(contactName)}"
            if (contactPhone != null) route += "&contactPhone=${android.net.Uri.encode(contactPhone)}"
            if (contactEmail != null) route += "&contactEmail=${android.net.Uri.encode(contactEmail)}"
            return route
        }
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
}

