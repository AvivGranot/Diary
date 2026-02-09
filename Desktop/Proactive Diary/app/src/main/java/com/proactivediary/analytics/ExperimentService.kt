package com.proactivediary.analytics

import kotlinx.coroutines.flow.StateFlow

/**
 * Abstraction for the A/B testing service.
 *
 * Production implementation: [FirebaseExperimentService]
 * Test implementation: FakeExperimentService (in test source set)
 */
interface ExperimentService {
    val isInitialized: StateFlow<Boolean>
    suspend fun initialize()
    fun getVariant(experiment: Experiment): String
    fun observeVariant(experiment: Experiment): StateFlow<String>
    fun isVariant(experiment: Experiment, variant: String): Boolean
    fun logExposure(experiment: Experiment)
}
