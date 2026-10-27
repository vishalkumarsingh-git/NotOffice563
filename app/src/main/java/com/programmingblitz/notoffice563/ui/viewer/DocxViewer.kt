package com.programmingblitz.notoffice563.ui.viewer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignJustify
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.programmingblitz.notoffice563.domain.model.DocxAlignment
import com.programmingblitz.notoffice563.domain.model.DocxParagraph
import com.programmingblitz.notoffice563.domain.model.DocxRun
import com.programmingblitz.notoffice563.ui.theme.WordDocBlue
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassBadge
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassButton
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassDialog
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassFloatingToolbar
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassIconButton
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassSurface
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassTextField
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassTokens

@Composable
fun DocxViewer(
    state: ViewerContentState.DocxContent,
    onToggleEditing: (Boolean) -> Unit,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onParagraphsChange: (List<DocxParagraph>, String) -> Unit,
    onSaveOriginal: () -> Unit,
    onSaveCopy: () -> Unit,
    onSaveNote: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    var fontSizeSp by remember { mutableFloatStateOf(16f) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var showSaveMenu by remember { mutableStateOf(false) }
    var showUnsavedDialog by remember { mutableStateOf(false) }
    var noteInput by remember { mutableStateOf("") }

    // Rich Text Editor state
    var textFieldValue by remember(state.parsedDoc) {
        mutableStateOf(TextFieldValue(buildAnnotatedStringFromParsed(state.parsedDoc)))
    }
    var activeAlignment by remember { mutableStateOf(DocxAlignment.LEFT) }

    // Active typing styles for empty selection
    var isBoldActive by remember { mutableStateOf(false) }
    var isItalicActive by remember { mutableStateOf(false) }
    var isUnderlineActive by remember { mutableStateOf(false) }

    // Inspect selection styles
    val selection = textFieldValue.selection
    val isSelectionNotEmpty = !selection.collapsed
    val currentAnnotated = textFieldValue.annotatedString

    val isBoldSelected = if (isSelectionNotEmpty) {
        val styles = currentAnnotated.spanStyles.filter { it.start < selection.end && it.end > selection.start }
        styles.any { it.item.fontWeight == FontWeight.Bold }
    } else isBoldActive

    val isItalicSelected = if (isSelectionNotEmpty) {
        val styles = currentAnnotated.spanStyles.filter { it.start < selection.end && it.end > selection.start }
        styles.any { it.item.fontStyle == FontStyle.Italic }
    } else isItalicActive

    val isUnderlineSelected = if (isSelectionNotEmpty) {
        val styles = currentAnnotated.spanStyles.filter { it.start < selection.end && it.end > selection.start }
        styles.any { it.item.textDecoration == TextDecoration.Underline }
    } else isUnderlineActive

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top Liquid Glass Status & Action Bar
            LiquidGlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                shape = LiquidGlassTokens.ShapeMedium,
                isDark = isDark,
                elevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LiquidGlassBadge(
                                text = "WORD DOCX",
                                color = WordDocBlue,
                                containerColor = WordDocBlue.copy(alpha = 0.18f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${state.parsedDoc.wordCount} words • ${state.parsedDoc.paragraphs.size} paragraphs",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    color = if (isDark) LiquidGlassTokens.DarkTextSecondary else LiquidGlassTokens.LightTextSecondary
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        // Save Status Indicator
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            when (state.saveStatus) {
                                SaveStatus.SAVING -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(12.dp),
                                        strokeWidth = 1.5.dp,
                                        color = WordDocBlue
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Saving...",
                                        style = TextStyle(fontSize = 11.sp, color = WordDocBlue)
                                    )
                                }
                                SaveStatus.UNSAVED_CHANGES -> {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFE65100))
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Unsaved changes",
                                        style = TextStyle(fontSize = 11.sp, color = Color(0xFFE65100))
                                    )
                                }
                                SaveStatus.SAVED -> {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Saved",
                                        style = TextStyle(fontSize = 11.sp, color = Color(0xFF2E7D32))
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (state.isEditing) {
                            Box {
                                LiquidGlassButton(
                                    text = "Save",
                                    onClick = { showSaveMenu = true },
                                    icon = Icons.Filled.Save,
                                    isPrimary = true,
                                    accentColor = WordDocBlue,
                                    height = 36.dp,
                                    modifier = Modifier.testTag("docx_save_dropdown_button")
                                )

                                DropdownMenu(
                                    expanded = showSaveMenu,
                                    onDismissRequest = { showSaveMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Save Original") },
                                        leadingIcon = { Icon(Icons.Filled.Save, contentDescription = null) },
                                        onClick = {
                                            showSaveMenu = false
                                            onSaveOriginal()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Save a Copy") },
                                        leadingIcon = { Icon(Icons.Filled.Save, contentDescription = null) },
                                        onClick = {
                                            showSaveMenu = false
                                            onSaveCopy()
                                        }
                                    )
                                }
                            }

                            LiquidGlassIconButton(
                                onClick = {
                                    if (state.hasUnsavedChanges) {
                                        showUnsavedDialog = true
                                    } else {
                                        onToggleEditing(false)
                                    }
                                },
                                icon = Icons.Filled.Close,
                                contentDescription = "Close Edit",
                                size = 36.dp
                            )
                        } else {
                            LiquidGlassButton(
                                text = "Edit",
                                onClick = { onToggleEditing(true) },
                                icon = Icons.Filled.Edit,
                                isPrimary = false,
                                height = 36.dp,
                                modifier = Modifier.testTag("docx_edit_button")
                            )
                        }
                    }
                }
            }

            // Word Canvas Paper Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                val paperBg = if (isDark) LiquidGlassTokens.DocumentCanvasDark else LiquidGlassTokens.DocumentCanvasLight
                val paperTextPrimary = if (isDark) LiquidGlassTokens.DarkTextPrimary else Color(0xFF1E293B)
                val paperTextSecondary = if (isDark) LiquidGlassTokens.DarkTextSecondary else Color(0xFF64748B)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .shadow(8.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(paperBg)
                        .border(
                            1.dp,
                            if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(20.dp)
                ) {
                    if (state.isEditing) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = "Document Title",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WordDocBlue
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LiquidGlassTextField(
                                value = state.editedTitle,
                                onValueChange = onTitleChange,
                                placeholder = "Enter document title..."
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Content Body (Rich Text Editing)",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WordDocBlue
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(400.dp)
                                    .border(
                                        1.dp,
                                        if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.15f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(12.dp)
                            ) {
                                BasicTextField(
                                    value = textFieldValue,
                                    onValueChange = { newValue ->
                                        textFieldValue = applyActiveStylesToNewTypedChar(
                                            oldVal = textFieldValue,
                                            newVal = newValue,
                                            isBold = isBoldActive,
                                            isItalic = isItalicActive,
                                            isUnderline = isUnderlineActive
                                        )
                                        val (paragraphs, rawText) = convertAnnotatedStringToParagraphs(
                                            textFieldValue.annotatedString,
                                            activeAlignment
                                        )
                                        onParagraphsChange(paragraphs, rawText)
                                    },
                                    textStyle = TextStyle(
                                        fontSize = fontSizeSp.sp,
                                        color = paperTextPrimary,
                                        textAlign = when (activeAlignment) {
                                            DocxAlignment.CENTER -> TextAlign.Center
                                            DocxAlignment.RIGHT -> TextAlign.Right
                                            DocxAlignment.JUSTIFY -> TextAlign.Justify
                                            else -> TextAlign.Left
                                        }
                                    ),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .testTag("docx_rich_editor_input")
                                )
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            // Document Title Header
                            Text(
                                text = state.parsedDoc.title,
                                style = TextStyle(
                                    fontSize = (fontSizeSp + 8).sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WordDocBlue,
                                    lineHeight = (fontSizeSp * 1.3f).sp
                                )
                            )

                            if (state.parsedDoc.creator.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Author: ${state.parsedDoc.creator}",
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        color = paperTextSecondary
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f))
                            Spacer(modifier = Modifier.height(16.dp))

                            // Paragraphs rendering with full runs
                            state.parsedDoc.paragraphs.forEach { p ->
                                val textAlign = when (p.alignment) {
                                    DocxAlignment.CENTER -> TextAlign.Center
                                    DocxAlignment.RIGHT -> TextAlign.Right
                                    DocxAlignment.JUSTIFY -> TextAlign.Justify
                                    else -> TextAlign.Left
                                }

                                when {
                                    p.isHeading -> {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = p.fullText,
                                            style = TextStyle(
                                                fontSize = (fontSizeSp + 4).sp,
                                                fontWeight = FontWeight.Bold,
                                                color = WordDocBlue,
                                                textAlign = textAlign,
                                                lineHeight = (fontSizeSp * 1.35f).sp
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                    }

                                    p.isBullet -> {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 3.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Text(
                                                text = "• ",
                                                style = TextStyle(
                                                    fontSize = fontSizeSp.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = WordDocBlue
                                                )
                                            )
                                            Text(
                                                text = buildAnnotatedStringFromRuns(p.runs, fontSizeSp, paperTextPrimary),
                                                style = TextStyle(
                                                    fontSize = fontSizeSp.sp,
                                                    lineHeight = (fontSizeSp * 1.45f).sp,
                                                    textAlign = textAlign
                                                )
                                            )
                                        }
                                    }

                                    else -> {
                                        Text(
                                            text = buildAnnotatedStringFromRuns(p.runs, fontSizeSp, paperTextPrimary),
                                            style = TextStyle(
                                                fontSize = fontSizeSp.sp,
                                                lineHeight = (fontSizeSp * 1.45f).sp,
                                                textAlign = textAlign
                                            ),
                                            modifier = Modifier.padding(vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Floating Contextual Formatting Toolbar (Liquid Glass Pill)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                LiquidGlassFloatingToolbar(isDark = isDark) {
                    // Font Size controls
                    LiquidGlassIconButton(
                        onClick = { fontSizeSp = (fontSizeSp - 2f).coerceAtLeast(12f) },
                        icon = Icons.Filled.FormatSize,
                        contentDescription = "Decrease text size",
                        enabled = fontSizeSp > 12f,
                        size = 36.dp
                    )
                    Text(
                        text = "${fontSizeSp.toInt()}pt",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) LiquidGlassTokens.DarkTextPrimary else LiquidGlassTokens.LightTextPrimary
                        ),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    LiquidGlassIconButton(
                        onClick = { fontSizeSp = (fontSizeSp + 2f).coerceAtMost(28f) },
                        icon = Icons.Filled.FormatSize,
                        contentDescription = "Increase text size",
                        enabled = fontSizeSp < 28f,
                        size = 36.dp
                    )

                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(20.dp)
                            .background(if (isDark) Color.White.copy(0.2f) else Color.Black.copy(0.12f))
                    )
                    Spacer(modifier = Modifier.width(6.dp))

                    // Formatting Buttons: Bold, Italic, Underline
                    LiquidGlassIconButton(
                        onClick = {
                            if (isSelectionNotEmpty) {
                                textFieldValue = toggleSpanStyleOnSelection(
                                    textFieldValue = textFieldValue,
                                    spanStyle = SpanStyle(fontWeight = FontWeight.Bold),
                                    isActive = isBoldSelected
                                )
                                val (paragraphs, rawText) = convertAnnotatedStringToParagraphs(textFieldValue.annotatedString, activeAlignment)
                                onParagraphsChange(paragraphs, rawText)
                            } else {
                                isBoldActive = !isBoldActive
                            }
                        },
                        icon = Icons.Filled.FormatBold,
                        contentDescription = "Bold",
                        isActive = isBoldSelected,
                        activeColor = WordDocBlue,
                        size = 36.dp,
                        modifier = Modifier.testTag("toolbar_bold_button")
                    )

                    LiquidGlassIconButton(
                        onClick = {
                            if (isSelectionNotEmpty) {
                                textFieldValue = toggleSpanStyleOnSelection(
                                    textFieldValue = textFieldValue,
                                    spanStyle = SpanStyle(fontStyle = FontStyle.Italic),
                                    isActive = isItalicSelected
                                )
                                val (paragraphs, rawText) = convertAnnotatedStringToParagraphs(textFieldValue.annotatedString, activeAlignment)
                                onParagraphsChange(paragraphs, rawText)
                            } else {
                                isItalicActive = !isItalicActive
                            }
                        },
                        icon = Icons.Filled.FormatItalic,
                        contentDescription = "Italic",
                        isActive = isItalicSelected,
                        activeColor = WordDocBlue,
                        size = 36.dp,
                        modifier = Modifier.testTag("toolbar_italic_button")
                    )

                    LiquidGlassIconButton(
                        onClick = {
                            if (isSelectionNotEmpty) {
                                textFieldValue = toggleSpanStyleOnSelection(
                                    textFieldValue = textFieldValue,
                                    spanStyle = SpanStyle(textDecoration = TextDecoration.Underline),
                                    isActive = isUnderlineSelected
                                )
                                val (paragraphs, rawText) = convertAnnotatedStringToParagraphs(textFieldValue.annotatedString, activeAlignment)
                                onParagraphsChange(paragraphs, rawText)
                            } else {
                                isUnderlineActive = !isUnderlineActive
                            }
                        },
                        icon = Icons.Filled.FormatUnderlined,
                        contentDescription = "Underline",
                        isActive = isUnderlineSelected,
                        activeColor = WordDocBlue,
                        size = 36.dp,
                        modifier = Modifier.testTag("toolbar_underline_button")
                    )

                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(20.dp)
                            .background(if (isDark) Color.White.copy(0.2f) else Color.Black.copy(0.12f))
                    )
                    Spacer(modifier = Modifier.width(6.dp))

                    // Alignment Controls: Left, Center, Right, Justify
                    LiquidGlassIconButton(
                        onClick = {
                            activeAlignment = DocxAlignment.LEFT
                            val (paragraphs, rawText) = convertAnnotatedStringToParagraphs(textFieldValue.annotatedString, DocxAlignment.LEFT)
                            onParagraphsChange(paragraphs, rawText)
                        },
                        icon = Icons.Filled.FormatAlignLeft,
                        contentDescription = "Align Left",
                        isActive = activeAlignment == DocxAlignment.LEFT,
                        activeColor = WordDocBlue,
                        size = 36.dp
                    )

                    LiquidGlassIconButton(
                        onClick = {
                            activeAlignment = DocxAlignment.CENTER
                            val (paragraphs, rawText) = convertAnnotatedStringToParagraphs(textFieldValue.annotatedString, DocxAlignment.CENTER)
                            onParagraphsChange(paragraphs, rawText)
                        },
                        icon = Icons.Filled.FormatAlignCenter,
                        contentDescription = "Align Center",
                        isActive = activeAlignment == DocxAlignment.CENTER,
                        activeColor = WordDocBlue,
                        size = 36.dp
                    )

                    LiquidGlassIconButton(
                        onClick = {
                            activeAlignment = DocxAlignment.RIGHT
                            val (paragraphs, rawText) = convertAnnotatedStringToParagraphs(textFieldValue.annotatedString, DocxAlignment.RIGHT)
                            onParagraphsChange(paragraphs, rawText)
                        },
                        icon = Icons.Filled.FormatAlignRight,
                        contentDescription = "Align Right",
                        isActive = activeAlignment == DocxAlignment.RIGHT,
                        activeColor = WordDocBlue,
                        size = 36.dp
                    )

                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(20.dp)
                            .background(if (isDark) Color.White.copy(0.2f) else Color.Black.copy(0.12f))
                    )
                    Spacer(modifier = Modifier.width(6.dp))

                    LiquidGlassIconButton(
                        onClick = {
                            val currentText = textFieldValue.text
                            val newText = if (currentText.startsWith("• ")) currentText else "• $currentText"
                            textFieldValue = textFieldValue.copy(text = newText)
                            val (paragraphs, rawText) = convertAnnotatedStringToParagraphs(textFieldValue.annotatedString, activeAlignment)
                            onParagraphsChange(paragraphs, rawText)
                        },
                        icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                        contentDescription = "Bullet List",
                        size = 36.dp
                    )

                    LiquidGlassIconButton(
                        onClick = { showNoteDialog = true },
                        icon = Icons.Filled.EditNote,
                        contentDescription = "Document Notes",
                        activeColor = WordDocBlue,
                        size = 36.dp
                    )
                }
            }
        }
    }

    // Unsaved Changes Confirmation Dialog
    if (showUnsavedDialog) {
        LiquidGlassDialog(
            onDismissRequest = { showUnsavedDialog = false },
            title = "Unsaved Changes",
            isDark = isDark,
            confirmButton = {
                LiquidGlassButton(
                    text = "Save Changes",
                    onClick = {
                        showUnsavedDialog = false
                        onSaveOriginal()
                        onToggleEditing(false)
                    },
                    accentColor = WordDocBlue,
                    height = 40.dp
                )
            },
            dismissButton = {
                LiquidGlassButton(
                    text = "Discard",
                    onClick = {
                        showUnsavedDialog = false
                        onToggleEditing(false)
                    },
                    isPrimary = false,
                    height = 40.dp
                )
            }
        ) {
            Text(
                text = "You have unsaved changes in this document. Would you like to save them before leaving?",
                style = TextStyle(
                    fontSize = 14.sp,
                    color = if (isDark) LiquidGlassTokens.DarkTextPrimary else LiquidGlassTokens.LightTextPrimary
                )
            )
        }
    }

    // Liquid Glass Note Dialog
    if (showNoteDialog) {
        LiquidGlassDialog(
            onDismissRequest = { showNoteDialog = false },
            title = "Document Personal Note",
            isDark = isDark,
            confirmButton = {
                LiquidGlassButton(
                    text = "Save Note",
                    onClick = {
                        showNoteDialog = false
                        onSaveNote("doc:global", noteInput)
                    },
                    accentColor = WordDocBlue,
                    height = 40.dp
                )
            },
            dismissButton = {
                LiquidGlassButton(
                    text = "Cancel",
                    onClick = { showNoteDialog = false },
                    isPrimary = false,
                    height = 40.dp
                )
            }
        ) {
            Text(
                text = "Store private annotations for this document without altering the original file:",
                style = TextStyle(
                    fontSize = 13.sp,
                    color = if (isDark) LiquidGlassTokens.DarkTextSecondary else LiquidGlassTokens.LightTextSecondary
                )
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = noteInput,
                onValueChange = { noteInput = it },
                placeholder = { Text("Write personal notes or action items...") },
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    color = if (isDark) LiquidGlassTokens.DarkTextPrimary else LiquidGlassTokens.LightTextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            )
        }
    }
}

