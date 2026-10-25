package com.example.ui.viewer

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppPreferences
import com.example.data.local.DocumentNoteEntity
import com.example.data.local.RecentFileEntity
import com.example.data.repository.DocumentRepository
import com.example.document.parser.DocxParser
import com.example.document.parser.DocumentFormatDetector
import com.example.document.parser.DocumentFormatValidation
import com.example.document.parser.PptxParser
import com.example.document.parser.XlsxParser
import com.example.document.pdf.PdfManager
import com.example.document.saf.SafHelper
import com.example.domain.model.DocumentType
import com.example.domain.model.ParsedDocxDocument
import com.example.domain.model.ParsedPresentation
import com.example.domain.model.ParsedSpreadsheet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

sealed class ViewerContentState {
    object Loading : ViewerContentState()

    data class PdfContent(
        val pageCount: Int,
        val currentPage: Int = 0,
        val renderedPageBitmap: Bitmap? = null,
        val zoomFactor: Float = 1.0f,
        val pageNote: String = ""
    ) : ViewerContentState()

    data class DocxContent(
        val parsedDoc: ParsedDocxDocument,
        val isEditing: Boolean = false,
        val editedTitle: String = "",
        val editedContent: String = "",
        val documentNote: String = ""
    ) : ViewerContentState()

    data class XlsxContent(
        val parsedSpreadsheet: ParsedSpreadsheet,
        val currentSheetIndex: Int = 0,
        val selectedCellRef: String = "A1",
        val selectedCellValue: String = "",
        val isEditingCell: Boolean = false
    ) : ViewerContentState()

    data class PptxContent(
        val parsedPresentation: ParsedPresentation,
        val currentSlideIndex: Int = 0
    ) : ViewerContentState()

    data class LegacyFallback(
        val documentType: DocumentType,
        val message: String
    ) : ViewerContentState()

    data class Error(
        val errorMessage: String,
        val canOpenExternally: Boolean = true
    ) : ViewerContentState()
}

