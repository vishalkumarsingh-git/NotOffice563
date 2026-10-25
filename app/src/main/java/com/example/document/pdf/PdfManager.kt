package com.example.document.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class PdfManager(private val context: Context) {

    private var fileDescriptor: ParcelFileDescriptor? = null
    private var pdfRenderer: PdfRenderer? = null
    private var tempFile: File? = null

    val pageCount: Int
        get() = pdfRenderer?.pageCount ?: 0

    suspend fun open(uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            close()
            // Direct open via ContentResolver
            try {
                fileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
            } catch (e: Exception) {
                // If direct descriptor fails, copy to local cache file
                val cacheFile = File(context.cacheDir, "current_view_pdf_${System.currentTimeMillis()}.pdf")
                tempFile = cacheFile
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(cacheFile).use { output ->
                        input.copyTo(output)
                    }
                }
                fileDescriptor = ParcelFileDescriptor.open(cacheFile, ParcelFileDescriptor.MODE_READ_ONLY)
            }

            val pfd = fileDescriptor ?: throw IllegalStateException("Unable to open PDF file descriptor.")
            pdfRenderer = PdfRenderer(pfd)
            Result.success(pdfRenderer?.pageCount ?: 0)
        } catch (e: Exception) {
            close()
            Result.failure(e)
        }
    }

    suspend fun renderPage(pageIndex: Int, targetWidth: Int = 1200): Bitmap? = withContext(Dispatchers.Default) {
        val renderer = pdfRenderer ?: return@withContext null
        if (pageIndex < 0 || pageIndex >= renderer.pageCount) return@withContext null

        synchronized(renderer) {
            try {
                val page = renderer.openPage(pageIndex)
                val width = targetWidth.coerceAtLeast(300)
                val height = (width.toFloat() * page.height / page.width).toInt().coerceAtLeast(300)

                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                // Fill white background before rendering
                bitmap.eraseColor(Color.WHITE)

                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                bitmap
            } catch (_: Exception) {
                null
            }
        }
    }

    fun close() {
        try {
            pdfRenderer?.close()
        } catch (_: Exception) {}
        pdfRenderer = null

        try {
            fileDescriptor?.close()
        } catch (_: Exception) {}
        fileDescriptor = null

        tempFile?.delete()
        tempFile = null
    }
}
