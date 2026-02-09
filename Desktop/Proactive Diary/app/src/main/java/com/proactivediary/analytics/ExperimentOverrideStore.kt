package com.proactivediary.analytics

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SharedPreferences-backed store for debug experiment overrides.
 *
 * In release builds nobody writes to it, so all overrides return null
 * and production behavior is unchanged. In debug builds the
 * DebugExperimentScreen writes overrides here.
 */
@Singleton
class ExperimentOverrideStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("experiment_overrides", Context.MODE_PRIVATE)

    fun getOverride(experiment: Experiment): String? {
        return prefs.getString(experiment.key, null)
    }

    fun setOverride(experiment: Experiment, variant: String?) {
        if (variant == null) {
            prefs.edit().remove(experiment.key).apply()
        } else {
            prefs.edit().putString(experiment.key, variant).apply()
        }
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