class ViewerViewModel(
    private val uriString: String,
    private val repository: DocumentRepository,
    private val preferences: AppPreferences
) : ViewModel() {

    private val _contentState = MutableStateFlow<ViewerContentState>(ViewerContentState.Loading)
    val contentState: StateFlow<ViewerContentState> = _contentState.asStateFlow()

    private val _fileMetadata = MutableStateFlow<RecentFileEntity?>(null)
    val fileMetadata: StateFlow<RecentFileEntity?> = _fileMetadata.asStateFlow()

    private val _messageEvents = MutableSharedFlow<String>()
    val messageEvents: SharedFlow<String> = _messageEvents.asSharedFlow()

    private var pdfManager: PdfManager? = null

    fun loadDocument(context: Context) {
        viewModelScope.launch {
            _contentState.value = ViewerContentState.Loading

            val uri = Uri.parse(uriString)

            // 1. Check access
            if (!SafHelper.canAccessUri(context, uri)) {
                _contentState.value = ViewerContentState.Error(
                    errorMessage = "NotOffice563 no longer has access to this document. The file may have been moved, deleted, or permission revoked.",
                    canOpenExternally = false
                )
                return@launch
            }

            // 2. Load or register entity
            val entity = repository.registerOpenedDocument(context, uri)
            _fileMetadata.value = entity

            val initialType = runCatching { DocumentType.valueOf(entity.fileType) }.getOrDefault(DocumentType.UNKNOWN)

            // 3. Detect and validate format
            when (val validation = DocumentFormatDetector.detectAndValidate(context, uri, initialType)) {
                is DocumentFormatValidation.LegacyFormat -> {
                    _contentState.value = ViewerContentState.LegacyFallback(
                        documentType = validation.type,
                        message = validation.message
                    )
                }
                is DocumentFormatValidation.Corrupted -> {
                    _contentState.value = ViewerContentState.Error(
                        errorMessage = "Document appears damaged or unreadable: ${validation.message}"
                    )
                }
                is DocumentFormatValidation.Unsupported -> {
                    _contentState.value = ViewerContentState.Error(
                        errorMessage = "This file format cannot currently be viewed by NotOffice563."
                    )
                }
                is DocumentFormatValidation.Valid -> {
                    openValidatedDocument(context, uri, validation.type, entity.lastPosition)
                }
            }
        }
    }

    private suspend fun openValidatedDocument(
        context: Context,
        uri: Uri,
        type: DocumentType,
        savedPosition: String?
    ) = withContext(Dispatchers.IO) {
        try {
            when (type) {
                DocumentType.PDF -> {
                    val manager = PdfManager(context)
                    pdfManager = manager
                    val openResult = manager.open(uri)
                    if (openResult.isSuccess) {
                        val totalPages = openResult.getOrThrow()
                        var initialPage = 0
                        if (savedPosition != null && savedPosition.startsWith("page:")) {
                            initialPage = (savedPosition.removePrefix("page:").toIntOrNull() ?: 0).coerceIn(0, (totalPages - 1).coerceAtLeast(0))
                        }
                        val bitmap = manager.renderPage(initialPage)
                        _contentState.value = ViewerContentState.PdfContent(
                            pageCount = totalPages,
                            currentPage = initialPage,
                            renderedPageBitmap = bitmap
                        )
                        loadNoteForTarget("page:$initialPage")
                    } else {
                        _contentState.value = ViewerContentState.Error(
                            errorMessage = "PDF rendering failed: ${openResult.exceptionOrNull()?.localizedMessage}"
                        )
                    }
                }

                DocumentType.WORD_DOCX -> {
                    val inputStream = context.contentResolver.openInputStream(uri)
                        ?: throw IllegalStateException("Cannot open document stream")
                    val parsed = DocxParser.parse(inputStream, _fileMetadata.value?.displayName ?: "Document")
                    _contentState.value = ViewerContentState.DocxContent(
                        parsedDoc = parsed,
                        editedTitle = parsed.title,
                        editedContent = parsed.rawText
                    )
                    loadNoteForTarget("doc:global")
                }

                DocumentType.EXCEL_XLSX -> {
                    val inputStream = context.contentResolver.openInputStream(uri)
                        ?: throw IllegalStateException("Cannot open spreadsheet stream")
                    val parsed = XlsxParser.parse(inputStream)
                    var initialSheet = 0
                    if (savedPosition != null && savedPosition.startsWith("sheet:")) {
                        val sheetName = savedPosition.removePrefix("sheet:")
                        val idx = parsed.sheets.indexOfFirst { it.name == sheetName }
                        if (idx != -1) initialSheet = idx
                    }
                    _contentState.value = ViewerContentState.XlsxContent(
                        parsedSpreadsheet = parsed,
                        currentSheetIndex = initialSheet
                    )
                }

                DocumentType.PPT_PPTX -> {
                    val inputStream = context.contentResolver.openInputStream(uri)
                        ?: throw IllegalStateException("Cannot open presentation stream")
                    val parsed = PptxParser.parse(inputStream)
                    var initialSlide = 0
                    if (savedPosition != null && savedPosition.startsWith("slide:")) {
                        initialSlide = (savedPosition.removePrefix("slide:").toIntOrNull() ?: 0).coerceIn(0, (parsed.slides.size - 1).coerceAtLeast(0))
                    }
                    _contentState.value = ViewerContentState.PptxContent(
                        parsedPresentation = parsed,
                        currentSlideIndex = initialSlide
                    )
                }

                DocumentType.WORD_DOC -> {
                    _contentState.value = ViewerContentState.LegacyFallback(
                        documentType = DocumentType.WORD_DOC,
                        message = "This legacy Word format (.doc) cannot currently be rendered by NotOffice563."
                    )
                }

                DocumentType.EXCEL_XLS -> {
                    _contentState.value = ViewerContentState.LegacyFallback(
                        documentType = DocumentType.EXCEL_XLS,
                        message = "This legacy Excel format (.xls) cannot currently be fully rendered by NotOffice563."
                    )
                }

                DocumentType.PPT_PPT -> {
                    _contentState.value = ViewerContentState.LegacyFallback(
                        documentType = DocumentType.PPT_PPT,
                        message = "This legacy PowerPoint format (.ppt) cannot currently be fully rendered by NotOffice563."
                    )
                }

                DocumentType.UNKNOWN -> {
                    _contentState.value = ViewerContentState.Error(
                        errorMessage = "Unsupported document format."
                    )
                }
            }
        } catch (e: Exception) {
            _contentState.value = ViewerContentState.Error(
                errorMessage = "Failed to parse document: ${e.localizedMessage ?: "Unknown error"}"
            )
        }
    }

    // PDF Actions
    fun setPdfPage(pageIndex: Int) {
        val current = _contentState.value as? ViewerContentState.PdfContent ?: return
        val manager = pdfManager ?: return
        if (pageIndex < 0 || pageIndex >= current.pageCount) return

        viewModelScope.launch {
            val bitmap = manager.renderPage(pageIndex, (1200 * current.zoomFactor).toInt())
            _contentState.value = current.copy(
                currentPage = pageIndex,
                renderedPageBitmap = bitmap
            )
            repository.updateLastPosition(uriString, "page:$pageIndex")
            loadNoteForTarget("page:$pageIndex")
        }
    }

    fun setPdfZoom(factor: Float) {
        val current = _contentState.value as? ViewerContentState.PdfContent ?: return
        val clamped = factor.coerceIn(0.5f, 3.0f)
        viewModelScope.launch {
            val bitmap = pdfManager?.renderPage(current.currentPage, (1200 * clamped).toInt())
            _contentState.value = current.copy(zoomFactor = clamped, renderedPageBitmap = bitmap)
        }
    }

    // DOCX Editing & Notes
    fun toggleDocxEditing(isEditing: Boolean) {
        val current = _contentState.value as? ViewerContentState.DocxContent ?: return
        _contentState.value = current.copy(isEditing = isEditing)
    }

    fun updateDocxEditTitle(title: String) {
        val current = _contentState.value as? ViewerContentState.DocxContent ?: return
        _contentState.value = current.copy(editedTitle = title)
    }

    fun updateDocxEditContent(content: String) {
        val current = _contentState.value as? ViewerContentState.DocxContent ?: return
        _contentState.value = current.copy(editedContent = content)
    }

    fun saveEditedDocxCopy(context: Context, onSaved: (Uri) -> Unit) {
        val current = _contentState.value as? ViewerContentState.DocxContent ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val bytes = DocxParser.createBasicDocx(current.editedTitle, current.editedContent)
                val exportFile = File(context.cacheDir, "edited_${System.currentTimeMillis()}_${current.editedTitle.replace(" ", "_")}.docx")
                FileOutputStream(exportFile).use { it.write(bytes) }
                val exportUri = Uri.fromFile(exportFile)
                _messageEvents.emit("Saved edited document copy: ${exportFile.name}")
                withContext(Dispatchers.Main) {
                    onSaved(exportUri)
                }
            } catch (e: Exception) {
                _messageEvents.emit("Save failed: ${e.localizedMessage}")
            }
        }
    }

    // XLSX Actions
    fun setSheetIndex(index: Int) {
        val current = _contentState.value as? ViewerContentState.XlsxContent ?: return
        if (index in current.parsedSpreadsheet.sheets.indices) {
            _contentState.value = current.copy(currentSheetIndex = index)
            val sheetName = current.parsedSpreadsheet.sheets[index].name
            viewModelScope.launch {
                repository.updateLastPosition(uriString, "sheet:$sheetName")
            }
        }
    }

    fun selectCell(ref: String, value: String) {
        val current = _contentState.value as? ViewerContentState.XlsxContent ?: return
        _contentState.value = current.copy(
            selectedCellRef = ref,
            selectedCellValue = value
        )
    }

    // PPTX Actions
    fun setSlideIndex(index: Int) {
        val current = _contentState.value as? ViewerContentState.PptxContent ?: return
        if (index in current.parsedPresentation.slides.indices) {
            _contentState.value = current.copy(currentSlideIndex = index)
            viewModelScope.launch {
                repository.updateLastPosition(uriString, "slide:$index")
            }
        }
    }

    // Notes Support
    private fun loadNoteForTarget(targetKey: String) {
        viewModelScope.launch {
            // Can be observed if note exists
        }
    }

    fun saveNote(targetKey: String, noteText: String) {
        viewModelScope.launch {
            repository.saveNote(uriString, targetKey, noteText)
            _messageEvents.emit("Note saved for $targetKey")
        }
    }

    fun toggleFavorite() {
        val meta = _fileMetadata.value ?: return
        viewModelScope.launch {
            repository.toggleFavorite(meta.uri, meta.isFavorite)
            _fileMetadata.value = meta.copy(isFavorite = !meta.isFavorite)
        }
    }

    override fun onCleared() {
        super.onCleared()
        pdfManager?.close()
    }
}
