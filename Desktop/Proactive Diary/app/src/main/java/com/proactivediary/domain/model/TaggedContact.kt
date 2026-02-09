package com.proactivediary.domain.model

data class TaggedContact(
    val displayName: String,
    val lookupUri: String,
    val email: String? = null,
    val phone: String? = null
)
