package com.programmingblitz.notoffice563.ui.creation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.programmingblitz.notoffice563.document.generator.DocxGenerator
import com.programmingblitz.notoffice563.document.generator.DocumentFormatCategory
import com.programmingblitz.notoffice563.document.generator.DocumentStarterTemplate
import com.programmingblitz.notoffice563.document.generator.PdfGenerator
import com.programmingblitz.notoffice563.document.generator.PptxGenerator
import com.programmingblitz.notoffice563.document.generator.XlsxGenerator
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassBadge
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassButton
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassIconButton
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassSegmentedControl
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassSurface
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassTextField
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassTokens
import com.programmingblitz.notoffice563.util.HapticUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDocumentBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onCreateDocument: (format: DocumentFormatCategory, templateId: String, title: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    var selectedFormatIndex by remember { mutableStateOf(0) }
    val formats = listOf(
        DocumentFormatCategory.DOCX,
        DocumentFormatCategory.XLSX,
        DocumentFormatCategory.PPTX,
        DocumentFormatCategory.PDF
    )
    val selectedFormat = formats[selectedFormatIndex]

    val templates: List<DocumentStarterTemplate> = when (selectedFormat) {
        DocumentFormatCategory.DOCX -> DocxGenerator.templates
        DocumentFormatCategory.XLSX -> XlsxGenerator.templates
        DocumentFormatCategory.PPTX -> PptxGenerator.templates
        DocumentFormatCategory.PDF -> PdfGenerator.templates
    }

    var selectedTemplateId by remember(selectedFormat) {
        mutableStateOf(templates.firstOrNull()?.id ?: "")
    }

    val currentTemplate = templates.find { it.id == selectedTemplateId } ?: templates.first()
    var documentTitle by remember(selectedFormat, selectedTemplateId) {
        mutableStateOf(currentTemplate.defaultFileName)
    }

    var isCreating by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isDark) LiquidGlassTokens.DarkGlassSurfaceElevated else LiquidGlassTokens.LightGlassSurfaceElevated,
        shape = LiquidGlassTokens.ShapeLarge,
        dragHandle = null
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp)
                .testTag("create_document_sheet")
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(LiquidGlassTokens.AzureGlass.copy(alpha = 0.15f))
                        .border(1.dp, LiquidGlassTokens.AzureGlass.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        tint = LiquidGlassTokens.AzureGlass,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "New Document",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) LiquidGlassTokens.DarkTextPrimary else LiquidGlassTokens.LightTextPrimary
                    )
                    Text(
                        text = "Select a format and starter template",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) LiquidGlassTokens.DarkTextSecondary else LiquidGlassTokens.LightTextSecondary
                    )
                }

                LiquidGlassIconButton(
                    onClick = onDismiss,
                    icon = Icons.Filled.Close,
                    contentDescription = "Close",
                    size = 32.dp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Format Segmented Selector
            LiquidGlassSegmentedControl(
                items = listOf("Word", "Excel", "PowerPoint", "PDF"),
                selectedIndex = selectedFormatIndex,
                onSelectIndex = { index ->
                    selectedFormatIndex = index
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Title input
            Text(
                text = "Document Title",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) LiquidGlassTokens.DarkTextSecondary else LiquidGlassTokens.LightTextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            LiquidGlassTextField(
                value = documentTitle,
                onValueChange = { documentTitle = it },
                placeholder = "Enter document title...",
                onClear = { documentTitle = "" }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Templates list
            Text(
                text = "Choose Template",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) LiquidGlassTokens.DarkTextSecondary else LiquidGlassTokens.LightTextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                templates.forEach { tmpl ->
                    val isSelected = tmpl.id == selectedTemplateId
                    val icon = when (selectedFormat) {
                        DocumentFormatCategory.DOCX -> Icons.Filled.Description
                        DocumentFormatCategory.XLSX -> Icons.Filled.TableChart
                        DocumentFormatCategory.PPTX -> Icons.Filled.Slideshow
                        DocumentFormatCategory.PDF -> Icons.Filled.PictureAsPdf
                    }

                    TemplateTile(
                        title = tmpl.title,
                        description = tmpl.description,
                        icon = icon,
                        isSelected = isSelected,
                        format = selectedFormat,
                        onClick = {
                            HapticUtil.performSelection(haptic)
                            selectedTemplateId = tmpl.id
                            documentTitle = tmpl.defaultFileName
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Button
            LiquidGlassButton(
                text = "Create ${selectedFormat.title}",
                icon = Icons.Filled.Add,
                isLoading = isCreating,
                onClick = {
                    isCreating = true
                    onCreateDocument(selectedFormat, selectedTemplateId, documentTitle)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("confirm_create_document_button")
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Generates authentic ${selectedFormat.extension.uppercase()} files that open seamlessly across mobile & desktop software.",
                style = MaterialTheme.typography.labelSmall,
                color = if (isDark) LiquidGlassTokens.DarkTextTertiary else LiquidGlassTokens.LightTextTertiary,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TemplateTile(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    format: DocumentFormatCategory,
    onClick: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val accentColor = when (format) {
        DocumentFormatCategory.DOCX -> Color(0xFF007AFF)
        DocumentFormatCategory.XLSX -> Color(0xFF34C759)
        DocumentFormatCategory.PPTX -> Color(0xFFFF9500)
        DocumentFormatCategory.PDF -> Color(0xFFFF3B30)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(LiquidGlassTokens.ShapeSmall)
            .background(
                if (isSelected) {
                    accentColor.copy(alpha = if (isDark) 0.18f else 0.1f)
                } else {
                    if (isDark) Color(0x1AFFFFFF) else Color(0x66FFFFFF)
                }
            )
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) accentColor else (if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.06f)),
                shape = LiquidGlassTokens.ShapeSmall
            )
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(LiquidGlassTokens.ShapeMicro)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isDark) LiquidGlassTokens.DarkTextPrimary else LiquidGlassTokens.LightTextPrimary
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) LiquidGlassTokens.DarkTextSecondary else LiquidGlassTokens.LightTextSecondary
                )
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(accentColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
