package com.proactivediary.ui.debug

import androidx.lifecycle.ViewModel
import com.proactivediary.analytics.ExperimentOverrideStore
import com.proactivediary.analytics.ExperimentService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DebugExperimentViewModel @Inject constructor(
    val experimentService: ExperimentService,
    val overrideStore: ExperimentOverrideStore
) : ViewModel()
