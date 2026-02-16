package com.proactivediary.data.social

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class Note(
    val id: String,
    val content: String,
    val status: String,
    val createdAt: Long,
    val readAt: Long?
)

@Singleton
class NotesRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val functions: FirebaseFunctions,
    private val auth: FirebaseAuth
) {

    /**
     * Send an anonymous positive note to a registered user.
     * Content moderation happens server-side via Gemini.
     */
    suspend fun sendNote(recipientId: String, content: String): Result<String> {
        return try {
            val data = hashMapOf(
                "recipientId" to recipientId,
                "content" to content
            )
            val result = functions.getHttpsCallable("sendNote")
                .call(data)
                .await()

            @Suppress("UNCHECKED_CAST")
            val resultData = result.data as? Map<String, Any>
            val noteId = resultData?.get("noteId") as? String ?: ""
            Result.success(noteId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Real-time listener for received notes (newest first).
     */
    fun observeReceivedNotes(): Flow<List<Note>> = callbackFlow {
        val uid = auth.currentUser?.uid ?: run {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = firestore.collection("notes")
            .whereEqualTo("recipientId", uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val notes = snapshot?.documents?.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    Note(
                        id = doc.id,
                        content = data["content"] as? String ?: "",
                        status = data["status"] as? String ?: "delivered",
                        createdAt = (data["createdAt"] as? com.google.firebase.Timestamp)?.toDate()?.time ?: 0L,
                        readAt = (data["readAt"] as? com.google.firebase.Timestamp)?.toDate()?.time
                    )
                } ?: emptyList()

                trySend(notes)
            }

        awaitClose { registration.remove() }
    }

    /**
     * Mark a note as read.
     */
    suspend fun markAsRead(noteId: String) {
        try {
            firestore.collection("notes").document(noteId)
                .update(
                    mapOf(
                        "status" to "read",
                        "readAt" to com.google.firebase.Timestamp.now()
                    )
                )
                .await()
        } catch (e: Exception) {
            // Non-fatal
        }
    }

    /**
     * Count of unread notes for badge display.
     */
    fun observeUnreadCount(): Flow<Int> = callbackFlow {
        val uid = auth.currentUser?.uid ?: run {
            trySend(0)
            close()
            return@callbackFlow
        }

        val registration = firestore.collection("notes")
            .whereEqualTo("recipientId", uid)
            .whereEqualTo("status", "delivered")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(0)
                    return@addSnapshotListener
                }
                trySend(snapshot?.size() ?: 0)
            }

        awaitClose { registration.remove() }
    }
}
