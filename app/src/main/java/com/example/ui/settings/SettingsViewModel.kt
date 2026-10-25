package com.example.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppPreferences
import com.example.data.local.AppThemeMode
import com.example.data.local.FileSortOrder
import com.example.data.local.FileViewMode
import com.example.data.local.UserPreferences
import com.example.data.repository.DocumentRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferences: AppPreferences,
    private val repository: DocumentRepository
) : ViewModel() {

    private val _messageEvents = MutableSharedFlow<String>()
    val messageEvents: SharedFlow<String> = _messageEvents.asSharedFlow()

    val userPreferences: StateFlow<UserPreferences> = preferences.userPreferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    fun setTheme(theme: AppThemeMode) {
        viewModelScope.launch {
            preferences.setThemeMode(theme)
        }
    }

    fun setSortOrder(order: FileSortOrder) {
        viewModelScope.launch {
            preferences.setSortOrder(order)
        }
    }

    fun setViewMode(mode: FileViewMode) {
        viewModelScope.launch {
            preferences.setViewMode(mode)
        }
    }

    fun setMaxRecent(max: Int) {
        viewModelScope.launch {
            preferences.setMaxRecentFiles(max)
        }
    }

    fun setRememberPosition(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setRememberPosition(enabled)
        }
    }

    fun setConfirmDelete(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setConfirmDelete(enabled)
        }
    }

    fun clearRecentFiles() {
        viewModelScope.launch {
            repository.clearRecent()
            _messageEvents.emit("Recent files cleared successfully")
        }
    }
}
