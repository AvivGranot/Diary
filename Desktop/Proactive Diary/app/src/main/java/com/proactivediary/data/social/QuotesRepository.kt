package com.proactivediary.data.social

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class Quote(
    val id: String,
    val authorId: String,
    val authorName: String,
    val authorPhotoUrl: String? = null,
    val content: String,
    val likeCount: Int,
    val commentCount: Int,
    val createdAt: Long,
    val isLikedByMe: Boolean = false
)

data class QuoteComment(
    val id: String,
    val authorId: String,
    val authorName: String,
    val content: String,
    val createdAt: Long
)

@Singleton
class QuotesRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    /**
     * Submit a quote directly to Firestore.
     * Firestore rules enforce: authorId == auth.uid.
     */
    suspend fun submitQuote(content: String): Result<String> {
        val user = auth.currentUser ?: return Result.failure(Exception("Not signed in"))
        return try {
            val quoteData = hashMapOf(
                "authorId" to user.uid,
                "authorName" to (user.displayName ?: "Anonymous"),
                "authorPhotoUrl" to user.photoUrl?.toString(),
                "content" to content.trim(),
                "likeCount" to 0,
                "commentCount" to 0,
                "createdAt" to FieldValue.serverTimestamp(),
                "reported" to false
            )

            val docRef = firestore.collection("quotes")
                .add(quoteData)
                .await()

            // Increment global quote counter (best-effort)
            try {
                firestore.collection("counters").document("global")
                    .update("quoteCount", FieldValue.increment(1))
                    .await()
            } catch (_: Exception) {
                // Counter doc may not exist yet — create it
                firestore.collection("counters").document("global")
                    .set(hashMapOf("quoteCount" to 1, "userCount" to 0), com.google.firebase.firestore.SetOptions.merge())
                    .await()
            }

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Toggle like on a quote — direct Firestore write.
     * Rules enforce: likes/{userId} can be created/deleted by that userId.
     */
    suspend fun toggleLike(quoteId: String): Result<Boolean> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Not signed in"))
        return try {
            val likeRef = firestore.collection("quotes")
                .document(quoteId)
                .collection("likes")
                .document(uid)

            val likeDoc = likeRef.get().await()

            if (likeDoc.exists()) {
                // Unlike
                likeRef.delete().await()
                firestore.collection("quotes").document(quoteId)
                    .update("likeCount", FieldValue.increment(-1)).await()
                Result.success(false)
            } else {
                // Like
                likeRef.set(hashMapOf("createdAt" to FieldValue.serverTimestamp())).await()
                firestore.collection("quotes").document(quoteId)
                    .update("likeCount", FieldValue.increment(1)).await()
                Result.success(true)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Add a comment to a quote — direct Firestore write.
     * Rules enforce: comments/{commentId} authorId == auth.uid.
     */
    suspend fun addComment(quoteId: String, content: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("Not signed in"))
        return try {
            val commentData = hashMapOf(
                "authorId" to user.uid,
                "authorName" to (user.displayName ?: "Anonymous"),
                "content" to content.trim(),
                "createdAt" to FieldValue.serverTimestamp()
            )

            firestore.collection("quotes")
                .document(quoteId)
                .collection("comments")
                .add(commentData)
                .await()

            // Increment comment count
            firestore.collection("quotes").document(quoteId)
                .update("commentCount", FieldValue.increment(1)).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get leaderboard — direct Firestore query instead of Cloud Function.
     */
    suspend fun getLeaderboard(
        period: String = "weekly",
        limit: Int = 20
    ): Result<List<Quote>> {
        return try {
            var query = firestore.collection("quotes")
                .whereEqualTo("reported", false)

            if (period == "weekly" || period == "daily") {
                val cutoff = java.util.Calendar.getInstance().apply {
                    if (period == "weekly") add(java.util.Calendar.DAY_OF_YEAR, -7)
                    else add(java.util.Calendar.DAY_OF_YEAR, -1)
                }.time
                val cutoffTs = com.google.firebase.Timestamp(cutoff)

                // Fetch by recency, then sort by likes in memory
                val snapshot = query
                    .whereGreaterThanOrEqualTo("createdAt", cutoffTs)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit((limit * 3).toLong())
                    .get()
                    .await()

                val quotes = snapshot.documents.mapNotNull { doc ->
                    docToQuote(doc)
                }.sortedByDescending { it.likeCount }
                    .take(limit)

                return Result.success(quotes)
            }

            // all_time: sort by likes directly
            val snapshot = query
                .orderBy("likeCount", Query.Direction.DESCENDING)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val quotes = snapshot.documents.mapNotNull { doc -> docToQuote(doc) }
            Result.success(quotes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Real-time feed of newest quotes.
     */
    fun observeNewQuotes(limit: Int = 50): Flow<List<Quote>> = callbackFlow {
        val registration = firestore.collection("quotes")
            .whereEqualTo("reported", false)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val quotes = snapshot?.documents?.mapNotNull { doc ->
                    docToQuote(doc)
                } ?: emptyList()

                trySend(quotes)
            }

        awaitClose { registration.remove() }
    }

    /**
     * Observe my own quotes.
     */
    fun observeMyQuotes(): Flow<List<Quote>> = callbackFlow {
        val uid = auth.currentUser?.uid ?: run {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = firestore.collection("quotes")
            .whereEqualTo("authorId", uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val quotes = snapshot?.documents?.mapNotNull { doc ->
                    docToQuote(doc)
                } ?: emptyList()

                trySend(quotes)
            }

        awaitClose { registration.remove() }
    }

    /**
     * Load comments for a specific quote.
     */
    fun observeComments(quoteId: String): Flow<List<QuoteComment>> = callbackFlow {
        val registration = firestore.collection("quotes")
            .document(quoteId)
            .collection("comments")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val comments = snapshot?.documents?.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    QuoteComment(
                        id = doc.id,
                        authorId = data["authorId"] as? String ?: "",
                        authorName = data["authorName"] as? String ?: "Anonymous",
                        content = data["content"] as? String ?: "",
                        createdAt = (data["createdAt"] as? com.google.firebase.Timestamp)?.toDate()?.time ?: 0L
                    )
                } ?: emptyList()

                trySend(comments)
            }

        awaitClose { registration.remove() }
    }

    /**
     * Check if current user has liked a specific quote.
     */
    suspend fun hasLiked(quoteId: String): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            val doc = firestore.collection("quotes")
                .document(quoteId)
                .collection("likes")
                .document(uid)
                .get()
                .await()
            doc.exists()
        } catch (e: Exception) {
            false
        }
    }

    private fun docToQuote(doc: com.google.firebase.firestore.DocumentSnapshot): Quote? {
        val data = doc.data ?: return null
        return Quote(
            id = doc.id,
            authorId = data["authorId"] as? String ?: "",
            authorName = data["authorName"] as? String ?: "Anonymous",
            authorPhotoUrl = data["authorPhotoUrl"] as? String,
            content = data["content"] as? String ?: "",
            likeCount = (data["likeCount"] as? Number)?.toInt() ?: 0,
            commentCount = (data["commentCount"] as? Number)?.toInt() ?: 0,
            createdAt = (data["createdAt"] as? com.google.firebase.Timestamp)?.toDate()?.time ?: 0L
        )
    }
}
