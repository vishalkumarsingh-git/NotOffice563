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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.programmingblitz.notoffice563.ui.theme.WordDocBlue
import com.programmingblitz.notoffice563.ui.theme.WordDocContainer
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
    onSaveCopy: () -> Unit,
    onSaveNote: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    var fontSizeSp by remember { mutableFloatStateOf(16f) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var noteInput by remember { mutableStateOf("") }

    // Text Formatting State for Basic Editing
    var isBoldActive by remember { mutableStateOf(false) }
    var isItalicActive by remember { mutableStateOf(false) }
    var isUnderlineActive by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top Liquid Glass Document Status Bar
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
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (state.isEditing) {
                            LiquidGlassButton(
                                text = "Save Copy",
                                onClick = onSaveCopy,
                                icon = Icons.Filled.Save,
                                isPrimary = true,
                                accentColor = WordDocBlue,
                                height = 36.dp,
                                modifier = Modifier.testTag("docx_save_copy_button")
                            )
                            LiquidGlassIconButton(
                                onClick = { onToggleEditing(false) },
                                icon = Icons.Filled.Close,
                                contentDescription = "Cancel Edit",
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

            // Word Document Paper Canvas (Solid, Glare-Free Workspace)
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
                                text = "Content Body",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WordDocBlue
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = state.editedContent,
                                onValueChange = onContentChange,
                                textStyle = TextStyle(
                                    fontSize = fontSizeSp.sp,
                                    color = paperTextPrimary,
                                    fontWeight = if (isBoldActive) FontWeight.Bold else FontWeight.Normal,
                                    fontStyle = if (isItalicActive) FontStyle.Italic else FontStyle.Normal
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(380.dp)
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            // Document Header
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

                            // Paragraphs rendering
                            state.parsedDoc.paragraphs.forEach { p ->
                                when {
                                    p.isHeading -> {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = p.text,
                                            style = TextStyle(
                                                fontSize = (fontSizeSp + 4).sp,
                                                fontWeight = FontWeight.Bold,
                                                color = WordDocBlue,
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
                                                text = p.text,
                                                style = TextStyle(
                                                    fontSize = fontSizeSp.sp,
                                                    lineHeight = (fontSizeSp * 1.45f).sp,
                                                    color = paperTextPrimary,
                                                    fontWeight = if (p.isBold) FontWeight.Bold else FontWeight.Normal,
                                                    fontStyle = if (p.isItalic) FontStyle.Italic else FontStyle.Normal
                                                )
                                            )
                                        }
                                    }

                                    else -> {
                                        Text(
                                            text = p.text,
                                            style = TextStyle(
                                                fontSize = fontSizeSp.sp,
                                                lineHeight = (fontSizeSp * 1.45f).sp,
                                                color = paperTextPrimary,
                                                fontWeight = if (p.isBold) FontWeight.Bold else FontWeight.Normal,
                                                fontStyle = if (p.isItalic) FontStyle.Italic else FontStyle.Normal
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

            // Floating Contextual Formatting Toolbar (Floating Liquid Glass Pill)
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
                            isBoldActive = !isBoldActive
                            if (state.isEditing) {
                                onContentChange("${state.editedContent}\n[Bold: Active]")
                            }
                        },
                        icon = Icons.Filled.FormatBold,
                        contentDescription = "Bold",
                        isActive = isBoldActive,
                        activeColor = WordDocBlue,
                        size = 36.dp
                    )

                    LiquidGlassIconButton(
                        onClick = {
                            isItalicActive = !isItalicActive
                            if (state.isEditing) {
                                onContentChange("${state.editedContent}\n[Italic: Active]")
                            }
                        },
                        icon = Icons.Filled.FormatItalic,
                        contentDescription = "Italic",
                        isActive = isItalicActive,
                        activeColor = WordDocBlue,
                        size = 36.dp
                    )

                    LiquidGlassIconButton(
                        onClick = {
                            isUnderlineActive = !isUnderlineActive
                            if (state.isEditing) {
                                onContentChange("${state.editedContent}\n[Underline: Active]")
                            }
                        },
                        icon = Icons.Filled.FormatUnderlined,
                        contentDescription = "Underline",
                        isActive = isUnderlineActive,
                        activeColor = WordDocBlue,
                        size = 36.dp
                    )

                    LiquidGlassIconButton(
                        onClick = {
                            if (state.isEditing) {
                                onContentChange("${state.editedContent}\n• Bullet Point Item")
                            }
                        },
                        icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                        contentDescription = "Bullet List",
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
