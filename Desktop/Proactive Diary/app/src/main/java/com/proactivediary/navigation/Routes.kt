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
    object ProfilePicture : Routes("profile_picture")
    object WriteFirstNote : Routes("write_first_note?next={next}") {
        fun create(next: String = "quotes_preview") = "write_first_note?next=$next"
    }
    object QuotesPreview : Routes("quotes_preview")

}
