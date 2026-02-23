package com.proactivediary.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proactivediary.data.db.dao.PreferenceDao
import com.proactivediary.data.db.entities.EntryEntity
import com.proactivediary.data.repository.EntryRepository
import com.proactivediary.data.repository.StreakRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val streakRepository: StreakRepository,
    private val preferenceDao: PreferenceDao,
    private val entryRepository: EntryRepository
) : ViewModel() {

    private val _currentStreak = MutableStateFlow(0)
    val currentStreak: StateFlow<Int> = _currentStreak

    private val _streakEnabled = MutableStateFlow(true)
    val streakEnabled: StateFlow<Boolean> = _streakEnabled

    init {
        refreshStreak()
        loadStreakPref()
    }

    fun refreshStreak() {
        viewModelScope.launch {
            _currentStreak.value = streakRepository.calculateWritingStreak()
        }
    }

    private fun loadStreakPref() {
        viewModelScope.launch {
            val pref = preferenceDao.get("streak_enabled")
            _streakEnabled.value = pref?.value != "false"
        }
    }

    fun saveQuickThought(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val now = System.currentTimeMillis()
                val wordCount = text.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }.size
                val entry = EntryEntity(
                    id = UUID.randomUUID().toString(),
                    title = "",
                    content = text,
                    contentPlain = text,
                    wordCount = wordCount,
                    createdAt = now,
                    updatedAt = now
                )
                entryRepository.insert(entry)
            }
        }
    }
}
