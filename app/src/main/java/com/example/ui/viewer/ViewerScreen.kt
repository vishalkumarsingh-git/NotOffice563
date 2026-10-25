package com.example.ui.viewer

import android.net.Uri
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.document.saf.SafHelper
import com.example.ui.components.EmptyState
import com.example.ui.components.FileInfoBottomSheet
import com.example.ui.theme.PptPresentationAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewerScreen(
    viewModel: ViewerViewModel,
    onNavigateBack: () -> Unit,
    onOpenNewDocument: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val contentState by viewModel.contentState.collectAsStateWithLifecycle()
    val fileMetadata by viewModel.fileMetadata.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showFileInfoSheet by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        viewModel.loadDocument(context)
        viewModel.messageEvents.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = fileMetadata?.displayName ?: "Document Viewer",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Favorite Toggle
                    IconButton(
                        onClick = { viewModel.toggleFavorite() },
                        modifier = Modifier.testTag("viewer_favorite_button")
                    ) {
                        val isFav = fileMetadata?.isFavorite == true
                        Icon(
                            imageVector = if (isFav) Icons.Filled.Star else Icons.Filled.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (isFav) PptPresentationAmber else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Share Button
                    IconButton(
                        onClick = {
                            val meta = fileMetadata
                            if (meta != null) {
                                SafHelper.shareDocument(
                                    context = context,
                                    uri = Uri.parse(meta.uri),
                                    mimeType = meta.mimeType,
                                    title = meta.displayName
                                )
                            }
                        }
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "Share")
                    }

                    // Overflow Menu
                    Box {
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "More")
                        }

                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("File Details") },
                                onClick = {
                                    showOverflowMenu = false
                                    showFileInfoSheet = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Open with another app") },
                                onClick = {
                                    showOverflowMenu = false
                                    val meta = fileMetadata
                                    if (meta != null) {
                                        SafHelper.openWithAnotherApp(
                                            context = context,
                                            uri = Uri.parse(meta.uri),
                                            mimeType = meta.mimeType
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = contentState) {
                is ViewerContentState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(modifier = Modifier.size(44.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Preparing document viewer...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                is ViewerContentState.PdfContent -> {
                    PdfViewer(
                        state = state,
                        onPageChange = { viewModel.setPdfPage(it) },
                        onZoomChange = { viewModel.setPdfZoom(it) },
                        onSaveNote = { key, text -> viewModel.saveNote(key, text) }
                    )
                }

                is ViewerContentState.DocxContent -> {
                    DocxViewer(
                        state = state,
                        onToggleEditing = { viewModel.toggleDocxEditing(it) },
                        onTitleChange = { viewModel.updateDocxEditTitle(it) },
                        onContentChange = { viewModel.updateDocxEditContent(it) },
                        onSaveCopy = {
                            viewModel.saveEditedDocxCopy(context) { newUri ->
                                onOpenNewDocument(newUri.toString())
                            }
                        },
                        onSaveNote = { key, text -> viewModel.saveNote(key, text) }
                    )
                }

                is ViewerContentState.XlsxContent -> {
                    XlsxViewer(
                        state = state,
                        onSelectSheet = { viewModel.setSheetIndex(it) },
                        onSelectCell = { ref, valStr -> viewModel.selectCell(ref, valStr) }
                    )
                }

                is ViewerContentState.PptxContent -> {
                    PptxViewer(
                        state = state,
                        onSlideSelect = { viewModel.setSlideIndex(it) }
                    )
                }

                is ViewerContentState.LegacyFallback -> {
                    LegacyFallbackViewer(
                        documentType = state.documentType,
                        message = state.message,
                        onOpenWithAnotherApp = {
                            val meta = fileMetadata
                            if (meta != null) {
                                SafHelper.openWithAnotherApp(
                                    context = context,
                                    uri = Uri.parse(meta.uri),
                                    mimeType = meta.mimeType
                                )
                            }
                        },
                        onShare = {
                            val meta = fileMetadata
                            if (meta != null) {
                                SafHelper.shareDocument(
                                    context = context,
                                    uri = Uri.parse(meta.uri),
                                    mimeType = meta.mimeType,
                                    title = meta.displayName
                                )
                            }
                        },
                        onShowFileInfo = { showFileInfoSheet = true }
                    )
                }

                is ViewerContentState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Unable to view document",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        if (state.canOpenExternally && fileMetadata != null) {
                            Button(
                                onClick = {
                                    val meta = fileMetadata!!
                                    SafHelper.openWithAnotherApp(
                                        context = context,
                                        uri = Uri.parse(meta.uri),
                                        mimeType = meta.mimeType
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Filled.OpenInBrowser, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Open with another app")
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        OutlinedButton(
                            onClick = { viewModel.loadDocument(context) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }

    if (showFileInfoSheet && fileMetadata != null) {
        FileInfoBottomSheet(
            file = fileMetadata,
            sheetState = sheetState,
            onDismiss = { showFileInfoSheet = false },
            onOpen = {},
            onRename = { _, _ -> },
            onDelete = { onNavigateBack() },
            onShare = {
                val meta = fileMetadata!!
                SafHelper.shareDocument(
                    context = context,
                    uri = Uri.parse(meta.uri),
                    mimeType = meta.mimeType,
                    title = meta.displayName
                )
            },
            onOpenWith = {
                val meta = fileMetadata!!
                SafHelper.openWithAnotherApp(
                    context = context,
                    uri = Uri.parse(meta.uri),
                    mimeType = meta.mimeType
                )
            },
            onToggleFavorite = { viewModel.toggleFavorite() },
            onRemoveRecent = { onNavigateBack() }
        )
    }
}
