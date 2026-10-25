package com.example.ui.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.RecentFileEntity
import com.example.document.saf.SafHelper
import com.example.domain.model.DocumentCategory
import com.example.ui.components.DocumentGridItem
import com.example.ui.components.DocumentListItem
import com.example.ui.components.EmptyState
import com.example.ui.components.FileInfoBottomSheet
import com.example.ui.components.StorageCard
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.ExcelSheetContainer
import com.example.ui.theme.ExcelSheetContainerDark
import com.example.ui.theme.ExcelSheetGreen
import com.example.ui.theme.ExcelSheetGreenLight
import com.example.ui.theme.PdfRubyContainer
import com.example.ui.theme.PdfRubyContainerDark
import com.example.ui.theme.PdfRubyRed
import com.example.ui.theme.PdfRubyRedLight
import com.example.ui.theme.PptPresentationAmber
import com.example.ui.theme.PptPresentationAmberLight
import com.example.ui.theme.PptPresentationContainer
import com.example.ui.theme.PptPresentationContainerDark
import com.example.ui.theme.SleekShapes
import com.example.ui.theme.SleekSizes
import com.example.ui.theme.WordDocBlue
import com.example.ui.theme.WordDocBlueLight
import com.example.ui.theme.WordDocContainer
import com.example.ui.theme.WordDocContainerDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToViewer: (String) -> Unit,
    onNavigateToCategory: (DocumentCategory) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToRecent: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedFileForInfo by remember { mutableStateOf<RecentFileEntity?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.onDocumentPicked(context, uri, onNavigateToViewer)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refreshStorage()
        viewModel.messageEvents.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Header: NotOffice563 Branding & Search Bar
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = SleekShapes.chip,
                                    color = AccentBlue.copy(alpha = if (isDark) 0.22f else 0.12f),
                                    border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        text = "WORKSPACE",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentBlue,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "NotOffice563",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Independent Office & PDF Workspace",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = onNavigateToSearch,
                            modifier = Modifier
                                .testTag("home_search_button")
                                .size(SleekSizes.buttonSmall)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Universal Search",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Storage Overview Hero Card
                    StorageCard(storageInfo = uiState.storageInfo)

                    Spacer(modifier = Modifier.height(14.dp))

                    // Prominent Open Document Action
                    Button(
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
                        shape = SleekShapes.card,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentBlue,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(SleekSizes.buttonLarge)
                            .testTag("open_file_fab")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FileOpen,
                            contentDescription = null,
                            modifier = Modifier.size(SleekSizes.iconMedium)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Browse & Open Document",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Quick Access Categories
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Categories",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CategoryCard(
                            title = "Documents",
                            count = uiState.documentCount,
                            icon = Icons.Filled.Description,
                            color = if (isDark) WordDocBlueLight else WordDocBlue,
                            containerColor = if (isDark) WordDocContainerDark else WordDocContainer,
                            onClick = { onNavigateToCategory(DocumentCategory.DOCUMENTS) },
                            modifier = Modifier.weight(1f)
                        )
                        CategoryCard(
                            title = "Spreadsheets",
                            count = uiState.spreadsheetCount,
                            icon = Icons.Filled.TableChart,
                            color = if (isDark) ExcelSheetGreenLight else ExcelSheetGreen,
                            containerColor = if (isDark) ExcelSheetContainerDark else ExcelSheetContainer,
                            onClick = { onNavigateToCategory(DocumentCategory.SPREADSHEETS) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CategoryCard(
                            title = "Presentations",
                            count = uiState.presentationCount,
                            icon = Icons.Filled.Slideshow,
                            color = if (isDark) PptPresentationAmberLight else PptPresentationAmber,
                            containerColor = if (isDark) PptPresentationContainerDark else PptPresentationContainer,
                            onClick = { onNavigateToCategory(DocumentCategory.PRESENTATIONS) },
                            modifier = Modifier.weight(1f)
                        )
                        CategoryCard(
                            title = "PDFs",
                            count = uiState.pdfCount,
                            icon = Icons.Filled.PictureAsPdf,
                            color = if (isDark) PdfRubyRedLight else PdfRubyRed,
                            containerColor = if (isDark) PdfRubyContainerDark else PdfRubyContainer,
                            onClick = { onNavigateToCategory(DocumentCategory.PDFS) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Favorites Section (if present)
            if (uiState.favoriteFiles.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = PptPresentationAmber,
                                modifier = Modifier.size(SleekSizes.iconMedium)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Favorites",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.favoriteFiles, key = { "fav_${it.uri}" }) { file ->
                                DocumentGridItem(
                                    file = file,
                                    onClick = { onNavigateToViewer(file.uri) },
                                    onFavoriteToggle = { viewModel.toggleFavorite(file) },
                                    modifier = Modifier.width(155.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Recently Opened Files
            item {
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Documents",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (uiState.recentFiles.isNotEmpty()) {
                        TextButton(onClick = onNavigateToRecent) {
                            Text(
                                text = "See all",
                                color = AccentBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            if (uiState.recentFiles.isEmpty()) {
                item {
                    EmptyState(
                        title = "No documents in workspace",
                        subtitle = "Select any Word, Excel, PowerPoint, or PDF file on your device to start reading.",
                        actionButtonText = "+ Open File",
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
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(uiState.recentFiles, key = { "recent_${it.uri}" }) { file ->
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
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

@Composable
fun CategoryCard(
    title: String,
    count: Int,
    icon: ImageVector,
    color: Color,
    containerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .testTag("category_card_${title.lowercase()}")
            .clickable(onClick = onClick),
        shape = SleekShapes.card,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(SleekShapes.chip)
                    .background(containerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(SleekSizes.iconLarge)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$count file${if (count == 1) "" else "s"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
