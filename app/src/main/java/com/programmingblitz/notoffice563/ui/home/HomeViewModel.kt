package com.programmingblitz.notoffice563.ui.home

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.programmingblitz.notoffice563.data.local.RecentFileEntity
import com.programmingblitz.notoffice563.data.repository.DocumentRepository
import com.programmingblitz.notoffice563.domain.model.DocumentCategory
import com.programmingblitz.notoffice563.domain.model.DocumentType
import com.programmingblitz.notoffice563.util.DeviceStorageInfo
import com.programmingblitz.notoffice563.util.StorageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val recentFiles: List<RecentFileEntity> = emptyList(),
    val favoriteFiles: List<RecentFileEntity> = emptyList(),
    val documentCount: Int = 0,
    val spreadsheetCount: Int = 0,
    val presentationCount: Int = 0,
    val pdfCount: Int = 0,
    val storageInfo: DeviceStorageInfo = DeviceStorageInfo(0, 0, 0, 0f),
    val isLoading: Boolean = false
)

class HomeViewModel(
    private val repository: DocumentRepository
) : ViewModel() {

    private val _storageInfo = MutableStateFlow(DeviceStorageInfo(0, 0, 0, 0f))
    private val _messageEvents = MutableSharedFlow<String>()
    val messageEvents: SharedFlow<String> = _messageEvents.asSharedFlow()

    init {
        refreshStorage()
    }

    val uiState: StateFlow<HomeUiState> = combine(
        repository.allRecentFiles,
        repository.favoriteFiles,
        _storageInfo
    ) { allFiles, favorites, storage ->
        var docCount = 0
        var sheetCount = 0
        var pptCount = 0
        var pdfCount = 0

        for (file in allFiles) {
            val type = runCatching { DocumentType.valueOf(file.fileType) }.getOrDefault(DocumentType.UNKNOWN)
            when (type.category) {
                DocumentCategory.DOCUMENTS -> docCount++
                DocumentCategory.SPREADSHEETS -> sheetCount++
                DocumentCategory.PRESENTATIONS -> pptCount++
                DocumentCategory.PDFS -> pdfCount++
                DocumentCategory.ALL -> {}
            }
        }

        HomeUiState(
            recentFiles = allFiles.take(8),
            favoriteFiles = favorites,
            documentCount = docCount,
            spreadsheetCount = sheetCount,
            presentationCount = pptCount,
            pdfCount = pdfCount,
            storageInfo = storage,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    fun refreshStorage() {
        viewModelScope.launch(Dispatchers.IO) {
            val info = StorageUtils.getDeviceStorageInfo()
            _storageInfo.value = info
        }
    }

    fun onDocumentPicked(context: Context, uri: Uri, onNavigateToViewer: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val entity = repository.registerOpenedDocument(context, uri)
                onNavigateToViewer(entity.uri)
            } catch (e: Exception) {
                _messageEvents.emit("Could not open selected document: ${e.localizedMessage}")
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

    fun createDocument(
        context: Context,
        format: com.programmingblitz.notoffice563.document.generator.DocumentFormatCategory,
        templateId: String,
        title: String,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = com.programmingblitz.notoffice563.document.generator.DocumentCreationManager.createAndSaveDocument(
                context = context,
                repository = repository,
                format = format,
                templateId = templateId,
                customTitle = title
            )
            result.onSuccess { (uri, _) ->
                onSuccess(uri.toString())
            }.onFailure { err ->
                _messageEvents.emit("Failed to create document: ${err.localizedMessage}")
            }
        }
    }

    fun removeRecent(file: RecentFileEntity) {
        viewModelScope.launch {
            repository.removeRecent(file.uri)
            _messageEvents.emit("Removed from recent list")
        }
    }
}
