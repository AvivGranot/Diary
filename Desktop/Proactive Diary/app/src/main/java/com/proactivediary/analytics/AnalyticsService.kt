package com.proactivediary.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsService @Inject constructor() {

    private val analytics: FirebaseAnalytics = Firebase.analytics

    // ─── Onboarding ───
    fun logTypewriterCompleted() = log("typewriter_completed")
    fun logTypewriterSkipped() = log("typewriter_skipped")

    fun logDesignStudioCompleted(color: String, form: String, canvas: String, texture: String) {
        log("design_studio_completed", bundleOf(
            "color" to color,
            "form" to form,
            "canvas" to canvas,
            "texture" to texture
        ))
    }

    fun logOnboardingGoalsCompleted(goalCount: Int) {
        log("onboarding_goals_completed", bundleOf("goal_count" to goalCount))
    }

    fun logOnboardingGoalsSkipped() = log("onboarding_goals_skipped")

    // ─── Activation funnel ───
    fun logWriteScreenViewed() = log("write_screen_viewed")

    fun logFirstEntryStarted(wordCount: Int) {
        log("first_entry_started", bundleOf("word_count" to wordCount))
    }

    fun logShareCompleted(shareType: String) {
        log("share_completed", bundleOf("share_type" to shareType))
    }

    // ─── Writing ───
    fun logFirstEntryWritten(wordCount: Int, mood: String?, timeSinceInstallMs: Long) {
        log("first_entry_written", bundleOf(
            "word_count" to wordCount,
            "mood" to (mood ?: "none"),
            "time_since_install_sec" to (timeSinceInstallMs / 1000)
        ))
    }

    fun logEntrySaved(wordCount: Int, mood: String?, hasTitle: Boolean, sessionDurationMs: Long) {
        log("entry_saved", bundleOf(
            "word_count" to wordCount,
            "mood" to (mood ?: "none"),
            "has_title" to hasTitle,
            "session_duration_sec" to (sessionDurationMs / 1000)
        ))
    }

    // ─── Goals ───
    fun logGoalCheckedIn(goalId: String, streakCount: Int) {
        log("goal_checked_in", bundleOf(
            "goal_id" to goalId,
            "streak_count" to streakCount
        ))
    }

    fun logGoalCreated(frequency: String) {
        log("goal_created", bundleOf("frequency" to frequency))
    }

    // ─── Paywall ───
    fun logPaywallShown() = log("paywall_shown")
    fun logPaywallDismissed() = log("paywall_dismissed")

    fun logSubscriptionStarted(planType: String) {
        log("subscription_started", bundleOf("plan_type" to planType))
    }

    fun logSubscriptionCancelled() = log("subscription_cancelled")

    // ─── Notifications ───
    fun logNotificationTapped(type: String) {
        log("notification_tapped", bundleOf("type" to type))
    }

    // ─── Growth Loop (Contact Tagging) ───
    fun logContactTagged(contactCount: Int) {
        log("contact_tagged", bundleOf("contact_count" to contactCount))
    }

    fun logContactShared(hasEmail: Boolean, hasPhone: Boolean) {
        log("contact_shared", bundleOf("has_email" to hasEmail, "has_phone" to hasPhone))
    }

    // ─── App lifecycle ───
    fun logAppOpened() = log("app_opened")

    // ─── Engagement & Retention ───
    fun logSessionStart(daysSinceInstall: Int, totalEntries: Int) {
        log("session_start_enriched", bundleOf(
            "days_since_install" to daysSinceInstall,
            "total_entries" to totalEntries
        ))
    }

    fun logSessionEnd(durationSec: Long, screensViewed: Int) {
        log("session_end", bundleOf(
            "duration_sec" to durationSec,
            "screens_viewed" to screensViewed
        ))
    }

    fun logRetentionDay(day: Int, totalEntries: Int, totalWords: Int) {
        log("retention_day", bundleOf(
            "day" to day,
            "total_entries" to totalEntries,
            "total_words" to totalWords
        ))
    }

    fun logWritingStreak(streak: Int) {
        log("writing_streak", bundleOf("streak" to streak))
    }

    fun logJournalViewed(entryCount: Int, searchUsed: Boolean) {
        log("journal_viewed", bundleOf(
            "entry_count" to entryCount,
            "search_used" to searchUsed
        ))
    }

    fun logFeatureUsed(featureName: String) {
        log("feature_used", bundleOf("feature" to featureName))
    }

    fun logWritingPattern(dayOfWeek: String, hourOfDay: Int, wordCount: Int) {
        log("writing_pattern", bundleOf(
            "day_of_week" to dayOfWeek,
            "hour_of_day" to hourOfDay,
            "word_count" to wordCount
        ))
    }

    fun logExportUsed(format: String, entryCount: Int) {
        log("export_used", bundleOf("format" to format, "entry_count" to entryCount))
    }

    // ─── Conversion Funnel ───
    fun logTrialMilestone(entriesWritten: Int, entriesRemaining: Int) {
        log("trial_milestone", bundleOf(
            "entries_written" to entriesWritten,
            "entries_remaining" to entriesRemaining
        ))
    }

    fun logPaywallAction(action: String, trialEntriesUsed: Int) {
        log("paywall_action", bundleOf(
            "action" to action,
            "trial_entries_used" to trialEntriesUsed
        ))
    }

    // ─── Book Export ───
    fun logBookExported(year: Int, entryCount: Int) {
        log("book_exported", bundleOf("year" to year, "entry_count" to entryCount))
    }

    // ─── Helpers ───
    private fun log(event: String, params: Bundle? = null) {
        analytics.logEvent(event, params)
    }

    private fun bundleOf(vararg pairs: Pair<String, Any>): Bundle {
        return Bundle().apply {
            pairs.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Boolean -> putBoolean(key, value)
                    is Double -> putDouble(key, value)
                    is Float -> putFloat(key, value)
                }
            }
        }
    }
}
