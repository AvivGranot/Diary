package com.proactivediary.domain.model

data class DiscoverEntry(
    val id: String,
    val author: String,
    val authorYears: String,
    val title: String,
    val excerpt: String,
    val source: String,
    val category: String,
    val era: String
)
