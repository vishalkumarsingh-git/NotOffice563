package com.programmingblitz.notoffice563.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.programmingblitz.notoffice563.domain.model.DocumentType
import com.programmingblitz.notoffice563.ui.theme.ExcelSheetGreen
import com.programmingblitz.notoffice563.ui.theme.PdfRubyRed
import com.programmingblitz.notoffice563.ui.theme.PptPresentationAmber
import com.programmingblitz.notoffice563.ui.theme.WordDocBlue

/**
 * Microsoft 365-inspired custom vector document identity icon.
 * Features classic document fold geometry, vibrant brand colors, and clean typography badges.
 */
@Composable
fun OfficeDocumentIcon(
    documentType: DocumentType,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    val brandColor = when (documentType) {
        DocumentType.WORD_DOCX, DocumentType.WORD_DOC -> WordDocBlue
        DocumentType.EXCEL_XLSX, DocumentType.EXCEL_XLS -> ExcelSheetGreen
        DocumentType.PPT_PPTX, DocumentType.PPT_PPT -> PptPresentationAmber
        DocumentType.PDF -> PdfRubyRed
        else -> WordDocBlue
    }

    val badgeLetter = when (documentType) {
        DocumentType.WORD_DOCX, DocumentType.WORD_DOC -> "W"
        DocumentType.EXCEL_XLSX, DocumentType.EXCEL_XLS -> "X"
        DocumentType.PPT_PPTX, DocumentType.PPT_PPT -> "P"
        DocumentType.PDF -> "PDF"
        else -> "DOC"
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.22f))
            .background(brandColor.copy(alpha = 0.12f))
            .border(1.dp, brandColor.copy(alpha = 0.35f), RoundedCornerShape(size * 0.22f)),
        contentAlignment = Alignment.Center
    ) {
        // Folded Document Vector Base
        Canvas(modifier = Modifier.fillMaxSize().padding(size * 0.18f)) {
            val w = this.size.width
            val h = this.size.height
            val fold = w * 0.28f

            // Document body path with folded top-right corner
            val docPath = Path().apply {
                moveTo(0f, 0f)
                lineTo(w - fold, 0f)
                lineTo(w, fold)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }

            // Draw document page surface
            drawPath(
                path = docPath,
                color = brandColor.copy(alpha = 0.85f)
            )

            // Draw folded corner shadow triangle
            val foldPath = Path().apply {
                moveTo(w - fold, 0f)
                lineTo(w - fold, fold)
                lineTo(w, fold)
                close()
            }
            drawPath(
                path = foldPath,
                color = Color.White.copy(alpha = 0.35f)
            )
        }

        // Inner Embossed Brand Letter / Badge
        Text(
            text = badgeLetter,
            style = TextStyle(
                fontSize = if (badgeLetter.length > 2) (size.value * 0.22f).sp else (size.value * 0.36f).sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        )
    }
}
