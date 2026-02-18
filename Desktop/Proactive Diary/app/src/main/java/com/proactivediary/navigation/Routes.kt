package com.proactivediary.navigation

sealed class Routes(val route: String) {
    object Discover : Routes("discover")
    object Typewriter : Routes("typewriter")
    object DesignStudio : Routes("design_studio?edit={edit}") {
        fun createRoute(edit: Boolean = false) = "design_studio?edit=$edit"
    }
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
    object DiaryWrapped : Routes("diary_wrapped")
    object ThemeEvolution : Routes("theme_evolution")
    object ContactSupport : Routes("contact_support?category={category}") {
        fun createRoute(category: String = "support") = "contact_support?category=$category"
    }

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

    // New onboarding
    object QuickAuth : Routes("quick_auth")
    object ProfilePicture : Routes("profile_picture")
    object WriteFirstNote : Routes("write_first_note")
    object QuotesPreview : Routes("quotes_preview")
}
