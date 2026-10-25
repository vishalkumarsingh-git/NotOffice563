package com.example.ui.recent

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.RecentFileEntity
import com.example.document.saf.SafHelper
import com.example.ui.components.DocumentListItem
import com.example.ui.components.EmptyState
import com.example.ui.components.FileInfoBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentScreen(
    viewModel: RecentViewModel,
    onNavigateToViewer: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedFileForInfo by remember { mutableStateOf<RecentFileEntity?>(null) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        viewModel.messageEvents.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text("Recent Documents", fontWeight = FontWeight.Bold)
                },
                actions = {
                    if (uiState.files.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearConfirmDialog = true },
                            modifier = Modifier.testTag("clear_all_recent_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteSweep,
                                contentDescription = "Clear all recent files"
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.files.isEmpty()) {
                EmptyState(
                    title = "No recent files",
                    subtitle = "Documents you open from your storage or external apps will appear here for fast access.",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = "Showing ${uiState.files.size} recent document${if (uiState.files.size == 1) "" else "s"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.files, key = { it.uri }) { file ->
                        DocumentListItem(
                            file = file,
                            onClick = { onNavigateToViewer(file.uri) },
                            onFavoriteToggle = { viewModel.toggleFavorite(file) },
                            onInfoClick = { selectedFileForInfo = file },
                            onShareClick = {
                                SafHelper.shareDocument(
                                    context = context,
                                    uri = Uri.parse(file.uri),
                                    mimeType = file.mimeType,
                                    title = file.displayName
                                )
                            },
                            onOpenWithClick = {
                                SafHelper.openWithAnotherApp(
                                    context = context,
                                    uri = Uri.parse(file.uri),
                                    mimeType = file.mimeType
                                )
                            },
                            onRemoveRecentClick = { viewModel.removeRecent(file) }
                        )
                    }
                }
            }
        }
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Clear Recent Documents?") },
            text = {
                Text("This will clear your recent document history in NotOffice563. Your original files on device storage will remain untouched.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirmDialog = false
                        viewModel.clearAllRecent()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (selectedFileForInfo != null) {
        FileInfoBottomSheet(
            file = selectedFileForInfo,
            sheetState = sheetState,
            onDismiss = { selectedFileForInfo = null },
            onOpen = { onNavigateToViewer(it.uri) },
            onRename = { file, newName -> viewModel.renameFile(context, file, newName) },
            onDelete = { file -> viewModel.deleteFile(context, file) },
            onShare = { file ->
                SafHelper.shareDocument(
                    context = context,
                    uri = Uri.parse(file.uri),
                    mimeType = file.mimeType,
                    title = file.displayName
                )
            },
            onOpenWith = { file ->
                SafHelper.openWithAnotherApp(
                    context = context,
                    uri = Uri.parse(file.uri),
                    mimeType = file.mimeType
                )
            },
            onToggleFavorite = { viewModel.toggleFavorite(it) },
            onRemoveRecent = { viewModel.removeRecent(it) }
        )
    }
}
