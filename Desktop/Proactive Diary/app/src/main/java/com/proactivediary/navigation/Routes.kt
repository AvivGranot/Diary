package com.proactivediary.navigation

sealed class Routes(val route: String) {
    object Typewriter : Routes("typewriter")
    object DesignStudio : Routes("design_studio?edit={edit}") {
        fun createRoute(edit: Boolean = false) = "design_studio?edit=$edit"
    }
    object OnboardingGoals : Routes("onboarding_goals")
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
}
