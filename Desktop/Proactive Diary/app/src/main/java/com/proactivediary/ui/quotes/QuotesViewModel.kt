package com.proactivediary.ui.quotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proactivediary.data.social.ContentModerator
import com.proactivediary.data.social.Quote
import com.proactivediary.data.social.QuotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class QuotesTab { TRENDING, NEW, MY_QUOTES }

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
    private val contentModerator: ContentModerator
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
    }

    private fun loadTrending() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = quotesRepository.getLeaderboard("weekly", 30)
            result.fold(
                onSuccess = { quotes ->
                    _state.value = _state.value.copy(
                        trendingQuotes = quotes,
                        isLoading = false
                    )
                    // Check liked status for visible quotes
                    checkLikedStatus(quotes)
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message
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
    }
}
