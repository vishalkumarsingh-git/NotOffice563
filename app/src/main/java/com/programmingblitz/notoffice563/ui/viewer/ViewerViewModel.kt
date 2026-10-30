package com.programmingblitz.notoffice563.ui.viewer

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.programmingblitz.notoffice563.data.local.AppPreferences
import com.programmingblitz.notoffice563.data.local.DocumentNoteEntity
import com.programmingblitz.notoffice563.data.local.RecentFileEntity
import com.programmingblitz.notoffice563.data.repository.DocumentRepository
import com.programmingblitz.notoffice563.document.parser.DocxParser
import com.programmingblitz.notoffice563.document.parser.DocumentFormatDetector
import com.programmingblitz.notoffice563.document.parser.DocumentFormatValidation
import com.programmingblitz.notoffice563.document.parser.PptxParser
import com.programmingblitz.notoffice563.document.parser.XlsxParser
import com.programmingblitz.notoffice563.document.pdf.PdfManager
import com.programmingblitz.notoffice563.document.saf.SafHelper
import com.programmingblitz.notoffice563.domain.model.DocxAlignment
import com.programmingblitz.notoffice563.domain.model.DocxParagraph
import com.programmingblitz.notoffice563.domain.model.DocxRun
import com.programmingblitz.notoffice563.domain.model.DocumentType
import com.programmingblitz.notoffice563.domain.model.ParsedDocxDocument
import com.programmingblitz.notoffice563.domain.model.ParsedPresentation
import com.programmingblitz.notoffice563.domain.model.ParsedSpreadsheet
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

