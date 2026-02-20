package com.proactivediary.ui.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

// ── Data models ─────────────────────────────────────────────────────────

enum class ActivityType {
    QUOTE_LIKED,
    NOTE_RECEIVED,
    QUOTE_COMMENTED,
    LEADERBOARD_RANK,
    QUOTE_FEATURED
}

enum class ActivityIconType {
    HEART,
    ENVELOPE,
    CHAT,
    TROPHY,
    STAR
}

data class ActivityItem(
    val id: String,
    val type: ActivityType,
    val title: String,
    val subtitle: String,
    val timestamp: Long,
    val iconType: ActivityIconType,
    val isRead: Boolean = false,
    val referenceId: String? = null // quote ID, note ID, etc.
)

enum class ActivitySection { NEW, THIS_WEEK, EARLIER }

data class ActivityState(
    val items: List<ActivityItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
) {
    /** Items from today. */
    val newItems: List<ActivityItem>
        get() {
            val todayStart = todayStartMillis()
            return items.filter { it.timestamp >= todayStart }
        }

    /** Items from this week but not today. */
    val thisWeekItems: List<ActivityItem>
        get() {
            val todayStart = todayStartMillis()
            val weekStart = weekStartMillis()
            return items.filter { it.timestamp in weekStart until todayStart }
        }

    /** Items older than this week. */
    val earlierItems: List<ActivityItem>
        get() {
            val weekStart = weekStartMillis()
            return items.filter { it.timestamp < weekStart }
        }

    val unreadCount: Int
        get() = items.count { !it.isRead }

    val isEmpty: Boolean
        get() = items.isEmpty()
}

private fun todayStartMillis(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

private fun weekStartMillis(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    cal.add(Calendar.DAY_OF_YEAR, -7)
    return cal.timeInMillis
}

// ── ViewModel ───────────────────────────────────────────────────────────

@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow(ActivityState())
    val state: StateFlow<ActivityState> = _state.asStateFlow()

    /** Unread badge count, suitable for bottom nav. */
    val unreadCount: StateFlow<Int> = callbackFlow {
        val uid = auth.currentUser?.uid ?: run {
            trySend(0)
            close()
            return@callbackFlow
        }

        val registration = firestore.collection("users").document(uid)
            .collection("activity")
            .whereEqualTo("read", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(0)
                    return@addSnapshotListener
                }
                trySend(snapshot?.size() ?: 0)
            }

        awaitClose { registration.remove() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    init {
        loadActivity()
    }

    fun refresh() {
        loadActivity()
    }

    fun markAllRead() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val unread = _state.value.items.filter { !it.isRead }
            unread.forEach { item ->
                try {
                    firestore.collection("users").document(uid)
                        .collection("activity").document(item.id)
                        .update("read", true)
                } catch (_: Exception) { /* non-fatal */ }
            }
            // Optimistic UI update
            _state.value = _state.value.copy(
                items = _state.value.items.map { it.copy(isRead = true) }
            )
        }
    }

    private fun loadActivity() {
        val uid = auth.currentUser?.uid ?: run {
            _state.value = ActivityState(isLoading = false)
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            firestore.collection("users").document(uid)
                .collection("activity")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(100)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = error.message
                        )
                        return@addSnapshotListener
                    }

                    val items = snapshot?.documents?.mapNotNull { doc ->
                        val data = doc.data ?: return@mapNotNull null
                        val typeStr = data["type"] as? String ?: return@mapNotNull null
                        val type = parseActivityType(typeStr) ?: return@mapNotNull null
                        val ts = (data["timestamp"] as? Timestamp)?.toDate()?.time ?: 0L

                        ActivityItem(
                            id = doc.id,
                            type = type,
                            title = data["title"] as? String ?: "",
                            subtitle = data["subtitle"] as? String ?: "",
                            timestamp = ts,
                            iconType = iconForType(type),
                            isRead = data["read"] as? Boolean ?: false,
                            referenceId = data["referenceId"] as? String
                        )
                    } ?: emptyList()

                    _state.value = ActivityState(
                        items = items,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }

    companion object {
        fun parseActivityType(raw: String): ActivityType? = when (raw) {
            "quote_liked" -> ActivityType.QUOTE_LIKED
            "note_received" -> ActivityType.NOTE_RECEIVED
            "quote_commented" -> ActivityType.QUOTE_COMMENTED
            "leaderboard_rank" -> ActivityType.LEADERBOARD_RANK
            "quote_featured" -> ActivityType.QUOTE_FEATURED
            else -> null
        }

        fun iconForType(type: ActivityType): ActivityIconType = when (type) {
            ActivityType.QUOTE_LIKED -> ActivityIconType.HEART
            ActivityType.NOTE_RECEIVED -> ActivityIconType.ENVELOPE
            ActivityType.QUOTE_COMMENTED -> ActivityIconType.CHAT
            ActivityType.LEADERBOARD_RANK -> ActivityIconType.TROPHY
            ActivityType.QUOTE_FEATURED -> ActivityIconType.STAR
        }

        /**
         * Formats a timestamp into a relative string: "1m", "3h", "2d", "1w".
         */
        fun relativeTime(timestampMs: Long): String {
            val now = System.currentTimeMillis()
            val diffMs = now - timestampMs
            if (diffMs < 0) return "now"

            val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMs)
            val hours = TimeUnit.MILLISECONDS.toHours(diffMs)
            val days = TimeUnit.MILLISECONDS.toDays(diffMs)

            return when {
                minutes < 1 -> "now"
                minutes < 60 -> "${minutes}m"
                hours < 24 -> "${hours}h"
                days < 7 -> "${days}d"
                days < 30 -> "${days / 7}w"
                else -> "${days / 30}mo"
            }
        }
    }
}