// Helpers for Rich Text Conversion & Selection Manipulation

private fun buildAnnotatedStringFromParsed(doc: com.programmingblitz.notoffice563.domain.model.ParsedDocxDocument): AnnotatedString {
    return buildAnnotatedString {
        doc.paragraphs.forEachIndexed { pIndex, p ->
            if (pIndex > 0) append("\n")
            if (p.isBullet) append("• ")
            if (p.runs.isNotEmpty()) {
                p.runs.forEach { run ->
                    withStyle(
                        SpanStyle(
                            fontWeight = if (run.isBold) FontWeight.Bold else FontWeight.Normal,
                            fontStyle = if (run.isItalic) FontStyle.Italic else FontStyle.Normal,
                            textDecoration = if (run.isUnderline) TextDecoration.Underline else TextDecoration.None
                        )
                    ) {
                        append(run.text)
                    }
                }
            } else {
                withStyle(
                    SpanStyle(
                        fontWeight = if (p.isBold) FontWeight.Bold else FontWeight.Normal,
                        fontStyle = if (p.isItalic) FontStyle.Italic else FontStyle.Normal,
                        textDecoration = if (p.isUnderline) TextDecoration.Underline else TextDecoration.None
                    )
                ) {
                    append(p.fullText)
                }
            }
        }
    }
}

