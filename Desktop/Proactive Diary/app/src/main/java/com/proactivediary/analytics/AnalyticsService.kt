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

    // ─── App lifecycle ───
    fun logAppOpened() = log("app_opened")

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
