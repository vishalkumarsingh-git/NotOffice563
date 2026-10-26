package com.programmingblitz.notoffice563.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.programmingblitz.notoffice563.data.local.RecentFileEntity
import com.programmingblitz.notoffice563.data.repository.DocumentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val results: List<RecentFileEntity> = emptyList(),
    val totalWorkspaceFiles: Int = 0,
    val isSearching: Boolean = false
)

class SearchViewModel(
    private val repository: DocumentRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")

    val uiState: StateFlow<SearchUiState> = combine(
        _query,
        repository.allRecentFiles
    ) { query, allFiles ->
        val results = if (query.isBlank()) {
            emptyList()
        } else {
            val q = query.trim().lowercase()
            allFiles.filter { file ->
                file.displayName.lowercase().contains(q) ||
                        file.extension.lowercase().contains(q) ||
                        file.fileType.lowercase().contains(q)
            }
        }

        SearchUiState(
            query = query,
            results = results,
            totalWorkspaceFiles = allFiles.size,
            isSearching = query.isNotBlank()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SearchUiState()
    )

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun toggleFavorite(file: RecentFileEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(file.uri, file.isFavorite)
        }
    }
}