private fun buildAnnotatedStringFromRuns(runs: List<DocxRun>, fontSizeSp: Float, defaultColor: Color): AnnotatedString {
    return buildAnnotatedString {
        if (runs.isEmpty()) {
            append("")
            return@buildAnnotatedString
        }
        runs.forEach { run ->
            withStyle(
                SpanStyle(
                    color = defaultColor,
                    fontWeight = if (run.isBold) FontWeight.Bold else FontWeight.Normal,
                    fontStyle = if (run.isItalic) FontStyle.Italic else FontStyle.Normal,
                    textDecoration = if (run.isUnderline) TextDecoration.Underline else TextDecoration.None,
                    fontSize = (run.fontSizeSp ?: fontSizeSp).sp
                )
            ) {
                append(run.text)
            }
        }
    }
}

private fun applyActiveStylesToNewTypedChar(
    oldVal: TextFieldValue,
    newVal: TextFieldValue,
    isBold: Boolean,
    isItalic: Boolean,
    isUnderline: Boolean
): TextFieldValue {
    if (!isBold && !isItalic && !isUnderline) return newVal
    if (newVal.text.length <= oldVal.text.length) return newVal

    val start = oldVal.selection.start.coerceAtLeast(0)
    val end = newVal.selection.start.coerceAtMost(newVal.text.length)
    if (start >= end) return newVal

    val builder = AnnotatedString.Builder(newVal.annotatedString)
    if (isBold) builder.addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
    if (isItalic) builder.addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
    if (isUnderline) builder.addStyle(SpanStyle(textDecoration = TextDecoration.Underline), start, end)

    return newVal.copy(annotatedString = builder.toAnnotatedString())
}

