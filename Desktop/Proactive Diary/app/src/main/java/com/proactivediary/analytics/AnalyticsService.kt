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
    fun logFirstEntryWritten(wordCount: Int, timeSinceInstallMs: Long) {
        log("first_entry_written", bundleOf(
            "word_count" to wordCount,
            "time_since_install_sec" to (timeSinceInstallMs / 1000)
        ))
    }

    fun logEntrySaved(wordCount: Int, hasTitle: Boolean, sessionDurationMs: Long) {
        log("entry_saved", bundleOf(
            "word_count" to wordCount,
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

    // ─── User Properties (Segmentation) ───
    fun setUserProperties(
        planType: String,
        totalEntries: Int,
        daysSinceInstall: Int,
        writingStreak: Int,
        onboardingCompleted: Boolean,
        hasGoals: Boolean,
        designCustomized: Boolean
    ) {
        analytics.setUserProperty("plan_type", planType)
        analytics.setUserProperty("total_entries", bucketEntries(totalEntries))
        analytics.setUserProperty("days_since_install", bucketDays(daysSinceInstall))
        analytics.setUserProperty("writing_streak", bucketStreak(writingStreak))
        analytics.setUserProperty("onboarding_completed", onboardingCompleted.toString())
        analytics.setUserProperty("has_goals", hasGoals.toString())
        analytics.setUserProperty("design_customized", designCustomized.toString())
    }

    private fun bucketEntries(count: Int): String = when {
        count == 0 -> "0"
        count <= 5 -> "1-5"
        count <= 10 -> "6-10"
        count <= 25 -> "11-25"
        count <= 50 -> "26-50"
        else -> "51+"
    }

    private fun bucketDays(days: Int): String = when {
        days == 0 -> "0"
        days == 1 -> "1"
        days <= 7 -> "2-7"
        days <= 14 -> "8-14"
        days <= 30 -> "15-30"
        else -> "31+"
    }

    private fun bucketStreak(streak: Int): String = when {
        streak == 0 -> "0"
        streak <= 3 -> "1-3"
        streak <= 7 -> "4-7"
        streak <= 14 -> "8-14"
        else -> "15+"
    }

    // ─── Support ───
    fun logSupportRequestSubmitted(category: String) {
        log("support_request_submitted", bundleOf("category" to category))
    }

    fun logOnboardingCompleted(designCustomized: Boolean, goalsSet: Int) {
        log("onboarding_completed", bundleOf(
            "design_customized" to designCustomized,
            "goals_set" to goalsSet
        ))
    }

    // ─── Social Onboarding Funnel ───
    fun logOnboardingStart() = log("onboarding_start")

    fun logOnboardingSplashComplete(durationMs: Long) {
        log("onboarding_splash_complete", bundleOf("duration_ms" to durationMs))
    }

    fun logOnboardingAuthStart() = log("onboarding_auth_start")

    fun logOnboardingAuthComplete(method: String, durationMs: Long) {
        log("onboarding_auth_complete", bundleOf("method" to method, "duration_ms" to durationMs))
    }

    fun logOnboardingAuthSkip() = log("onboarding_auth_skip")

    fun logOnboardingFirstNoteShown() = log("onboarding_first_note_shown")

    fun logOnboardingFirstNoteComplete(wordCount: Int, durationMs: Long) {
        log("onboarding_first_note_complete", bundleOf("word_count" to wordCount, "duration_ms" to durationMs))
    }

    fun logOnboardingFirstNoteSkip() = log("onboarding_first_note_skip")

    fun logOnboardingQuotesShown() = log("onboarding_quotes_shown")

    fun logOnboardingQuotesComplete(durationMs: Long) {
        log("onboarding_quotes_complete", bundleOf("duration_ms" to durationMs))
    }

    fun logOnboardingNotifShown() = log("onboarding_notif_shown")
    fun logOnboardingNotifGranted() = log("onboarding_notif_granted")
    fun logOnboardingNotifDenied() = log("onboarding_notif_denied")

    // ─── Social — Notes (Viral Loop) ───
    fun logContactsImported(totalContacts: Int, matchCount: Int) {
        log("contacts_imported", bundleOf("total_contacts" to totalContacts, "match_count" to matchCount))
    }

    fun logNoteComposeStart() = log("note_compose_start")

    fun logNoteSent(wordCount: Int) {
        log("note_sent", bundleOf("word_count" to wordCount))
    }

    fun logNoteInboxOpened(unreadCount: Int) {
        log("note_inbox_opened", bundleOf("unread_count" to unreadCount))
    }

    fun logNoteRead(noteId: String) {
        log("note_read", bundleOf("note_id" to noteId))
    }

    fun logNotePushTapped(noteId: String) {
        log("note_push_tapped", bundleOf("note_id" to noteId))
    }

    fun logNoteInviteSent() = log("note_invite_sent")

    // ─── Social — Quotes (Engagement Loop) ───
    fun logQuotesTabViewed() = log("quotes_tab_viewed")
    fun logQuoteComposeStart() = log("quote_compose_start")

    fun logQuoteSubmitted(wordCount: Int) {
        log("quote_submitted", bundleOf("word_count" to wordCount))
    }

    fun logQuoteLiked(quoteId: String) {
        log("quote_liked", bundleOf("quote_id" to quoteId))
    }

    fun logQuoteUnliked(quoteId: String) {
        log("quote_unliked", bundleOf("quote_id" to quoteId))
    }

    fun logCommentSubmitted(quoteId: String) {
        log("comment_submitted", bundleOf("quote_id" to quoteId))
    }

    fun logLeaderboardPeriodChanged(period: String) {
        log("leaderboard_period_changed", bundleOf("period" to period))
    }

    // ─── Profile Picture ───
    fun logProfilePictureScreenShown() = log("profile_picture_shown")
    fun logProfilePictureSkipped() = log("profile_picture_skipped")

    fun logProfilePhotoUploaded(source: String) {
        log("profile_photo_uploaded", bundleOf("source" to source))
    }

    // ─── Navigation ───
    fun logTabSwitched(tab: String) {
        log("tab_switched", bundleOf("tab" to tab))
    }

    fun logAppOpenedWithSource(source: String) {
        log("app_opened_source", bundleOf("source" to source))
    }

    // ─── Billing (extended) ───
    fun logEntryLimitHit(entryCount: Int) {
        log("entry_limit_hit", bundleOf("entry_count" to entryCount))
    }

    fun logPaywallShownWithTrigger(trigger: String) {
        log("paywall_shown", bundleOf("trigger" to trigger))
    }

    // ─── V2 Feature Analytics ───
    fun logPhotoAttached() = log("photo_attached")
    fun logPhotoRemoved() = log("photo_removed")
    fun logLocationCaptured() = log("location_captured")
    fun logWeatherCaptured() = log("weather_captured")

    fun logTemplateUsed(templateId: String) {
        log("template_used", bundleOf("template_id" to templateId))
    }

    fun logVoiceNoteRecorded(durationMs: Long) {
        log("voice_note_recorded", bundleOf("duration_ms" to durationMs))
    }

    fun logAIInsightViewed() = log("ai_insight_viewed")
    fun logAIPromptUsed() = log("ai_prompt_used")
    fun logCalendarViewOpened() = log("calendar_view_opened")
    fun logGalleryViewOpened() = log("gallery_view_opened")
    fun logSwipeNavigationUsed() = log("swipe_navigation_used")

    // ─── Sync ───

    fun logSyncRestoreStarted(cloudEntryCount: Int) {
        log("sync_restore_started", Bundle().apply { putInt("cloud_entries", cloudEntryCount) })
    }

    fun logSyncRestoreCompleted(entryCount: Int, goalCount: Int, durationMs: Long) {
        log("sync_restore_completed", Bundle().apply {
            putInt("entries", entryCount)
            putInt("goals", goalCount)
            putLong("duration_ms", durationMs)
        })
    }

    fun logSyncRestoreFailed(error: String) {
        log("sync_restore_failed", Bundle().apply { putString("error", error) })
    }

    fun logGuestDataMerged(localEntries: Int, cloudEntries: Int) {
        log("guest_data_merged", Bundle().apply {
            putInt("local_entries", localEntries)
            putInt("cloud_entries", cloudEntries)
        })
    }

    fun logSyncPushSuccess(source: String, count: Int) {
        log("sync_push_success", Bundle().apply {
            putString("source", source)
            putInt("count", count)
        })
    }

    fun logSyncPushFailed(source: String, error: String) {
        log("sync_push_failed", Bundle().apply {
            putString("source", source)
            putString("error", error)
        })
    }

    // ─── Sydney ───
    fun logSydneyCapture(wordCount: Int, durationMs: Long) {
        log("sydney_capture", bundleOf(
            "word_count" to wordCount,
            "duration_ms" to durationMs
        ))
    }

    fun logSydneyListenerToggled(enabled: Boolean) {
        log("sydney_listener_toggled", bundleOf("enabled" to enabled))
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
