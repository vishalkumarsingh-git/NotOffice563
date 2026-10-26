package com.programmingblitz.notoffice563.document.generator

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.programmingblitz.notoffice563.data.local.RecentFileEntity
import com.programmingblitz.notoffice563.data.repository.DocumentRepository
import com.programmingblitz.notoffice563.domain.model.DocumentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object DocumentCreationManager {

    /**
     * Creates a genuine document from a template, saves it into the app's document storage,
     * registers it with the repository, and returns the Content URI and RecentFileEntity.
     */
    suspend fun createAndSaveDocument(
        context: Context,
        repository: DocumentRepository,
        format: DocumentFormatCategory,
        templateId: String,
        customTitle: String
    ): Result<Pair<Uri, RecentFileEntity>> = withContext(Dispatchers.IO) {
        runCatching {
            val sanitizedTitle = customTitle.trim().ifEmpty { format.title }
                .replace("[\\\\/:*?\"<>|]".toRegex(), "_")
            val fileName = "$sanitizedTitle.${format.extension}"

            // 1. Generate the genuine binary OOXML / PDF bytes
            val bytes = when (format) {
                DocumentFormatCategory.DOCX -> DocxGenerator.generate(templateId, sanitizedTitle)
                DocumentFormatCategory.XLSX -> XlsxGenerator.generate(templateId, sanitizedTitle)
                DocumentFormatCategory.PPTX -> PptxGenerator.generate(templateId, sanitizedTitle)
                DocumentFormatCategory.PDF -> PdfGenerator.generate(templateId, sanitizedTitle)
            }

            // 2. Prepare destination directory
            val docsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                ?: File(context.filesDir, "documents")
            if (!docsDir.exists()) {
                docsDir.mkdirs()
            }

            // Ensure unique filename if already exists
            var targetFile = File(docsDir, fileName)
            var counter = 1
            while (targetFile.exists()) {
                targetFile = File(docsDir, "$sanitizedTitle ($counter).${format.extension}")
                counter++
            }

            // 3. Write file bytes
            FileOutputStream(targetFile).use { fos ->
                fos.write(bytes)
                fos.flush()
            }

            // 4. Get FileProvider Content URI
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                targetFile
            )

            // 5. Register in Room Database
            val recentEntity = repository.registerOpenedDocument(context, contentUri)

            Pair(contentUri, recentEntity)
        }
    }

    /**
     * Writes generated document content directly into a user-selected SAF URI.
     */
    suspend fun writeToSafUri(
        context: Context,
        targetUri: Uri,
        format: DocumentFormatCategory,
        templateId: String,
        customTitle: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val bytes = when (format) {
                DocumentFormatCategory.DOCX -> DocxGenerator.generate(templateId, customTitle)
                DocumentFormatCategory.XLSX -> XlsxGenerator.generate(templateId, customTitle)
                DocumentFormatCategory.PPTX -> PptxGenerator.generate(templateId, customTitle)
                DocumentFormatCategory.PDF -> PdfGenerator.generate(templateId, customTitle)
            }

            context.contentResolver.openOutputStream(targetUri)?.use { os ->
                os.write(bytes)
                os.flush()
            } ?: throw IllegalStateException("Cannot open output stream for selected URI.")
        }
    }
}