private fun toggleSpanStyleOnSelection(
    textFieldValue: TextFieldValue,
    spanStyle: SpanStyle,
    isActive: Boolean
): TextFieldValue {
    val sel = textFieldValue.selection
    if (sel.collapsed) return textFieldValue

    val start = sel.min
    val end = sel.max
    val currentAnnotated = textFieldValue.annotatedString

    val builder = AnnotatedString.Builder(currentAnnotated.text)

    // Copy existing styles excluding the target style on this range if isActive is true
    currentAnnotated.spanStyles.forEach { range ->
        if (isActive && range.start >= start && range.end <= end) {
            val style = range.item
            val sameWeight = spanStyle.fontWeight != null && style.fontWeight == spanStyle.fontWeight
            val sameItalic = spanStyle.fontStyle != null && style.fontStyle == spanStyle.fontStyle
            val sameUnderline = spanStyle.textDecoration != null && style.textDecoration == spanStyle.textDecoration

            if (!sameWeight && !sameItalic && !sameUnderline) {
                builder.addStyle(style, range.start, range.end)
            }
        } else {
            builder.addStyle(range.item, range.start, range.end)
        }
    }

    if (!isActive) {
        builder.addStyle(spanStyle, start, end)
    }

    return textFieldValue.copy(annotatedString = builder.toAnnotatedString())
}

