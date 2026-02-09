package com.proactivediary.analytics

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

/**
 * Production A/B testing service backed by Firebase Remote Config.
 *
 * - Fetches experiment variants from Remote Config on init
 * - Checks [ExperimentOverrideStore] first (debug overrides)
 * - Caches assignments locally so the user always sees the same variant
 * - Exposes variant values as reactive StateFlows for Compose
 * - Logs experiment assignments as Firebase user properties for analytics segmentation
 */
class FirebaseExperimentService(
    private val remoteConfig: FirebaseRemoteConfig,
    private val analytics: FirebaseAnalytics,
    private val overrideStore: ExperimentOverrideStore
) : ExperimentService {

    private val _isInitialized = MutableStateFlow(false)
    override val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    // Cached variant assignments (experiment key -> variant string)
    private val assignments = mutableMapOf<String, String>()

    // Reactive flows for each experiment
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

        // Configure fetch settings (12-hour cache in production)
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 43200
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
    }

    override suspend fun initialize() {
        try {
            remoteConfig.fetchAndActivate().await()
        } catch (_: Exception) {
            // Network failure is fine — use cached/default values
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

    override fun getVariant(experiment: Experiment): String {
        // Debug overrides take precedence
        val override = overrideStore.getOverride(experiment)
        if (override != null && override in experiment.variants) return override

        return assignments[experiment.key] ?: experiment.defaultVariant
    }

    override fun observeVariant(experiment: Experiment): StateFlow<String> {
        return _flows.getOrPut(experiment.key) {
            MutableStateFlow(getVariant(experiment))
        }.asStateFlow()
    }

    override fun isVariant(experiment: Experiment, variant: String): Boolean {
        return getVariant(experiment) == variant
    }

    private fun logAssignments() {
        assignments.forEach { (key, variant) ->
            analytics.setUserProperty(key, variant)
        }
    }

    override fun logExposure(experiment: Experiment) {
        val variant = getVariant(experiment)
        analytics.logEvent("experiment_exposure", android.os.Bundle().apply {
            putString("experiment_key", experiment.key)
            putString("variant", variant)
        })
    }
}
