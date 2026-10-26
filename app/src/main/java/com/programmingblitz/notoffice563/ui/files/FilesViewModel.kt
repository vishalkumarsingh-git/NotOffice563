package com.programmingblitz.notoffice563.ui.files

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.programmingblitz.notoffice563.data.local.AppPreferences
import com.programmingblitz.notoffice563.data.local.FileSortOrder
import com.programmingblitz.notoffice563.data.local.FileViewMode
import com.programmingblitz.notoffice563.data.local.RecentFileEntity
import com.programmingblitz.notoffice563.data.repository.DocumentRepository
import com.programmingblitz.notoffice563.domain.model.DocumentCategory
import com.programmingblitz.notoffice563.domain.model.DocumentType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FilesUiState(
    val files: List<RecentFileEntity> = emptyList(),
    val selectedCategory: DocumentCategory = DocumentCategory.ALL,
    val searchQuery: String = "",
    val sortOrder: FileSortOrder = FileSortOrder.DATE_MODIFIED,
    val viewMode: FileViewMode = FileViewMode.LIST,
    val totalCount: Int = 0,
    val isLoading: Boolean = false
)

class FilesViewModel(
    private val repository: DocumentRepository,
    private val preferences: AppPreferences
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow(DocumentCategory.ALL)
    private val _searchQuery = MutableStateFlow("")
    private val _messageEvents = MutableSharedFlow<String>()
    val messageEvents: SharedFlow<String> = _messageEvents.asSharedFlow()

    val uiState: StateFlow<FilesUiState> = combine(
        repository.allRecentFiles,
        _selectedCategory,
        _searchQuery,
        preferences.userPreferences
    ) { allFiles, category, query, userPrefs ->
        // 1. Filter by category
        val categoryFiltered = if (category == DocumentCategory.ALL) {
            allFiles
        } else {
            allFiles.filter { file ->
                val type = runCatching { DocumentType.valueOf(file.fileType) }.getOrDefault(DocumentType.UNKNOWN)
                type.category == category
            }
        }

        // 2. Filter by search query
        val searchFiltered = if (query.isBlank()) {
            categoryFiltered
        } else {
            categoryFiltered.filter { file ->
                file.displayName.contains(query, ignoreCase = true) ||
                        file.extension.contains(query, ignoreCase = true) ||
                        file.fileType.contains(query, ignoreCase = true)
            }
        }

        // 3. Sort
        val sorted = when (userPrefs.sortOrder) {
            FileSortOrder.DATE_MODIFIED -> searchFiltered.sortedByDescending { it.lastModified.coerceAtLeast(it.lastOpened) }
            FileSortOrder.NAME -> searchFiltered.sortedBy { it.displayName.lowercase() }
            FileSortOrder.SIZE -> searchFiltered.sortedByDescending { it.fileSize }
            FileSortOrder.TYPE -> searchFiltered.sortedBy { it.fileType }
        }

        FilesUiState(
            files = sorted,
            selectedCategory = category,
            searchQuery = query,
            sortOrder = userPrefs.sortOrder,
            viewMode = userPrefs.viewMode,
            totalCount = searchFiltered.size,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FilesUiState(isLoading = true)
    )

    fun selectCategory(category: DocumentCategory) {
        _selectedCategory.value = category
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun setSortOrder(order: FileSortOrder) {
        viewModelScope.launch {
            preferences.setSortOrder(order)
        }
    }

    fun toggleViewMode() {
        viewModelScope.launch {
            val current = uiState.value.viewMode
            preferences.setViewMode(if (current == FileViewMode.LIST) FileViewMode.GRID else FileViewMode.LIST)
        }
    }

    fun onDocumentPicked(context: Context, uri: Uri, onNavigateToViewer: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val entity = repository.registerOpenedDocument(context, uri)
                onNavigateToViewer(entity.uri)
            } catch (e: Exception) {
                _messageEvents.emit("Could not open file: ${e.localizedMessage}")
            }
        }
    }

    fun toggleFavorite(file: RecentFileEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(file.uri, file.isFavorite)
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
            _messageEvents.emit("File removed from workspace")
        }
    }

    fun removeRecent(file: RecentFileEntity) {
        viewModelScope.launch {
            repository.removeRecent(file.uri)
            _messageEvents.emit("Removed from recent list")
        }
    }
}
