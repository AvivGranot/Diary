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

data class Quote(
    val id: String,
    val authorId: String,
    val authorName: String,
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
    private val functions: FirebaseFunctions,
    private val auth: FirebaseAuth
) {

    suspend fun submitQuote(content: String): Result<String> {
        return try {
            val data = hashMapOf("content" to content)
            val result = functions.getHttpsCallable("submitQuote")
                .call(data)
                .await()

            @Suppress("UNCHECKED_CAST")
            val resultData = result.data as? Map<String, Any>
            val quoteId = resultData?.get("quoteId") as? String ?: ""
            Result.success(quoteId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleLike(quoteId: String): Result<Boolean> {
        return try {
            val data = hashMapOf("quoteId" to quoteId)
            val result = functions.getHttpsCallable("toggleLike")
                .call(data)
                .await()

            @Suppress("UNCHECKED_CAST")
            val resultData = result.data as? Map<String, Any>
            val liked = resultData?.get("liked") as? Boolean ?: false
            Result.success(liked)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addComment(quoteId: String, content: String): Result<Unit> {
        return try {
            val data = hashMapOf(
                "quoteId" to quoteId,
                "content" to content
            )
            functions.getHttpsCallable("addComment")
                .call(data)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLeaderboard(
        period: String = "weekly",
        limit: Int = 20
    ): Result<List<Quote>> {
        return try {
            val data = hashMapOf(
                "period" to period,
                "limit" to limit
            )
            val result = functions.getHttpsCallable("getLeaderboard")
                .call(data)
                .await()

            @Suppress("UNCHECKED_CAST")
            val resultData = result.data as? Map<String, Any>
            val quotesData = resultData?.get("quotes") as? List<Map<String, Any>> ?: emptyList()

            val quotes = quotesData.map { q ->
                Quote(
                    id = q["id"] as? String ?: "",
                    authorId = q["authorId"] as? String ?: "",
                    authorName = q["authorName"] as? String ?: "Anonymous",
                    content = q["content"] as? String ?: "",
                    likeCount = (q["likeCount"] as? Number)?.toInt() ?: 0,
                    commentCount = (q["commentCount"] as? Number)?.toInt() ?: 0,
                    createdAt = (q["createdAt"] as? Number)?.toLong() ?: 0L
                )
            }

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
                    val data = doc.data ?: return@mapNotNull null
                    Quote(
                        id = doc.id,
                        authorId = data["authorId"] as? String ?: "",
                        authorName = data["authorName"] as? String ?: "Anonymous",
                        content = data["content"] as? String ?: "",
                        likeCount = (data["likeCount"] as? Number)?.toInt() ?: 0,
                        commentCount = (data["commentCount"] as? Number)?.toInt() ?: 0,
                        createdAt = (data["createdAt"] as? com.google.firebase.Timestamp)?.toDate()?.time ?: 0L
                    )
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
                    val data = doc.data ?: return@mapNotNull null
                    Quote(
                        id = doc.id,
                        authorId = data["authorId"] as? String ?: "",
                        authorName = data["authorName"] as? String ?: "Anonymous",
                        content = data["content"] as? String ?: "",
                        likeCount = (data["likeCount"] as? Number)?.toInt() ?: 0,
                        commentCount = (data["commentCount"] as? Number)?.toInt() ?: 0,
                        createdAt = (data["createdAt"] as? com.google.firebase.Timestamp)?.toDate()?.time ?: 0L
                    )
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
}