enum class SaveStatus {
    SAVED,
    UNSAVED_CHANGES,
    SAVING
}

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
        val editedParagraphs: List<DocxParagraph> = emptyList(),
        val documentNote: String = "",
        val saveStatus: SaveStatus = SaveStatus.SAVED,
        val hasUnsavedChanges: Boolean = false
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
                        editedContent = parsed.rawText,
                        editedParagraphs = parsed.paragraphs,
                        saveStatus = SaveStatus.SAVED,
                        hasUnsavedChanges = false
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

                DocumentType.WORD_DOC, DocumentType.RTF -> {
                    val inputStream = context.contentResolver.openInputStream(uri)
                        ?: throw IllegalStateException("Cannot open legacy document stream")
                    val parsed = com.programmingblitz.notoffice563.document.parser.DocParser.parse(
                        inputStream,
                        _fileMetadata.value?.displayName ?: "Document"
                    )
                    _contentState.value = ViewerContentState.DocxContent(
                        parsedDoc = parsed,
                        editedTitle = parsed.title,
                        editedContent = parsed.rawText,
                        editedParagraphs = parsed.paragraphs,
                        saveStatus = SaveStatus.SAVED,
                        hasUnsavedChanges = false
                    )
                    loadNoteForTarget("doc:global")
                }

                DocumentType.TXT -> {
                    val inputStream = context.contentResolver.openInputStream(uri)
                        ?: throw IllegalStateException("Cannot open text stream")
                    val parsed = com.programmingblitz.notoffice563.document.parser.DocParser.parsePlainText(
                        inputStream.readBytes(),
                        _fileMetadata.value?.displayName ?: "Document"
                    )
                    _contentState.value = ViewerContentState.DocxContent(
                        parsedDoc = parsed,
                        editedTitle = parsed.title,
                        editedContent = parsed.rawText,
                        editedParagraphs = parsed.paragraphs,
                        saveStatus = SaveStatus.SAVED,
                        hasUnsavedChanges = false
                    )
                    loadNoteForTarget("doc:global")
                }

                DocumentType.EXCEL_XLS -> {
                    val inputStream = context.contentResolver.openInputStream(uri)
                        ?: throw IllegalStateException("Cannot open legacy spreadsheet stream")
                    val parsed = com.programmingblitz.notoffice563.document.parser.XlsParser.parse(inputStream)
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

                DocumentType.CSV -> {
                    val inputStream = context.contentResolver.openInputStream(uri)
                        ?: throw IllegalStateException("Cannot open CSV stream")
                    val parsed = com.programmingblitz.notoffice563.document.parser.XlsParser.parseCsv(inputStream, ",")
                    _contentState.value = ViewerContentState.XlsxContent(
                        parsedSpreadsheet = parsed,
                        currentSheetIndex = 0
                    )
                }

                DocumentType.TSV -> {
                    val inputStream = context.contentResolver.openInputStream(uri)
                        ?: throw IllegalStateException("Cannot open TSV stream")
                    val parsed = com.programmingblitz.notoffice563.document.parser.XlsParser.parseCsv(inputStream, "\t")
                    _contentState.value = ViewerContentState.XlsxContent(
                        parsedSpreadsheet = parsed,
                        currentSheetIndex = 0
                    )
                }

                DocumentType.PPT_PPT -> {
                    val inputStream = context.contentResolver.openInputStream(uri)
                        ?: throw IllegalStateException("Cannot open legacy presentation stream")
                    val parsed = com.programmingblitz.notoffice563.document.parser.PptParser.parse(inputStream)
                    var initialSlide = 0
                    if (savedPosition != null && savedPosition.startsWith("slide:")) {
                        initialSlide = (savedPosition.removePrefix("slide:").toIntOrNull() ?: 0).coerceIn(0, (parsed.slides.size - 1).coerceAtLeast(0))
                    }
                    _contentState.value = ViewerContentState.PptxContent(
                        parsedPresentation = parsed,
                        currentSlideIndex = initialSlide
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
        _contentState.value = current.copy(
            editedTitle = title,
            hasUnsavedChanges = true,
            saveStatus = SaveStatus.UNSAVED_CHANGES
        )
    }

    fun updateDocxEditContent(content: String) {
        val current = _contentState.value as? ViewerContentState.DocxContent ?: return
        _contentState.value = current.copy(
            editedContent = content,
            hasUnsavedChanges = true,
            saveStatus = SaveStatus.UNSAVED_CHANGES
        )
    }

    fun updateDocxParagraphs(paragraphs: List<com.programmingblitz.notoffice563.domain.model.DocxParagraph>, rawText: String) {
        val current = _contentState.value as? ViewerContentState.DocxContent ?: return
        _contentState.value = current.copy(
            editedParagraphs = paragraphs,
            editedContent = rawText,
            hasUnsavedChanges = true,
            saveStatus = SaveStatus.UNSAVED_CHANGES
        )
    }

    fun saveOriginalDocx(context: Context) {
        val current = _contentState.value as? ViewerContentState.DocxContent ?: return
        _contentState.value = current.copy(saveStatus = SaveStatus.SAVING)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val uri = Uri.parse(uriString)
                val paragraphs = if (current.editedParagraphs.isNotEmpty()) {
                    current.editedParagraphs
                } else {
                    current.editedContent.split("\n\n", "\n")
                        .filter { it.isNotBlank() }
                        .map { com.programmingblitz.notoffice563.domain.model.DocxParagraph(text = it) }
                }

                val bytes = DocxParser.createRichDocx(current.editedTitle, paragraphs)

                val outputStream = context.contentResolver.openOutputStream(uri, "rwt")
                    ?: context.contentResolver.openOutputStream(uri, "wt")
                    ?: throw IllegalStateException("Cannot open output stream for original file")

                outputStream.use { stream ->
                    stream.write(bytes)
                    stream.flush()
                }

                val reParsed = context.contentResolver.openInputStream(uri)?.use { stream ->
                    DocxParser.parse(stream, current.editedTitle)
                } ?: current.parsedDoc

                val entity = repository.registerOpenedDocument(context, uri)
                _fileMetadata.value = entity

                _contentState.value = current.copy(
                    parsedDoc = reParsed,
                    editedTitle = reParsed.title,
                    editedContent = reParsed.rawText,
                    editedParagraphs = reParsed.paragraphs,
                    saveStatus = SaveStatus.SAVED,
                    hasUnsavedChanges = false
                )

                _messageEvents.emit("Saved changes to original document")
            } catch (e: Exception) {
                _contentState.value = current.copy(saveStatus = SaveStatus.UNSAVED_CHANGES)
                _messageEvents.emit("Save failed: ${e.localizedMessage}")
            }
        }
    }

    fun saveEditedDocxCopy(context: Context, onSaved: (Uri) -> Unit) {
        val current = _contentState.value as? ViewerContentState.DocxContent ?: return
        _contentState.value = current.copy(saveStatus = SaveStatus.SAVING)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val paragraphs = if (current.editedParagraphs.isNotEmpty()) {
                    current.editedParagraphs
                } else {
                    current.editedContent.split("\n\n", "\n")
                        .filter { it.isNotBlank() }
                        .map { com.programmingblitz.notoffice563.domain.model.DocxParagraph(text = it) }
                }

                val bytes = DocxParser.createRichDocx(current.editedTitle, paragraphs)

                val docDir = context.getExternalFilesDir(null) ?: context.filesDir
                val sanitizedTitle = current.editedTitle.trim().replace("[^a-zA-Z0-9_ -]".toRegex(), "_").ifBlank { "Copy" }
                val exportFile = File(docDir, "${sanitizedTitle}_${System.currentTimeMillis()}.docx")

                FileOutputStream(exportFile).use { it.write(bytes) }

                val exportUri = Uri.fromFile(exportFile)
                val newEntity = repository.registerOpenedDocument(context, exportUri)

                _contentState.value = current.copy(
                    saveStatus = SaveStatus.SAVED,
                    hasUnsavedChanges = false
                )

                _messageEvents.emit("Saved copy to ${exportFile.name}")
                withContext(Dispatchers.Main) {
                    onSaved(exportUri)
                }
            } catch (e: Exception) {
                _contentState.value = current.copy(saveStatus = SaveStatus.UNSAVED_CHANGES)
                _messageEvents.emit("Save copy failed: ${e.localizedMessage}")
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

    fun updateSelectedCellValue(newValue: String) {
        val current = _contentState.value as? ViewerContentState.XlsxContent ?: return
        val sheetIndex = current.currentSheetIndex
        val sheets = current.parsedSpreadsheet.sheets.toMutableList()
        val sheet = sheets.getOrNull(sheetIndex) ?: return

        val ref = current.selectedCellRef
        if (ref.isEmpty()) return

        val colLetters = ref.filter { it.isLetter() }
        val rowDigits = ref.filter { it.isDigit() }
        val colIndex = columnLetterToIndex(colLetters)
        val rowIndex = (rowDigits.toIntOrNull() ?: 1) - 1

        if (rowIndex < 0 || colIndex < 0) return

        val rows = sheet.rows.map { it.toMutableList() }.toMutableList()
        while (rows.size <= rowIndex) {
            val emptyRow = MutableList(sheet.maxColumns) { "" }
            rows.add(emptyRow)
        }
        val targetRow = rows[rowIndex]
        while (targetRow.size <= colIndex) {
            targetRow.add("")
        }
        targetRow[colIndex] = newValue

        val updatedSheet = sheet.copy(
            rows = rows,
            maxColumns = maxOf(sheet.maxColumns, colIndex + 1),
            maxRows = maxOf(sheet.maxRows, rows.size)
        )
        sheets[sheetIndex] = updatedSheet

        _contentState.value = current.copy(
            parsedSpreadsheet = current.parsedSpreadsheet.copy(sheets = sheets),
            selectedCellValue = newValue
        )
    }

    fun saveOriginalXlsx(context: Context) {
        val current = _contentState.value as? ViewerContentState.XlsxContent ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val uri = Uri.parse(uriString)
                val bytes = com.programmingblitz.notoffice563.document.generator.XlsxGenerator.buildXlsxFromSpreadsheet(current.parsedSpreadsheet)
                val outputStream = context.contentResolver.openOutputStream(uri, "rwt")
                    ?: context.contentResolver.openOutputStream(uri, "wt")
                    ?: throw IllegalStateException("Cannot open output stream for XLSX")

                outputStream.use { it.write(bytes) }
                _messageEvents.emit("Saved changes to original spreadsheet")
            } catch (e: Exception) {
                _messageEvents.emit("Save failed: ${e.localizedMessage}")
            }
        }
    }

    private fun columnLetterToIndex(colLetters: String): Int {
        var result = 0
        for (char in colLetters.uppercase()) {
            if (char in 'A'..'Z') {
                result = result * 26 + (char - 'A' + 1)
            }
        }
        return (result - 1).coerceAtLeast(0)
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

    fun updateSlideTitle(slideIndex: Int, newTitle: String) {
        val current = _contentState.value as? ViewerContentState.PptxContent ?: return
        val slides = current.parsedPresentation.slides.toMutableList()
        val slide = slides.getOrNull(slideIndex) ?: return
        slides[slideIndex] = slide.copy(title = newTitle)
        _contentState.value = current.copy(
            parsedPresentation = current.parsedPresentation.copy(slides = slides)
        )
    }

    fun addSlide() {
        val current = _contentState.value as? ViewerContentState.PptxContent ?: return
        val slides = current.parsedPresentation.slides.toMutableList()
        val newSlideNum = slides.size + 1
        val newSlide = com.programmingblitz.notoffice563.domain.model.PresentationSlide(
            slideNumber = newSlideNum,
            title = "New Slide $newSlideNum",
            bodyBullets = emptyList()
        )
        slides.add(newSlide)
        _contentState.value = current.copy(
            parsedPresentation = current.parsedPresentation.copy(slides = slides),
            currentSlideIndex = slides.size - 1
        )
    }

    fun deleteSlide(slideIndex: Int) {
        val current = _contentState.value as? ViewerContentState.PptxContent ?: return
        val slides = current.parsedPresentation.slides.toMutableList()
        if (slides.size <= 1) return
        if (slideIndex in slides.indices) {
            slides.removeAt(slideIndex)
            val renumbered = slides.mapIndexed { idx, s -> s.copy(slideNumber = idx + 1) }
            val newIndex = (current.currentSlideIndex).coerceAtMost(renumbered.size - 1)
            _contentState.value = current.copy(
                parsedPresentation = current.parsedPresentation.copy(slides = renumbered),
                currentSlideIndex = newIndex
            )
        }
    }

    fun saveOriginalPptx(context: Context) {
        val current = _contentState.value as? ViewerContentState.PptxContent ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val uri = Uri.parse(uriString)
                val bytes = com.programmingblitz.notoffice563.document.generator.PptxGenerator.buildPptxFromPresentation(current.parsedPresentation)
                val outputStream = context.contentResolver.openOutputStream(uri, "rwt")
                    ?: context.contentResolver.openOutputStream(uri, "wt")
                    ?: throw IllegalStateException("Cannot open output stream for PPTX")

                outputStream.use { it.write(bytes) }
                _messageEvents.emit("Saved changes to original presentation")
            } catch (e: Exception) {
                _messageEvents.emit("Save failed: ${e.localizedMessage}")
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
