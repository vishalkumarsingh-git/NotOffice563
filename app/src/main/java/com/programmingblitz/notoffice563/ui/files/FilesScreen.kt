package com.programmingblitz.notoffice563.ui.files

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassFilterChip
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassFloatingActionButton
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassIconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.programmingblitz.notoffice563.data.local.FileSortOrder
import com.programmingblitz.notoffice563.data.local.FileViewMode
import com.programmingblitz.notoffice563.data.local.RecentFileEntity
import com.programmingblitz.notoffice563.document.saf.SafHelper
import com.programmingblitz.notoffice563.domain.model.DocumentCategory
import com.programmingblitz.notoffice563.ui.components.DocumentGridItem
import com.programmingblitz.notoffice563.ui.components.DocumentListItem
import com.programmingblitz.notoffice563.ui.components.EmptyState
import com.programmingblitz.notoffice563.ui.components.FileInfoBottomSheet
import com.programmingblitz.notoffice563.ui.theme.AccentBlue
import com.programmingblitz.notoffice563.ui.theme.AccentBlueContainer
import com.programmingblitz.notoffice563.ui.theme.OnAccentBlueContainer
import com.programmingblitz.notoffice563.ui.theme.SleekShapes
import com.programmingblitz.notoffice563.ui.theme.SleekSizes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesScreen(
    viewModel: FilesViewModel,
    onNavigateToViewer: (String) -> Unit,
    onOpenDrawer: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedFileForInfo by remember { mutableStateOf<RecentFileEntity?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.onDocumentPicked(context, uri, onNavigateToViewer)
        }
    }

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
                        LiquidGlassIconButton(
                            onClick = onOpenDrawer,
                            icon = Icons.Filled.Menu,
                            contentDescription = "Open navigation drawer",
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .testTag("open_drawer_button")
                        )
                    }
                },
                title = {
                    Text(
                        text = "Workspace Files",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    // View mode toggle
                    LiquidGlassIconButton(
                        onClick = { viewModel.toggleViewMode() },
                        icon = if (uiState.viewMode == FileViewMode.LIST) Icons.Filled.GridView else Icons.AutoMirrored.Filled.ViewList,
                        contentDescription = "Switch view mode",
                        modifier = Modifier.testTag("toggle_view_mode_button")
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Sort menu
                    Box {
                        LiquidGlassIconButton(
                            onClick = { showSortMenu = true },
                            icon = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = "Sort files",
                            modifier = Modifier.testTag("sort_button")
                        )

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            FileSortOrder.entries.forEach { sortOption ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = sortOption.title,
                                            fontWeight = if (uiState.sortOrder == sortOption) FontWeight.Bold else FontWeight.Normal,
                                            color = if (uiState.sortOrder == sortOption) AccentBlue else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        viewModel.setSortOrder(sortOption)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            LiquidGlassFloatingActionButton(
                onClick = {
                    documentPickerLauncher.launch(
                        arrayOf(
                            "application/pdf",
                            "application/msword",
                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                            "application/vnd.ms-excel",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            "application/vnd.ms-powerpoint",
                            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                            "*/*"
                        )
                    )
                },
                contentDescription = "Open new file",
                modifier = Modifier
                    .padding(bottom = 80.dp)
                    .testTag("files_add_fab")
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input with Liquid Glass Search Pill
            com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassSearchPill(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = "Search files by name or format...",
                onClear = { viewModel.onSearchQueryChanged("") },
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("files_search_input")
            )

            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DocumentCategory.entries.forEach { category ->
                    val isSelected = uiState.selectedCategory == category
                    LiquidGlassFilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectCategory(category) },
                        label = category.displayName,
                        modifier = Modifier.testTag("category_chip_${category.name.lowercase()}")
                    )
                }
            }

            // Results count
            Text(
                text = "${uiState.totalCount} file${if (uiState.totalCount == 1) "" else "s"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )

            // Content List or Grid
            if (uiState.files.isEmpty()) {
                EmptyState(
                    title = if (uiState.searchQuery.isNotEmpty()) "No matching files" else "No files in this category",
                    subtitle = if (uiState.searchQuery.isNotEmpty()) "Try searching for a different name or format" else "Use the Storage Access Framework picker to add documents to NotOffice563.",
                    actionButtonText = "+ Pick Document",
                    onActionClick = {
                        documentPickerLauncher.launch(
                            arrayOf(
                                "application/pdf",
                                "application/msword",
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                "application/vnd.ms-excel",
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                "application/vnd.ms-powerpoint",
                                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                                "*/*"
                            )
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            } else if (uiState.viewMode == FileViewMode.LIST) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
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
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.files, key = { it.uri }) { file ->
                        DocumentGridItem(
                            file = file,
                            onClick = { onNavigateToViewer(file.uri) },
                            onFavoriteToggle = { viewModel.toggleFavorite(file) }
                        )
                    }
                }
            }
        }
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
