package com.proactivediary.data.social

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    /**
     * Fast client-side write: guarantee the user doc exists (~100ms).
     * Called before the slow profile creation path so downstream .set(merge) calls never fail.
     */
    suspend fun ensureMinimalProfile(
        displayName: String?,
        email: String?,
        photoUrl: String?
    ) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid)
            .set(
                mapOf(
                    "displayName" to (displayName ?: "Anonymous"),
                    "photoUrl" to photoUrl,
                    "emailHash" to email?.let { hashValue(it.lowercase().trim()) }
                ),
                SetOptions.merge()
            )
            .await()
    }

    /**
     * Create or update user profile directly in Firestore.
     * Also increments global user counter for new users and sends welcome note.
     */
    suspend fun createOrUpdateProfile(
        displayName: String?,
        phone: String? = null,
        email: String? = null,
        photoUrl: String? = null
    ): Result<Boolean> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Not signed in"))
        return try {
            val userRef = firestore.collection("users").document(uid)
            val existing = userRef.get().await()

            if (existing.exists() && existing.data?.containsKey("createdAt") == true) {
                // Returning user — just update mutable fields
                val updates = mutableMapOf<String, Any?>()
                if (displayName != null) updates["displayName"] = displayName
                if (photoUrl != null) updates["photoUrl"] = photoUrl
                if (updates.isNotEmpty()) userRef.update(updates).await()

                CoroutineScope(Dispatchers.IO).launch { updateFcmToken() }
                Result.success(false)
            } else {
                // New user — create profile + counter + welcome note
                val batch = firestore.batch()

                batch.set(userRef, mapOf(
                    "displayName" to (displayName ?: "Anonymous"),
                    "phoneHash" to phone?.let { hashValue(normalizePhone(it)) },
                    "emailHash" to email?.let { hashValue(it.lowercase().trim()) },
                    "photoUrl" to photoUrl,
                    "fcmToken" to null,
                    "noteCount" to 0,
                    "quoteCount" to 0,
                    "createdAt" to FieldValue.serverTimestamp()
                ), SetOptions.merge())

                // Increment global user counter
                val counterRef = firestore.collection("counters").document("global")
                batch.set(counterRef,
                    mapOf("userCount" to FieldValue.increment(1)),
                    SetOptions.merge())

                // Send welcome note
                val welcomeRef = firestore.collection("notes").document()
                batch.set(welcomeRef, mapOf(
                    "senderId" to "system",
                    "senderName" to "Proactive Diary",
                    "recipientId" to uid,
                    "content" to "Welcome. Someone out there is glad you\u2019re here. This is your space to think, write, and grow. \u2728",
                    "status" to "delivered",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "readAt" to null
                ))

                batch.commit().await()

                CoroutineScope(Dispatchers.IO).launch { updateFcmToken() }
                Result.success(true)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateFcmToken() {
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            val uid = auth.currentUser?.uid ?: return
            firestore.collection("users").document(uid)
                .update("fcmToken", token)
                .await()
        } catch (e: Exception) {
            // Non-fatal
        }
    }

    /**
     * Read global counters directly from Firestore (public read).
     */
    suspend fun getGlobalCounters(): Pair<Long, Long> {
        return try {
            val doc = firestore.collection("counters").document("global")
                .get()
                .await()
            if (!doc.exists()) return Pair(0L, 0L)
            val data = doc.data ?: return Pair(0L, 0L)
            val userCount = (data["userCount"] as? Number)?.toLong() ?: 0L
            val quoteCount = (data["quoteCount"] as? Number)?.toLong() ?: 0L
            Pair(userCount, quoteCount)
        } catch (e: Exception) {
            Pair(0L, 0L)
        }
    }

    private fun normalizePhone(phone: String): String {
        return phone.replace(Regex("[^+\\d]"), "")
    }

    companion object {
        fun hashValue(value: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(value.toByteArray(Charsets.UTF_8))
            return hashBytes.joinToString("") { "%02x".format(it) }
        }
    }
}
