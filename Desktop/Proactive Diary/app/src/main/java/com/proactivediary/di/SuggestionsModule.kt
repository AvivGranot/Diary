package com.proactivediary.di

import com.proactivediary.domain.suggestions.LocalSuggestionEngine
import com.proactivediary.domain.suggestions.SuggestionEngine
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SuggestionsModule {

    @Binds
    @Singleton
    abstract fun bindSuggestionEngine(impl: LocalSuggestionEngine): SuggestionEngine
}
