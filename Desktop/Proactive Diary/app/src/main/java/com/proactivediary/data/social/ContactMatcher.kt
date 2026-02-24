package com.proactivediary.data.social

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class MatchedContact(
    val userId: String,
    val displayName: String
)

@Singleton
class ContactMatcher @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    /**
     * Check if a single contact (by phone or email) is a registered user.
     * Uses hashing for privacy — raw values never sent to server.
     * Queries Firestore directly instead of Cloud Function.
     */
    suspend fun resolveContact(
        phone: String? = null,
        email: String? = null
    ): MatchedContact? {
        if (phone == null && email == null) return null
        val myUid = auth.currentUser?.uid ?: return null

        return try {
            // Try phone hash first
            if (phone != null) {
                val phoneHash = UserProfileRepository.hashValue(normalizePhone(phone))
                val snap = firestore.collection("users")
                    .whereEqualTo("phoneHash", phoneHash)
                    .limit(1)
                    .get()
                    .await()

                snap.documents.firstOrNull()?.let { doc ->
                    if (doc.id != myUid) {
                        return MatchedContact(
                            userId = doc.id,
                            displayName = doc.data?.get("displayName") as? String ?: "Anonymous"
                        )
                    }
                }
            }

            // Try email hash
            if (email != null) {
                val emailHash = UserProfileRepository.hashValue(email.lowercase().trim())
                val snap = firestore.collection("users")
                    .whereEqualTo("emailHash", emailHash)
                    .limit(1)
                    .get()
                    .await()

                snap.documents.firstOrNull()?.let { doc ->
                    if (doc.id != myUid) {
                        return MatchedContact(
                            userId = doc.id,
                            displayName = doc.data?.get("displayName") as? String ?: "Anonymous"
                        )
                    }
                }
            }

            null
        } catch (e: Exception) {
            null
        }
    }

    private fun normalizePhone(phone: String): String {
        return phone.replace(Regex("[^+\\d]"), "")
    }
}
