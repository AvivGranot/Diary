package com.proactivediary.domain.model

enum class GoalFrequency(val label: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly");

    companion object {
        fun fromString(value: String?): GoalFrequency {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: DAILY
        }
    }
}
