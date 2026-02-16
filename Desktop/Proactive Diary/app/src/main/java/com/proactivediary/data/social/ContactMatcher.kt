package com.proactivediary.data.social

import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class MatchedContact(
    val userId: String,
    val displayName: String
)

@Singleton
class ContactMatcher @Inject constructor(
    private val functions: FirebaseFunctions
) {

    /**
     * Check if a single contact (by phone or email) is a registered user.
     * Uses hashing for privacy — raw values never sent to server.
     */
    suspend fun resolveContact(
        phone: String? = null,
        email: String? = null
    ): MatchedContact? {
        if (phone == null && email == null) return null

        val data = hashMapOf<String, Any>()
        if (phone != null) {
            data["phoneHashes"] = listOf(UserProfileRepository.hashValue(normalizePhone(phone)))
        }
        if (email != null) {
            data["emailHashes"] = listOf(UserProfileRepository.hashValue(email.lowercase().trim()))
        }

        return try {
            val result = functions.getHttpsCallable("resolveContacts")
                .call(data)
                .await()

            @Suppress("UNCHECKED_CAST")
            val resultData = result.data as? Map<String, Any>
            val matches = resultData?.get("matches") as? List<Map<String, Any>> ?: emptyList()

            matches.firstOrNull()?.let {
                MatchedContact(
                    userId = it["userId"] as String,
                    displayName = it["displayName"] as String
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun normalizePhone(phone: String): String {
        return phone.replace(Regex("[^+\\d]"), "")
    }
}
