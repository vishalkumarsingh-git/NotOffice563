package com.programmingblitz.notoffice563.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassButton
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassDialog
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassIconButton
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassTokens
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.programmingblitz.notoffice563.data.local.RecentFileEntity
import com.programmingblitz.notoffice563.domain.model.DocumentType
import com.programmingblitz.notoffice563.ui.theme.AccentBlue
import com.programmingblitz.notoffice563.ui.theme.ExpenseRed
import com.programmingblitz.notoffice563.ui.theme.PptPresentationAmber
import com.programmingblitz.notoffice563.ui.theme.SleekShapes
import com.programmingblitz.notoffice563.ui.theme.SleekSizes
import com.programmingblitz.notoffice563.util.FileUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileInfoBottomSheet(
    file: RecentFileEntity?,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onOpen: (RecentFileEntity) -> Unit,
    onRename: (RecentFileEntity, String) -> Unit,
    onDelete: (RecentFileEntity) -> Unit,
    onShare: (RecentFileEntity) -> Unit,
    onOpenWith: (RecentFileEntity) -> Unit,
    onToggleFavorite: (RecentFileEntity) -> Unit,
    onRemoveRecent: (RecentFileEntity) -> Unit
) {
    if (file == null) return

    val appearance = getDocumentAppearance(file.fileType)
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var renameInput by remember(file) { mutableStateOf(file.displayName) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = SleekShapes.sheet,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header: Icon + Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(SleekShapes.card)
                        .background(appearance.containerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = appearance.icon,
                        contentDescription = null,
                        tint = appearance.iconColor,
                        modifier = Modifier.size(SleekSizes.iconLarge)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = file.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = runCatching { DocumentType.valueOf(file.fileType).friendlyName }.getOrDefault(file.fileType),
                        style = MaterialTheme.typography.bodySmall,
                        color = appearance.iconColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                LiquidGlassIconButton(
                    onClick = { onToggleFavorite(file) },
                    icon = if (file.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                    contentDescription = "Toggle favorite",
                    isActive = file.isFavorite,
                    activeColor = PptPresentationAmber,
                    size = 36.dp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Primary action button: Open
            LiquidGlassButton(
                text = "Open Document",
                icon = Icons.AutoMirrored.Filled.OpenInNew,
                onClick = {
                    onDismiss()
                    onOpen(file)
                },
                isPrimary = true,
                accentColor = LiquidGlassTokens.AzureGlass,
                height = 46.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("open_file_action_button")
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "File Metadata",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = AccentBlue
            )

            Spacer(modifier = Modifier.height(12.dp))

            MetadataRow("Extension", ".${file.extension}")
            MetadataRow("MIME Type", file.mimeType)
            MetadataRow("File Size", if (file.fileSize > 0) FileUtils.formatFileSize(file.fileSize) else "Unknown (SAF Stream)")
            MetadataRow("Last Opened", FileUtils.formatDate(file.lastOpened))
            if (file.lastPosition != null) {
                MetadataRow("Last Position", file.lastPosition)
            }
            MetadataRow("Location / URI", file.uri, isMonospace = true)

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Actions",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LiquidGlassButton(
                    text = "Share",
                    icon = Icons.Filled.Share,
                    onClick = { onShare(file) },
                    isPrimary = false,
                    height = 40.dp,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassButton(
                    text = "Open with",
                    icon = Icons.Filled.OpenInBrowser,
                    onClick = { onOpenWith(file) },
                    isPrimary = false,
                    height = 40.dp,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LiquidGlassButton(
                    text = "Rename",
                    icon = Icons.Filled.DriveFileRenameOutline,
                    onClick = { showRenameDialog = true },
                    isPrimary = false,
                    height = 40.dp,
                    modifier = Modifier.weight(1f)
                )

                LiquidGlassButton(
                    text = "Remove Recent",
                    onClick = {
                        onDismiss()
                        onRemoveRecent(file)
                    },
                    isPrimary = false,
                    height = 40.dp,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LiquidGlassButton(
                text = "Delete from Device Storage",
                icon = Icons.Filled.Delete,
                onClick = { showDeleteConfirmDialog = true },
                accentColor = LiquidGlassTokens.CrimsonGlass,
                isPrimary = true,
                height = 42.dp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showRenameDialog) {
        LiquidGlassDialog(
            onDismissRequest = { showRenameDialog = false },
            title = "Rename Document",
            confirmButton = {
                LiquidGlassButton(
                    text = "Save",
                    onClick = {
                        if (renameInput.isNotBlank()) {
                            onRename(file, renameInput.trim())
                            showRenameDialog = false
                        }
                    },
                    accentColor = LiquidGlassTokens.AzureGlass,
                    isPrimary = true,
                    height = 38.dp
                )
            },
            dismissButton = {
                LiquidGlassButton(
                    text = "Cancel",
                    onClick = { showRenameDialog = false },
                    isPrimary = false,
                    height = 38.dp
                )
            }
        ) {
            Column {
                Text(
                    text = "Enter a new display name for this file:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    singleLine = true,
                    shape = SleekShapes.input,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showDeleteConfirmDialog) {
        LiquidGlassDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = "Delete File from Device?",
            confirmButton = {
                LiquidGlassButton(
                    text = "Delete Permanently",
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDismiss()
                        onDelete(file)
                    },
                    accentColor = LiquidGlassTokens.CrimsonGlass,
                    isPrimary = true,
                    height = 38.dp
                )
            },
            dismissButton = {
                LiquidGlassButton(
                    text = "Cancel",
                    onClick = { showDeleteConfirmDialog = false },
                    isPrimary = false,
                    height = 38.dp
                )
            }
        ) {
            Text(
                text = "Are you sure you want to delete '${file.displayName}' permanently from your device? This cannot be undone.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MetadataRow(label: String, value: String, isMonospace: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(110.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = if (isMonospace) 2 else 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}
