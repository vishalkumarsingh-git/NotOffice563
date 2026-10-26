package com.programmingblitz.notoffice563.ui.viewer

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.programmingblitz.notoffice563.ui.theme.WordDocBlue
import com.programmingblitz.notoffice563.ui.theme.WordDocContainer

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
    var fontSizeSp by remember { mutableFloatStateOf(16f) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var noteInput by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize()) {
        // Toolbar
        Surface(
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reading info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${state.parsedDoc.wordCount} words",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${state.parsedDoc.paragraphs.size} paragraphs",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Font size & Editing Controls
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { fontSizeSp = (fontSizeSp - 2f).coerceAtLeast(12f) },
                        enabled = fontSizeSp > 12f
                    ) {
                        Text("A-", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    IconButton(
                        onClick = { fontSizeSp = (fontSizeSp + 2f).coerceAtMost(28f) },
                        enabled = fontSizeSp < 28f
                    ) {
                        Text("A+", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    IconButton(onClick = { showNoteDialog = true }) {
                        Icon(
                            Icons.Filled.EditNote,
                            contentDescription = "Document Notes",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (state.isEditing) {
                        FilledTonalButton(
                            onClick = onSaveCopy,
                            modifier = Modifier.testTag("docx_save_copy_button")
                        ) {
                            Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save Copy", fontSize = 12.sp)
                        }
                        IconButton(onClick = { onToggleEditing(false) }) {
                            Icon(Icons.Filled.Close, contentDescription = "Cancel Editing")
                        }
                    } else {
                        FilledTonalButton(
                            onClick = { onToggleEditing(true) },
                            modifier = Modifier.testTag("docx_edit_button")
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Basic Edit", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Disclaimer Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(WordDocContainer)
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                tint = WordDocBlue,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (state.isEditing) {
                    "Basic Editing: edits plain text & title, saving as a new .docx copy."
                } else {
                    "Clean Reading Interface: formatted for readability; not affiliated with Microsoft Word."
                },
                fontSize = 11.sp,
                color = WordDocBlue,
                fontWeight = FontWeight.Medium
            )
        }

        // Content Display or Edit Area
        if (state.isEditing) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = state.editedTitle,
                    onValueChange = onTitleChange,
                    label = { Text("Document Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.editedContent,
                    onValueChange = onContentChange,
                    label = { Text("Document Text") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                )
            }
        } else {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Title Header
                    Text(
                        text = state.parsedDoc.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (state.parsedDoc.creator.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Author: ${state.parsedDoc.creator}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    // Paragraphs
                    state.parsedDoc.paragraphs.forEach { p ->
                        when {
                            p.isHeading -> {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = p.text,
                                    fontSize = (fontSizeSp + 4).sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
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
                                    Text("• ", fontSize = fontSizeSp.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = p.text,
                                        fontSize = fontSizeSp.sp,
                                        lineHeight = (fontSizeSp * 1.4f).sp,
                                        fontWeight = if (p.isBold) FontWeight.Bold else FontWeight.Normal,
                                        fontStyle = if (p.isItalic) FontStyle.Italic else FontStyle.Normal
                                    )
                                }
                            }
                            else -> {
                                Text(
                                    text = p.text,
                                    fontSize = fontSizeSp.sp,
                                    lineHeight = (fontSizeSp * 1.45f).sp,
                                    fontWeight = if (p.isBold) FontWeight.Bold else FontWeight.Normal,
                                    fontStyle = if (p.isItalic) FontStyle.Italic else FontStyle.Normal,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Document Note Dialog
    if (showNoteDialog) {
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = { Text("Document Notes") },
            text = {
                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    placeholder = { Text("Write personal notes or summary for this document...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showNoteDialog = false
                        onSaveNote("doc:global", noteInput)
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
