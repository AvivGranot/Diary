package com.proactivediary.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proactivediary.data.social.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SocialSplashViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _userCount = MutableStateFlow(0L)
    val userCount: StateFlow<Long> = _userCount.asStateFlow()

    init {
        viewModelScope.launch {
            val (users, _) = userProfileRepository.getGlobalCounters()
            _userCount.value = users
        }
    }
}
