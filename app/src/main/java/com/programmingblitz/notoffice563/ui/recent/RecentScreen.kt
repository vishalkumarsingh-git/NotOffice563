package com.programmingblitz.notoffice563.ui.recent

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.programmingblitz.notoffice563.data.local.RecentFileEntity
import com.programmingblitz.notoffice563.document.saf.SafHelper
import com.programmingblitz.notoffice563.ui.components.DocumentListItem
import com.programmingblitz.notoffice563.ui.components.EmptyState
import com.programmingblitz.notoffice563.ui.components.FileInfoBottomSheet
import com.programmingblitz.notoffice563.ui.theme.ExpenseRed
import com.programmingblitz.notoffice563.ui.theme.SleekShapes
import com.programmingblitz.notoffice563.ui.theme.SleekSizes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentScreen(
    viewModel: RecentViewModel,
    onNavigateToViewer: (String) -> Unit,
    onOpenDrawer: (() -> Unit)? = null,
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if (onOpenDrawer != null) {
                        IconButton(
                            onClick = onOpenDrawer,
                            modifier = Modifier.testTag("open_drawer_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Open navigation drawer",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                },
                title = {
                    Text(
                        text = "Recent Documents",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    if (uiState.files.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearConfirmDialog = true },
                            modifier = Modifier
                                .size(SleekSizes.buttonSmall)
                                .testTag("clear_all_recent_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteSweep,
                                contentDescription = "Clear all recent files",
                                tint = MaterialTheme.colorScheme.error
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
                    title = "No recent documents",
                    subtitle = "Documents opened from your device storage or shared from other apps will appear here for fast access.",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = "${uiState.files.size} document${if (uiState.files.size == 1) "" else "s"} in history",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
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
            shape = SleekShapes.dialog,
            title = {
                Text(
                    text = "Clear Recent Documents?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "This will clear your recent document history in NotOffice563. Your original files on device storage will remain untouched.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirmDialog = false
                        viewModel.clearAllRecent()
                    },
                    shape = SleekShapes.card,
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text("Clear History")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // File details bottom sheet
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
