package com.example.ui.recent

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppPreferences
import com.example.data.local.RecentFileEntity
import com.example.data.repository.DocumentRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RecentUiState(
    val files: List<RecentFileEntity> = emptyList(),
    val maxLimit: Int = 25,
    val isLoading: Boolean = false
)

class RecentViewModel(
    private val repository: DocumentRepository,
    private val preferences: AppPreferences
) : ViewModel() {

    private val _messageEvents = MutableSharedFlow<String>()
    val messageEvents: SharedFlow<String> = _messageEvents.asSharedFlow()

    val uiState: StateFlow<RecentUiState> = combine(
        repository.allRecentFiles,
        preferences.userPreferences
    ) { allFiles, prefs ->
        RecentUiState(
            files = allFiles.take(prefs.maxRecentFiles),
            maxLimit = prefs.maxRecentFiles,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RecentUiState(isLoading = true)
    )

    fun toggleFavorite(file: RecentFileEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(file.uri, file.isFavorite)
        }
    }

    fun removeRecent(file: RecentFileEntity) {
        viewModelScope.launch {
            repository.removeRecent(file.uri)
            _messageEvents.emit("Removed '${file.displayName}' from recent")
        }
    }

    fun clearAllRecent() {
        viewModelScope.launch {
            repository.clearRecent()
            _messageEvents.emit("Recent files cleared")
        }
    }

    fun renameFile(context: Context, file: RecentFileEntity, newName: String) {
        viewModelScope.launch {
            repository.renameDocument(context, file.uri, newName)
            _messageEvents.emit("File renamed to $newName")
        }
    }

    fun deleteFile(context: Context, file: RecentFileEntity) {
        viewModelScope.launch {
            repository.deleteDocument(context, file.uri)
            _messageEvents.emit("File deleted")
        }
    }
}
