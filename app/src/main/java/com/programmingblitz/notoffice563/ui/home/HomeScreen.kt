package com.programmingblitz.notoffice563.ui.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.programmingblitz.notoffice563.data.local.RecentFileEntity
import com.programmingblitz.notoffice563.document.saf.SafHelper
import com.programmingblitz.notoffice563.domain.model.DocumentCategory
import com.programmingblitz.notoffice563.domain.model.DocumentType
import com.programmingblitz.notoffice563.ui.components.DocumentGridItem
import com.programmingblitz.notoffice563.ui.components.DocumentListItem
import com.programmingblitz.notoffice563.ui.components.EmptyState
import com.programmingblitz.notoffice563.ui.components.FileInfoBottomSheet
import com.programmingblitz.notoffice563.ui.components.OfficeDocumentIcon
import com.programmingblitz.notoffice563.ui.components.StorageCard
import com.programmingblitz.notoffice563.ui.theme.AccentBlue
import com.programmingblitz.notoffice563.ui.theme.ExcelSheetGreen
import com.programmingblitz.notoffice563.ui.theme.PdfRubyRed
import com.programmingblitz.notoffice563.ui.theme.PptPresentationAmber
import com.programmingblitz.notoffice563.ui.theme.WordDocBlue
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassBadge
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassButton
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassCard
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassIconButton
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassSurface
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToViewer: (String) -> Unit,
    onNavigateToCategory: (DocumentCategory) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToRecent: () -> Unit,
    onOpenDrawer: (() -> Unit)? = null,
    onNavigateToProfile: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedFileForInfo by remember { mutableStateOf<RecentFileEntity?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showCreateSheet by remember { mutableStateOf(false) }
    val createSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
            // 1. Refined Top Bar: Identity, Search, Account
            item {
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            if (onOpenDrawer != null) {
                                LiquidGlassIconButton(
                                    onClick = onOpenDrawer,
                                    icon = Icons.Filled.Menu,
                                    contentDescription = "Open Drawer",
                                    size = 36.dp,
                                    modifier = Modifier.testTag("open_drawer_button")
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                            }

                            Text(
                                text = "NotOffice563",
                                style = TextStyle(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) LiquidGlassTokens.DarkTextPrimary else LiquidGlassTokens.LightTextPrimary
                                )
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LiquidGlassIconButton(
                                onClick = onNavigateToSearch,
                                icon = Icons.Filled.Search,
                                contentDescription = "Search",
                                size = 36.dp,
                                modifier = Modifier.testTag("home_search_button")
                            )

                            if (onNavigateToProfile != null) {
                                LiquidGlassIconButton(
                                    onClick = onNavigateToProfile,
                                    icon = Icons.Filled.Person,
                                    contentDescription = "Profile",
                                    size = 36.dp,
                                    modifier = Modifier.testTag("home_profile_button")
                                )
                            }
                        }
                    }
                }
            }

            // 2. Storage Capacity Overview Card
            item {
                Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
                    StorageCard(storageInfo = uiState.storageInfo)
                }
            }

            // 3. Prominent New File & Browse Actions
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LiquidGlassButton(
                        text = "New File",
                        onClick = { showCreateSheet = true },
                        icon = Icons.Filled.Add,
                        isPrimary = true,
                        accentColor = AccentBlue,
                        height = 46.dp,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("create_document_button")
                    )

                    LiquidGlassButton(
                        text = "Browse",
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
                        icon = Icons.Filled.FolderOpen,
                        isPrimary = false,
                        height = 46.dp,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("open_file_fab")
                    )
                }
            }

            // 4. Document Categories Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Document Categories",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) LiquidGlassTokens.DarkTextPrimary else LiquidGlassTokens.LightTextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CategoryTile(
                            title = "Word Documents",
                            count = uiState.documentCount,
                            documentType = DocumentType.WORD_DOCX,
                            onClick = { onNavigateToCategory(DocumentCategory.DOCUMENTS) },
                            modifier = Modifier.weight(1f)
                        )
                        CategoryTile(
                            title = "Spreadsheets",
                            count = uiState.spreadsheetCount,
                            documentType = DocumentType.EXCEL_XLSX,
                            onClick = { onNavigateToCategory(DocumentCategory.SPREADSHEETS) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CategoryTile(
                            title = "Presentations",
                            count = uiState.presentationCount,
                            documentType = DocumentType.PPT_PPTX,
                            onClick = { onNavigateToCategory(DocumentCategory.PRESENTATIONS) },
                            modifier = Modifier.weight(1f)
                        )
                        CategoryTile(
                            title = "PDF Documents",
                            count = uiState.pdfCount,
                            documentType = DocumentType.PDF,
                            onClick = { onNavigateToCategory(DocumentCategory.PDFS) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 5. Favorites Section
            if (uiState.favoriteFiles.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = PptPresentationAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Favorites",
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) LiquidGlassTokens.DarkTextPrimary else LiquidGlassTokens.LightTextPrimary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 14.dp),
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

            // 6. Recent Documents Section
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Documents",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) LiquidGlassTokens.DarkTextPrimary else LiquidGlassTokens.LightTextPrimary
                        )
                    )
                    if (uiState.recentFiles.isNotEmpty()) {
                        TextButton(onClick = onNavigateToRecent) {
                            Text(
                                text = "See all",
                                style = TextStyle(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentBlue
                                )
                            )
                        }
                    }
                }
            }

            if (uiState.recentFiles.isEmpty()) {
                item {
                    EmptyState(
                        title = "No documents in workspace",
                        subtitle = "Open or create Word, Excel, PowerPoint, or PDF files to start your productivity workspace.",
                        actionButtonText = "+ New File",
                        onActionClick = { showCreateSheet = true },
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(uiState.recentFiles, key = { "recent_${it.uri}" }) { file ->
                    Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
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

    if (showCreateSheet) {
        com.programmingblitz.notoffice563.ui.creation.CreateDocumentBottomSheet(
            sheetState = createSheetState,
            onDismiss = { showCreateSheet = false },
            onCreateDocument = { format, templateId, title ->
                viewModel.createDocument(context, format, templateId, title) { uriStr ->
                    showCreateSheet = false
                    onNavigateToViewer(uriStr)
                }
            }
        )
    }
}

@Composable
fun CategoryTile(
    title: String,
    count: Int,
    documentType: DocumentType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    LiquidGlassCard(
        modifier = modifier.testTag("category_card_${title.lowercase()}"),
        onClick = onClick,
        isDark = isDark
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OfficeDocumentIcon(
                documentType = documentType,
                size = 38.dp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) LiquidGlassTokens.DarkTextPrimary else LiquidGlassTokens.LightTextPrimary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$count file${if (count == 1) "" else "s"}",
                    style = TextStyle(
                        fontSize = 11.sp,
                        color = if (isDark) LiquidGlassTokens.DarkTextSecondary else LiquidGlassTokens.LightTextSecondary
                    )
                )
            }
        }
    }
}
