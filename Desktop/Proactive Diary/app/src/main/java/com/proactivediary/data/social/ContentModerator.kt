package com.proactivediary.data.social

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Client-side content moderation. Provides instant feedback before server-side Gemini check.
 * Server-side moderation in Cloud Functions is the authoritative check.
 */
@Singleton
class ContentModerator @Inject constructor() {

    private val blockedWords = listOf(
        "fuck", "shit", "bitch", "asshole", "bastard", "damn", "crap",
        "hate you", "kill you", "die", "kys", "ugly", "stupid", "loser",
        "worthless", "pathetic", "disgusting", "trash"
    )

    fun check(text: String): ModerationResult {
        val lower = text.lowercase()
        val found = blockedWords.firstOrNull { lower.contains(it) }
        return if (found != null) {
            ModerationResult(
                isAllowed = false,
                reason = "Your message contains language that doesn't align with our positivity guidelines. Try rephrasing with kindness!"
            )
        } else {
            ModerationResult(isAllowed = true, reason = null)
        }
    }
}

data class ModerationResult(
    val isAllowed: Boolean,
    val reason: String?
)
