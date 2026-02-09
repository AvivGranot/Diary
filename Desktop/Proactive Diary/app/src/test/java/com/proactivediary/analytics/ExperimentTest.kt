package com.proactivediary.analytics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Invariant tests for the [Experiment] enum.
 * These catch misconfigurations before they ship.
 */
class ExperimentTest {

    @Test
    fun `every experiment default variant is in its variants list`() {
        Experiment.entries.forEach { exp ->
            assertTrue(
                "${exp.name}: defaultVariant '${exp.defaultVariant}' not in variants ${exp.variants}",
                exp.defaultVariant in exp.variants
            )
        }
    }

    @Test
    fun `every experiment has a unique Remote Config key`() {
        val keys = Experiment.entries.map { it.key }
        assertEquals(
            "Duplicate Remote Config keys found: ${keys.groupBy { it }.filter { it.value.size > 1 }.keys}",
            keys.size,
            keys.distinct().size
        )
    }

    @Test
    fun `every experiment has at least 2 variants`() {
        Experiment.entries.forEach { exp ->
            assertTrue(
                "${exp.name} has only ${exp.variants.size} variant(s) — need at least 2 for an experiment",
                exp.variants.size >= 2
            )
        }
    }

    @Test
    fun `first variant is always control`() {
        Experiment.entries.forEach { exp ->
            assertEquals(
                "${exp.name}: first variant should be 'control' but was '${exp.variants.first()}'",
                "control",
                exp.variants.first()
            )
        }
    }

    @Test
    fun `all Remote Config keys start with exp_ prefix`() {
        Experiment.entries.forEach { exp ->
            assertTrue(
                "${exp.name}: key '${exp.key}' should start with 'exp_'",
                exp.key.startsWith("exp_")
            )
        }
    }

    @Test
    fun `default variant is always control`() {
        Experiment.entries.forEach { exp ->
            assertEquals(
                "${exp.name}: defaultVariant should be 'control' but was '${exp.defaultVariant}'",
                "control",
                exp.defaultVariant
            )
        }
    }
}