private fun convertAnnotatedStringToParagraphs(
    annotatedString: AnnotatedString,
    alignment: DocxAlignment
): Pair<List<DocxParagraph>, String> {
    val text = annotatedString.text
    val rawParagraphs = text.split("\n")
    val resultParagraphs = mutableListOf<DocxParagraph>()

    var charIndex = 0
    for (pText in rawParagraphs) {
        val pEnd = charIndex + pText.length
        val isBullet = pText.startsWith("• ")
        val cleanPText = if (isBullet) pText.substring(2) else pText

        val runs = mutableListOf<DocxRun>()
        if (cleanPText.isNotEmpty()) {
            val pSpans = annotatedString.spanStyles.filter { it.start < pEnd && it.end > charIndex }

            val isBold = pSpans.any { it.item.fontWeight == FontWeight.Bold }
            val isItalic = pSpans.any { it.item.fontStyle == FontStyle.Italic }
            val isUnderline = pSpans.any { it.item.textDecoration == TextDecoration.Underline }

            runs.add(
                DocxRun(
                    text = cleanPText,
                    isBold = isBold,
                    isItalic = isItalic,
                    isUnderline = isUnderline
                )
            )
        }

        resultParagraphs.add(
            DocxParagraph(
                text = cleanPText,
                runs = runs,
                isBullet = isBullet,
                alignment = alignment
            )
        )

        charIndex = pEnd + 1
    }

    return Pair(resultParagraphs, text)
}
