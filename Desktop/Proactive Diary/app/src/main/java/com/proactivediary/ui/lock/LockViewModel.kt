package com.proactivediary.ui.lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proactivediary.data.db.dao.PreferenceDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LockViewModel @Inject constructor(
    private val preferenceDao: PreferenceDao
) : ViewModel() {

    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked

    init {
        viewModelScope.launch {
            val lockEnabled = preferenceDao.get("app_lock_enabled")?.value == "true"
            if (lockEnabled) {
                _isLocked.value = true
            }
        }
    }

    fun onAppResumed() {
        viewModelScope.launch {
            val lockEnabled = preferenceDao.get("app_lock_enabled")?.value == "true"
            if (lockEnabled) {
                _isLocked.value = true
            }
        }
    }

    fun unlock() {
        _isLocked.value = false
    }
}
