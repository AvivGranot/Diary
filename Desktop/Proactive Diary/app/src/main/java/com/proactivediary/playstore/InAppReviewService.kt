package com.proactivediary.playstore

import android.app.Activity
import com.google.android.play.core.review.ReviewManagerFactory
import com.proactivediary.data.db.dao.PreferenceDao
import com.proactivediary.data.db.entities.PreferenceEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InAppReviewService @Inject constructor(
    private val preferenceDao: PreferenceDao
) {
    companion object {
        private const val KEY_ENTRIES_SINCE_REVIEW = "entries_since_review"
        private const val KEY_LAST_REVIEW_PROMPT = "last_review_prompt_ms"
        private const val ENTRY_THRESHOLD = 10
        private const val MIN_DAYS_BETWEEN_PROMPTS = 60L
    }

    suspend fun maybeRequestReview(activity: Activity) {
        val entriesSinceReview = preferenceDao.get(KEY_ENTRIES_SINCE_REVIEW)?.value?.toIntOrNull() ?: 0
        val lastPrompt = preferenceDao.get(KEY_LAST_REVIEW_PROMPT)?.value?.toLongOrNull() ?: 0L
        val daysSinceLastPrompt = (System.currentTimeMillis() - lastPrompt) / (1000 * 60 * 60 * 24)

        if (entriesSinceReview < ENTRY_THRESHOLD) return
        if (lastPrompt > 0 && daysSinceLastPrompt < MIN_DAYS_BETWEEN_PROMPTS) return

        val manager = ReviewManagerFactory.create(activity)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                manager.launchReviewFlow(activity, task.result)
                // Only reset counters after successfully launching the review flow
                preferenceDao.insertSync(PreferenceEntity(KEY_ENTRIES_SINCE_REVIEW, "0"))
                preferenceDao.insertSync(PreferenceEntity(KEY_LAST_REVIEW_PROMPT, System.currentTimeMillis().toString()))
            }
        }
    }

    suspend fun incrementEntryCount() {
        val current = preferenceDao.get(KEY_ENTRIES_SINCE_REVIEW)?.value?.toIntOrNull() ?: 0
        preferenceDao.insert(PreferenceEntity(KEY_ENTRIES_SINCE_REVIEW, (current + 1).toString()))
    }
}
