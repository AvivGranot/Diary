package com.proactivediary.analytics

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory test double for [ExperimentService].
 *
 * - Use [setVariant] to configure variant assignments before assertions
 * - Check [exposuresLogged] to verify exposure tracking
 * - Call [reset] between tests to clear state
 */
class FakeExperimentService : ExperimentService {

    private val _isInitialized = MutableStateFlow(false)
    override val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    /** Override map: experiment -> variant. When empty, defaults are used. */
    val variantOverrides = mutableMapOf<Experiment, String>()

    /** Records every experiment that had [logExposure] called. */
    val exposuresLogged = mutableListOf<Experiment>()

    private val _flows = mutableMapOf<String, MutableStateFlow<String>>()

    override suspend fun initialize() {
        _isInitialized.value = true
    }

    override fun getVariant(experiment: Experiment): String {
        return variantOverrides[experiment] ?: experiment.defaultVariant
    }

    override fun observeVariant(experiment: Experiment): StateFlow<String> {
        return _flows.getOrPut(experiment.key) {
            MutableStateFlow(getVariant(experiment))
        }.asStateFlow()
    }

    override fun isVariant(experiment: Experiment, variant: String): Boolean {
        return getVariant(experiment) == variant
    }

    override fun logExposure(experiment: Experiment) {
        exposuresLogged.add(experiment)
    }

    /** Set a specific variant for an experiment. */
    fun setVariant(experiment: Experiment, variant: String) {
        variantOverrides[experiment] = variant
        _flows[experiment.key]?.value = variant
    }

    /** Clear all overrides and logged exposures. */
    fun reset() {
        variantOverrides.clear()
        exposuresLogged.clear()
        _flows.clear()
        _isInitialized.value = false
    }
}
