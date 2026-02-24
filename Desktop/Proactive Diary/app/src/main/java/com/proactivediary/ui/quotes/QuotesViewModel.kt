package com.proactivediary.ui.quotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proactivediary.analytics.AnalyticsService
import com.proactivediary.data.social.ContentModerator
import com.proactivediary.data.social.Quote
import com.proactivediary.data.social.QuotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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

            val updateQuoteLikeCount = { quotes: List<Quote> ->
                quotes.map { q ->
                    if (q.id == quoteId) {
                        q.copy(likeCount = q.likeCount + if (currentLiked) -1 else 1)
                    } else q
                }
            }

            _state.value = _state.value.copy(
                likedQuoteIds = updatedIds,
                trendingQuotes = updateQuoteLikeCount(_state.value.trendingQuotes),
                newQuotes = updateQuoteLikeCount(_state.value.newQuotes),
                myQuotes = updateQuoteLikeCount(_state.value.myQuotes)
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

        _state.value = _state.value.copy(isSubmitting = true, submitError = null)

        viewModelScope.launch {
            val result = quotesRepository.submitQuote(content)
            result.fold(
                onSuccess = {
                    analyticsService.logQuoteSubmitted(content.trim().split("\\s+".toRegex()).size)
                    _state.value = _state.value.copy(
                        isSubmitting = false,
                        composeSheetVisible = false,
                        composeContent = "",
                        composeWordCount = 0
                    )
                    // Refresh trending
                    loadTrending()
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isSubmitting = false,
                        submitError = e.message ?: "Failed to publish quote"
                    )
                }
            )
        }
    }

    fun refresh() {
        loadTrending()
    }

    companion object {
        const val MAX_QUOTE_WORDS = 25

        /** Sample quotes shown when Firestore returns empty or fails */
        val SAMPLE_QUOTES = listOf(
            Quote(id = "sample_1", authorId = "s1", authorName = "Maya Cohen", content = "The best time to start was yesterday. The second best time is now.", likeCount = 124, commentCount = 18, createdAt = System.currentTimeMillis() - 3_600_000),
            Quote(id = "sample_2", authorId = "s2", authorName = "Sarah Levi", content = "Be the energy you want to attract.", likeCount = 89, commentCount = 12, createdAt = System.currentTimeMillis() - 7_200_000),
            Quote(id = "sample_3", authorId = "s3", authorName = "Dan Amir", content = "Stay curious. Stay humble. Stay hungry.", likeCount = 67, commentCount = 5, createdAt = System.currentTimeMillis() - 10_800_000),
            Quote(id = "sample_4", authorId = "s4", authorName = "Lena Rubin", content = "Your vibe attracts your tribe.", likeCount = 52, commentCount = 8, createdAt = System.currentTimeMillis() - 14_400_000),
            Quote(id = "sample_5", authorId = "s5", authorName = "Ron Shapira", content = "Do it with passion or not at all.", likeCount = 41, commentCount = 3, createdAt = System.currentTimeMillis() - 18_000_000),
            Quote(id = "sample_6", authorId = "s6", authorName = "Noa Katz", content = "Breathe and let go.", likeCount = 38, commentCount = 2, createdAt = System.currentTimeMillis() - 21_600_000),
            Quote(id = "sample_7", authorId = "s7", authorName = "Tom Barak", content = "Trust the process.", likeCount = 33, commentCount = 1, createdAt = System.currentTimeMillis() - 25_200_000),
        )
    }
}
