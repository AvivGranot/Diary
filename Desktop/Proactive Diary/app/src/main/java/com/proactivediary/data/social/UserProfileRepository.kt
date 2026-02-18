package com.proactivediary.data.social

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val functions: FirebaseFunctions,
    private val auth: FirebaseAuth
) {

    suspend fun createOrUpdateProfile(
        displayName: String?,
        phone: String? = null,
        email: String? = null,
        photoUrl: String? = null
    ): Result<Boolean> {
        return try {
            val fcmToken = try {
                FirebaseMessaging.getInstance().token.await()
            } catch (e: Exception) {
                null
            }

            val data = hashMapOf<String, Any?>(
                "displayName" to (displayName ?: "Anonymous"),
                "phoneHash" to phone?.let { hashValue(normalizePhone(it)) },
                "emailHash" to email?.let { hashValue(it.lowercase().trim()) },
                "fcmToken" to fcmToken,
                "photoUrl" to photoUrl
            )

            val result = functions.getHttpsCallable("createUserProfile")
                .call(data)
                .await()

            @Suppress("UNCHECKED_CAST")
            val resultData = result.data as? Map<String, Any>
            val created = resultData?.get("created") as? Boolean ?: false
            Result.success(created)
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

    suspend fun getGlobalCounters(): Pair<Long, Long> {
        return try {
            val result = functions.getHttpsCallable("getCounters")
                .call()
                .await()
            @Suppress("UNCHECKED_CAST")
            val data = result.data as? Map<String, Any> ?: emptyMap()
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
