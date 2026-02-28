package com.proactivediary.ui.quotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proactivediary.analytics.AnalyticsService
import com.proactivediary.data.social.ContentModerator
import com.proactivediary.data.social.Quote
import com.proactivediary.data.social.QuotesRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

enum class QuotesTab { TRENDING, NEW, FOLLOWING }

data class QuotesState(
    val selectedTab: QuotesTab = QuotesTab.TRENDING,
    val trendingQuotes: List<Quote> = emptyList(),
    val newQuotes: List<Quote> = emptyList(),
    val myQuotes: List<Quote> = emptyList(),
    val likedQuoteIds: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val composeSheetVisible: Boolean = false,
    val composeContent: String = "",
    val composeWordCount: Int = 0,
    val isSubmitting: Boolean = false,
    val submitError: String? = null
)

@HiltViewModel
class QuotesViewModel @Inject constructor(
    private val quotesRepository: QuotesRepository,
    private val contentModerator: ContentModerator,
    private val analyticsService: AnalyticsService
) : ViewModel() {

    private val _state = MutableStateFlow(QuotesState())
    val state: StateFlow<QuotesState> = _state.asStateFlow()

    init {
        loadTrending()
        observeNewQuotes()
        observeMyQuotes()
    }

    fun selectTab(tab: QuotesTab) {
        _state.value = _state.value.copy(selectedTab = tab)
        analyticsService.logLeaderboardPeriodChanged(tab.name.lowercase())
    }

    private fun loadTrending() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = quotesRepository.getLeaderboard("weekly", 30)
            result.fold(
                onSuccess = { quotes ->
                    val finalQuotes = quotes.ifEmpty { SAMPLE_QUOTES }
                    _state.value = _state.value.copy(
                        trendingQuotes = finalQuotes,
                        isLoading = false
                    )
                    if (quotes.isNotEmpty()) checkLikedStatus(quotes)
                },
                onFailure = { _ ->
                    _state.value = _state.value.copy(
                        trendingQuotes = SAMPLE_QUOTES,
                        isLoading = false
                    )
                }
            )
        }
    }

    private fun observeNewQuotes() {
        viewModelScope.launch {
            quotesRepository.observeNewQuotes(50).collect { quotes ->
                _state.value = _state.value.copy(newQuotes = quotes)
            }
        }
    }

    private fun observeMyQuotes() {
        viewModelScope.launch {
            quotesRepository.observeMyQuotes().collect { quotes ->
                _state.value = _state.value.copy(myQuotes = quotes)
            }
        }
    }

    private fun checkLikedStatus(quotes: List<Quote>) {
        viewModelScope.launch {
            val likedIds = mutableSetOf<String>()
            quotes.forEach { quote ->
                if (quotesRepository.hasLiked(quote.id)) {
                    likedIds.add(quote.id)
                }
            }
            _state.value = _state.value.copy(likedQuoteIds = likedIds)
        }
    }

    fun toggleLike(quoteId: String) {
        viewModelScope.launch {
            val currentLiked = _state.value.likedQuoteIds.contains(quoteId)

            // Track like/unlike
            if (currentLiked) analyticsService.logQuoteUnliked(quoteId)
            else analyticsService.logQuoteLiked(quoteId)

            // Optimistic UI update
            val updatedIds = _state.value.likedQuoteIds.toMutableSet()
            if (currentLiked) updatedIds.remove(quoteId) else updatedIds.add(quoteId)

            val delta = if (currentLiked) -1 else 1
            val updateIfPresent = { quotes: List<Quote> ->
                if (quotes.none { it.id == quoteId }) quotes
                else quotes.map { q ->
                    if (q.id == quoteId) q.copy(likeCount = q.likeCount + delta) else q
                }
            }

            _state.value = _state.value.copy(
                likedQuoteIds = updatedIds,
                trendingQuotes = updateIfPresent(_state.value.trendingQuotes),
                newQuotes = updateIfPresent(_state.value.newQuotes),
                myQuotes = updateIfPresent(_state.value.myQuotes)
            )

            // Server call
            quotesRepository.toggleLike(quoteId)
        }
    }

    fun showComposeSheet() {
        analyticsService.logQuoteComposeStart()
        _state.value = _state.value.copy(
            composeSheetVisible = true,
            composeContent = "",
            composeWordCount = 0,
            submitError = null
        )
    }

    fun hideComposeSheet() {
        _state.value = _state.value.copy(composeSheetVisible = false)
    }

    fun updateComposeContent(text: String) {
        val words = if (text.isBlank()) 0 else text.trim().split("\\s+".toRegex()).size
        if (words <= MAX_QUOTE_WORDS) {
            _state.value = _state.value.copy(
                composeContent = text,
                composeWordCount = words,
                submitError = null
            )
        }
    }

    fun submitQuote() {
        val content = _state.value.composeContent
        if (content.isBlank()) return

        val modResult = contentModerator.check(content)
        if (!modResult.isAllowed) {
            _state.value = _state.value.copy(submitError = modResult.reason)
            return
        }

        // Optimistic: dismiss sheet + add quote immediately
        val user = FirebaseAuth.getInstance().currentUser
        val tempId = "pending_${System.currentTimeMillis()}"
        val newQuote = Quote(
            id = tempId,
            authorId = user?.uid ?: "",
            authorName = user?.displayName ?: "You",
            authorPhotoUrl = user?.photoUrl?.toString(),
            content = content.trim(),
            likeCount = 0,
            commentCount = 0,
            createdAt = System.currentTimeMillis()
        )
        _state.value = _state.value.copy(
            isSubmitting = false,
            composeSheetVisible = false,
            composeContent = "",
            composeWordCount = 0,
            trendingQuotes = listOf(newQuote) + _state.value.trendingQuotes,
            newQuotes = listOf(newQuote) + _state.value.newQuotes
        )

        // Background sync with timeout
        viewModelScope.launch {
            try {
                withTimeout(10_000L) {
                    quotesRepository.submitQuote(content)
                }
                analyticsService.logQuoteSubmitted(content.trim().split("\\s+".toRegex()).size)
                loadTrending() // refresh with real server data
            } catch (_: Exception) {
                // Quote is already shown locally — silent fail
                // Real data will appear on next refresh
            }
        }
    }

    fun refresh() {
        loadTrending()
    }

    companion object {
        const val MAX_QUOTE_WORDS = 25

        /** Sample quotes shown when Firestore returns empty or fails */
        val SAMPLE_QUOTES = listOf(
            Quote(id = "sample_1", authorId = "s1", authorName = "Mei Lin", authorPhotoUrl = "file:///android_asset/avatars/maya.jpg", content = "The best time to start was yesterday. The second best time is now.", likeCount = 124, commentCount = 18, createdAt = System.currentTimeMillis() - 3_600_000),
            Quote(id = "sample_2", authorId = "s2", authorName = "Sarah Levi", authorPhotoUrl = "file:///android_asset/avatars/sarah.jpg", content = "Be the energy you want to attract.", likeCount = 89, commentCount = 12, createdAt = System.currentTimeMillis() - 7_200_000),
            Quote(id = "sample_3", authorId = "s3", authorName = "Dan Amir", authorPhotoUrl = "file:///android_asset/avatars/dan.jpg", content = "Stay curious. Stay humble. Stay hungry.", likeCount = 67, commentCount = 5, createdAt = System.currentTimeMillis() - 10_800_000),
            Quote(id = "sample_4", authorId = "s4", authorName = "Lena Rubin", authorPhotoUrl = "file:///android_asset/avatars/lena.jpg", content = "Your vibe attracts your tribe.", likeCount = 52, commentCount = 8, createdAt = System.currentTimeMillis() - 14_400_000),
            Quote(id = "sample_5", authorId = "s5", authorName = "Ron Shapira", authorPhotoUrl = "file:///android_asset/avatars/ron.jpg", content = "Do it with passion or not at all.", likeCount = 41, commentCount = 3, createdAt = System.currentTimeMillis() - 18_000_000),
            Quote(id = "sample_6", authorId = "s6", authorName = "Noa Katz", authorPhotoUrl = "file:///android_asset/avatars/noa.jpg", content = "Breathe and let go.", likeCount = 38, commentCount = 2, createdAt = System.currentTimeMillis() - 21_600_000),
            Quote(id = "sample_7", authorId = "s7", authorName = "Tom Barak", authorPhotoUrl = "file:///android_asset/avatars/tom.jpg", content = "Trust the process.", likeCount = 33, commentCount = 1, createdAt = System.currentTimeMillis() - 25_200_000),
            Quote(id = "sample_8", authorId = "s8", authorName = "Ella Friedman", authorPhotoUrl = "file:///android_asset/avatars/ella.jpg", content = "Growth begins where comfort ends.", likeCount = 28, commentCount = 2, createdAt = System.currentTimeMillis() - 28_800_000),
            Quote(id = "sample_9", authorId = "s9", authorName = "Amit Peretz", authorPhotoUrl = "file:///android_asset/avatars/amit.jpg", content = "Small steps still move you forward.", likeCount = 22, commentCount = 0, createdAt = System.currentTimeMillis() - 32_400_000),
            Quote(id = "sample_10", authorId = "s10", authorName = "Lily Chen", authorPhotoUrl = "file:///android_asset/avatars/yael.jpg", content = "You are braver than you believe.", likeCount = 19, commentCount = 1, createdAt = System.currentTimeMillis() - 36_000_000),
        )
    }
}
