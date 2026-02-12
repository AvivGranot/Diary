package com.proactivediary.ui.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.proactivediary.data.db.entities.TemplateEntity
import com.proactivediary.data.repository.TemplateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TemplateItem(
    val id: String,
    val title: String,
    val description: String,
    val prompts: List<String>,
    val category: String,
    val isBuiltIn: Boolean
)

data class TemplatePickerUiState(
    val templates: List<TemplateItem> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class TemplatePickerViewModel @Inject constructor(
    private val templateRepository: TemplateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TemplatePickerUiState())
    val uiState: StateFlow<TemplatePickerUiState> = _uiState.asStateFlow()

    private val gson = Gson()

    init {
        loadTemplates()
    }

    private fun loadTemplates() {
        viewModelScope.launch {
            templateRepository.getAllTemplates().collect { entities ->
                val items = entities.map { it.toItem() }
                val categories = items.map { it.category }.distinct().sorted()
                _uiState.value = _uiState.value.copy(
                    templates = items,
                    categories = categories,
                    isLoading = false
                )
            }
        }
    }

    fun selectCategory(category: String?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    private fun TemplateEntity.toItem(): TemplateItem {
        val promptList: List<String> = try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(prompts, type) ?: emptyList()
        } catch (_: Exception) { emptyList() }

        return TemplateItem(
            id = id,
            title = title,
            description = description,
            prompts = promptList,
            category = category,
            isBuiltIn = isBuiltIn
        )
    }
}
