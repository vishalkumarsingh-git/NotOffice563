package com.programmingblitz.notoffice563.ui.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.programmingblitz.notoffice563.data.auth.AuthManager
import com.programmingblitz.notoffice563.data.auth.AuthResult
import com.programmingblitz.notoffice563.data.local.AppPreferences
import com.programmingblitz.notoffice563.data.local.AppThemeMode
import com.programmingblitz.notoffice563.data.local.FileSortOrder
import com.programmingblitz.notoffice563.data.local.FileViewMode
import com.programmingblitz.notoffice563.data.local.UserPreferences
import com.programmingblitz.notoffice563.domain.model.UserAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ProfileDialogState {
    data class MissingConfig(val message: String) : ProfileDialogState
    data class Error(val message: String) : ProfileDialogState
    object ConfirmSignOut : ProfileDialogState
}

class ProfileViewModel(
    private val authManager: AuthManager,
    private val preferences: AppPreferences
) : ViewModel() {

    val currentUser: StateFlow<UserAccount?> = authManager.currentUser
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val userPreferences: StateFlow<UserPreferences> = preferences.userPreferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _dialogState = MutableStateFlow<ProfileDialogState?>(null)
    val dialogState: StateFlow<ProfileDialogState?> = _dialogState.asStateFlow()

    private val _setupNotice = MutableStateFlow<String?>(
        if (authManager.getWebClientId().isNullOrBlank()) {
            "Google Sign-In is currently optional. Add GOOGLE_WEB_CLIENT_ID to your environment or configure google-services.json to enable cloud account linking. All local document features remain 100% functional."
        } else null
    )
    val setupNotice: StateFlow<String?> = _setupNotice.asStateFlow()

    fun signInWithGoogle(context: Context) {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = authManager.signInWithGoogle(context)) {
                is AuthResult.Success -> {
                    _dialogState.value = null
                    _setupNotice.value = null
                }
                is AuthResult.MissingConfiguration -> {
                    // Non-blocking: update inline setup notice instead of interrupting with a dialog
                    _setupNotice.value = result.message
                    _dialogState.value = null
                }
                is AuthResult.Error -> {
                    _dialogState.value = ProfileDialogState.Error(result.message)
                }
                is AuthResult.Cancelled -> {
                    // Clean cancellation: user dismissed the sheet, no disruptive alert
                    _dialogState.value = null
                }
            }
            _isLoading.value = false
        }
    }

    fun requestSignOutConfirmation() {
        _dialogState.value = ProfileDialogState.ConfirmSignOut
    }

    fun confirmSignOut() {
        viewModelScope.launch {
            _isLoading.value = true
            authManager.signOut()
            _dialogState.value = null
            _isLoading.value = false
        }
    }

    fun dismissDialog() {
        _dialogState.value = null
    }

    fun setTheme(theme: AppThemeMode) {
        viewModelScope.launch {
            preferences.setThemeMode(theme)
        }
    }

    fun setViewMode(mode: FileViewMode) {
        viewModelScope.launch {
            preferences.setViewMode(mode)
        }
    }

    fun setSortOrder(order: FileSortOrder) {
        viewModelScope.launch {
            preferences.setSortOrder(order)
        }
    }

    fun setMaxRecent(max: Int) {
        viewModelScope.launch {
            preferences.setMaxRecentFiles(max)
        }
    }
}
