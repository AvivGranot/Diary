package com.proactivediary.ui.quotes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proactivediary.data.social.ContentModerator
import com.proactivediary.data.social.Quote
import com.proactivediary.data.social.QuoteComment
import com.proactivediary.data.social.QuotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuoteDetailState(
    val quote: Quote? = null,
    val comments: List<QuoteComment> = emptyList(),
    val isLiked: Boolean = false,
    val commentText: String = "",
    val isSendingComment: Boolean = false,
    val commentError: String? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class QuoteDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val quotesRepository: QuotesRepository,
    private val contentModerator: ContentModerator
) : ViewModel() {

    private val quoteId: String = savedStateHandle["quoteId"] ?: ""

    private val _state = MutableStateFlow(QuoteDetailState())
    val state: StateFlow<QuoteDetailState> = _state.asStateFlow()

    init {
        loadQuote()
        observeComments()
    }

    private fun loadQuote() {
        viewModelScope.launch {
            // Load from leaderboard or new quotes
            val result = quotesRepository.getLeaderboard("all_time", 50)
            result.onSuccess { quotes ->
                val quote = quotes.find { it.id == quoteId }
                val liked = quotesRepository.hasLiked(quoteId)
                _state.value = _state.value.copy(
                    quote = quote,
                    isLiked = liked,
                    isLoading = false
                )
            }
            result.onFailure {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    private fun observeComments() {
        viewModelScope.launch {
            quotesRepository.observeComments(quoteId).collect { comments ->
                _state.value = _state.value.copy(comments = comments)
            }
        }
    }

    fun toggleLike() {
        val currentLiked = _state.value.isLiked
        val currentQuote = _state.value.quote ?: return

        // Optimistic update
        _state.value = _state.value.copy(
            isLiked = !currentLiked,
            quote = currentQuote.copy(
                likeCount = currentQuote.likeCount + if (currentLiked) -1 else 1
            )
        )

        viewModelScope.launch {
            quotesRepository.toggleLike(quoteId)
        }
    }

    fun updateCommentText(text: String) {
        if (text.length <= 100) {
            _state.value = _state.value.copy(commentText = text, commentError = null)
        }
    }

    fun submitComment() {
        val text = _state.value.commentText
        if (text.isBlank()) return

        val modResult = contentModerator.check(text)
        if (!modResult.isAllowed) {
            _state.value = _state.value.copy(commentError = modResult.reason)
            return
        }

        _state.value = _state.value.copy(isSendingComment = true, commentError = null)

        viewModelScope.launch {
            val result = quotesRepository.addComment(quoteId, text)
            result.fold(
                onSuccess = {
                    _state.value = _state.value.copy(
                        isSendingComment = false,
                        commentText = "",
                        quote = _state.value.quote?.copy(
                            commentCount = (_state.value.quote?.commentCount ?: 0) + 1
                        )
                    )
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isSendingComment = false,
                        commentError = e.message ?: "Failed to post comment"
                    )
                }
            )
        }
    }
}
