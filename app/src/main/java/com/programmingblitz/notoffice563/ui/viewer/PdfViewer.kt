package com.programmingblitz.notoffice563.ui.viewer

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.programmingblitz.notoffice563.ui.theme.PdfRubyRed
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassBadge
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassButton
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassDialog
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassFloatingToolbar
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassIconButton
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassSurface
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassTokens

@Composable
fun PdfViewer(
    state: ViewerContentState.PdfContent,
    onPageChange: (Int) -> Unit,
    onZoomChange: (Float) -> Unit,
    onSaveNote: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    var showNoteDialog by remember { mutableStateOf(false) }
    var noteInput by remember { mutableStateOf("") }
    var showJumpDialog by remember { mutableStateOf(false) }
    var jumpPageInput by remember { mutableStateOf("${state.currentPage + 1}") }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Liquid Glass PDF Status Header
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LiquidGlassBadge(
                            text = "PDF DOCUMENT",
                            color = PdfRubyRed,
                            containerColor = PdfRubyRed.copy(alpha = 0.18f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Page ${state.currentPage + 1} of ${state.pageCount}",
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) LiquidGlassTokens.DarkTextSecondary else LiquidGlassTokens.LightTextSecondary
                            )
                        )
                    }

                    Icon(
                        imageVector = Icons.Filled.PictureAsPdf,
                        contentDescription = null,
                        tint = PdfRubyRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Rendered PDF Page Canvas (Solid Reading Surface)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                val canvasBg = if (isDark) LiquidGlassTokens.DocumentCanvasDark else Color(0xFFE2E8F0)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(canvasBg)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.renderedPageBitmap != null) {
                        Box(
                            modifier = Modifier
                                .shadow(10.dp, RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                        ) {
                            Image(
                                bitmap = state.renderedPageBitmap.asImageBitmap(),
                                contentDescription = "PDF Page ${state.currentPage + 1}",
                                modifier = Modifier.fillMaxWidth(fraction = (state.zoomFactor).coerceIn(0.5f, 1.0f)),
                                contentScale = ContentScale.FillWidth
                            )
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(44.dp),
                                color = PdfRubyRed,
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Rendering PDF page...",
                                style = TextStyle(
                                    fontSize = 13.sp,
                                    color = if (isDark) LiquidGlassTokens.DarkTextSecondary else LiquidGlassTokens.LightTextSecondary
                                )
                            )
                        }
                    }
                }
            }

            // Bottom Floating Liquid Glass Navigation & Zoom Toolbar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                LiquidGlassFloatingToolbar(isDark = isDark) {
                    // Page Prev
                    LiquidGlassIconButton(
                        onClick = { onPageChange(state.currentPage - 1) },
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Page",
                        enabled = state.currentPage > 0,
                        size = 36.dp,
                        modifier = Modifier.testTag("pdf_prev_page")
                    )

                    // Page Pill
                    Box(
                        modifier = Modifier
                            .clip(LiquidGlassTokens.ShapeCapsule)
                            .background(if (isDark) Color(0x33FFFFFF) else Color(0x1F000000))
                            .clickable {
                                jumpPageInput = "${state.currentPage + 1}"
                                showJumpDialog = true
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${state.currentPage + 1} / ${state.pageCount}",
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) LiquidGlassTokens.DarkTextPrimary else LiquidGlassTokens.LightTextPrimary
                            )
                        )
                    }

                    // Page Next
                    LiquidGlassIconButton(
                        onClick = { onPageChange(state.currentPage + 1) },
                        icon = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Page",
                        enabled = state.currentPage < state.pageCount - 1,
                        size = 36.dp,
                        modifier = Modifier.testTag("pdf_next_page")
                    )

                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(20.dp)
                            .background(if (isDark) Color.White.copy(0.2f) else Color.Black.copy(0.12f))
                    )
                    Spacer(modifier = Modifier.width(6.dp))

                    // Zoom Out
                    LiquidGlassIconButton(
                        onClick = { onZoomChange(state.zoomFactor - 0.25f) },
                        icon = Icons.Filled.ZoomOut,
                        contentDescription = "Zoom Out",
                        enabled = state.zoomFactor > 0.6f,
                        size = 36.dp
                    )

                    Text(
                        text = "${(state.zoomFactor * 100).toInt()}%",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) LiquidGlassTokens.DarkTextPrimary else LiquidGlassTokens.LightTextPrimary
                        ),
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )

                    // Zoom In
                    LiquidGlassIconButton(
                        onClick = { onZoomChange(state.zoomFactor + 0.25f) },
                        icon = Icons.Filled.ZoomIn,
                        contentDescription = "Zoom In",
                        enabled = state.zoomFactor < 2.5f,
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

                    // Page Note
                    LiquidGlassIconButton(
                        onClick = { showNoteDialog = true },
                        icon = Icons.Filled.EditNote,
                        contentDescription = "Page Note",
                        activeColor = PdfRubyRed,
                        size = 36.dp,
                        modifier = Modifier.testTag("pdf_notes_button")
                    )
                }
            }
        }
    }

    // Page Note Liquid Glass Dialog
    if (showNoteDialog) {
        LiquidGlassDialog(
            onDismissRequest = { showNoteDialog = false },
            title = "Note for Page ${state.currentPage + 1}",
            isDark = isDark,
            confirmButton = {
                LiquidGlassButton(
                    text = "Save Note",
                    onClick = {
                        showNoteDialog = false
                        onSaveNote("page:${state.currentPage}", noteInput)
                    },
                    accentColor = PdfRubyRed,
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
                text = "Store a personal note for Page ${state.currentPage + 1} without modifying the PDF document:",
                style = TextStyle(
                    fontSize = 13.sp,
                    color = if (isDark) LiquidGlassTokens.DarkTextSecondary else LiquidGlassTokens.LightTextSecondary
                )
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = noteInput,
                onValueChange = { noteInput = it },
                placeholder = { Text("Type key takeaway or notes...") },
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

    // Jump to Page Liquid Glass Dialog
    if (showJumpDialog) {
        LiquidGlassDialog(
            onDismissRequest = { showJumpDialog = false },
            title = "Jump to Page",
            isDark = isDark,
            confirmButton = {
                LiquidGlassButton(
                    text = "Go",
                    onClick = {
                        val target = jumpPageInput.toIntOrNull()
                        if (target != null && target in 1..state.pageCount) {
                            onPageChange(target - 1)
                        }
                        showJumpDialog = false
                    },
                    accentColor = PdfRubyRed,
                    height = 40.dp
                )
            },
            dismissButton = {
                LiquidGlassButton(
                    text = "Cancel",
                    onClick = { showJumpDialog = false },
                    isPrimary = false,
                    height = 40.dp
                )
            }
        ) {
            OutlinedTextField(
                value = jumpPageInput,
                onValueChange = { jumpPageInput = it },
                label = { Text("Page Number (1 - ${state.pageCount})") },
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 15.sp,
                    color = if (isDark) LiquidGlassTokens.DarkTextPrimary else LiquidGlassTokens.LightTextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
