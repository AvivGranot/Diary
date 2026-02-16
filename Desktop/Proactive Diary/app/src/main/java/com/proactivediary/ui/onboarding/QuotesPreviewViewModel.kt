package com.proactivediary.ui.onboarding

import androidx.lifecycle.ViewModel
import com.proactivediary.data.social.QuotesRepository
import com.proactivediary.data.social.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class QuotesPreviewViewModel @Inject constructor(
    val quotesRepository: QuotesRepository,
    val userProfileRepository: UserProfileRepository
) : ViewModel()
