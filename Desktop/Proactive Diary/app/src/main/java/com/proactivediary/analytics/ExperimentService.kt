package com.proactivediary.analytics

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central A/B testing service backed by Firebase Remote Config.
 *
 * - Fetches experiment variants from Remote Config on init
 * - Caches assignments locally so the user always sees the same variant
 * - Exposes variant values as reactive StateFlows for Compose
 * - Logs experiment assignments as Firebase user properties for analytics segmentation
 */
@Singleton
class ExperimentService @Inject constructor() {

    private val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
    private val analytics: FirebaseAnalytics = Firebase.analytics

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    // Cached variant assignments (experiment key → variant string)
    private val assignments = mutableMapOf<String, String>()

    // Reactive flows for each experiment — UI can collectAsState()
    private val _flows = mutableMapOf<String, MutableStateFlow<String>>()

    init {
        // Set defaults from Experiment enum so the app works before fetch completes
        val defaults = Experiment.entries.associate { it.key to it.defaultVariant }
        remoteConfig.setDefaultsAsync(defaults)

        // Apply defaults immediately
        Experiment.entries.forEach { exp ->
            assignments[exp.key] = exp.defaultVariant
            _flows[exp.key] = MutableStateFlow(exp.defaultVariant)
        }

        // Configure fetch settings (12-hour cache in production; shorter in debug)
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 43200 // 12 hours
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
    }

    /**
     * Fetch and activate Remote Config values. Call once at app startup.
     * Non-blocking — uses cached values until fetch completes.
     */
    suspend fun initialize() {
        try {
            remoteConfig.fetchAndActivate().await()
        } catch (_: Exception) {
            // Network failure is fine — we'll use cached/default values
        }

        // Read activated values and update assignments
        Experiment.entries.forEach { exp ->
            val value = remoteConfig.getString(exp.key)
            val variant = if (value.isNotBlank() && value in exp.variants) value else exp.defaultVariant
            assignments[exp.key] = variant
            _flows[exp.key]?.value = variant
        }

        // Log all assignments as user properties for analytics segmentation
        logAssignments()

        _isInitialized.value = true
    }

    /**
     * Get the current variant for an experiment.
     * Returns the default variant if Remote Config hasn't fetched yet.
     */
    fun getVariant(experiment: Experiment): String {
        return assignments[experiment.key] ?: experiment.defaultVariant
    }

    /**
     * Observe the variant for an experiment as a StateFlow.
     * Use this in ViewModels: `experimentService.observeVariant(Experiment.X).collectAsState()`
     */
    fun observeVariant(experiment: Experiment): StateFlow<String> {
        return _flows.getOrPut(experiment.key) {
            MutableStateFlow(getVariant(experiment))
        }.asStateFlow()
    }

    /**
     * Check if a specific variant is active for an experiment.
     */
    fun isVariant(experiment: Experiment, variant: String): Boolean {
        return getVariant(experiment) == variant
    }

    /**
     * Log all experiment assignments as Firebase user properties.
     * This allows segmenting analytics data by experiment variant.
     */
    private fun logAssignments() {
        assignments.forEach { (key, variant) ->
            analytics.setUserProperty(key, variant)
        }
    }

    /**
     * Log an experiment exposure event. Call this when the user actually
     * *sees* the variant (not just when assigned). This prevents
     * diluting results with users who were assigned but never exposed.
     */
    fun logExposure(experiment: Experiment) {
        val variant = getVariant(experiment)
        analytics.logEvent("experiment_exposure", android.os.Bundle().apply {
            putString("experiment_key", experiment.key)
            putString("variant", variant)
        })
    }
}
