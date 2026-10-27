package com.programmingblitz.notoffice563.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.programmingblitz.notoffice563.data.local.RecentFileEntity
import com.programmingblitz.notoffice563.domain.model.DocumentType
import com.programmingblitz.notoffice563.ui.theme.ExcelSheetContainer
import com.programmingblitz.notoffice563.ui.theme.ExcelSheetContainerDark
import com.programmingblitz.notoffice563.ui.theme.ExcelSheetGreen
import com.programmingblitz.notoffice563.ui.theme.ExcelSheetGreenLight
import com.programmingblitz.notoffice563.ui.theme.PdfRubyContainer
import com.programmingblitz.notoffice563.ui.theme.PdfRubyContainerDark
import com.programmingblitz.notoffice563.ui.theme.PdfRubyRed
import com.programmingblitz.notoffice563.ui.theme.PdfRubyRedLight
import com.programmingblitz.notoffice563.ui.theme.PptPresentationAmber
import com.programmingblitz.notoffice563.ui.theme.PptPresentationAmberLight
import com.programmingblitz.notoffice563.ui.theme.PptPresentationContainer
import com.programmingblitz.notoffice563.ui.theme.PptPresentationContainerDark
import com.programmingblitz.notoffice563.ui.theme.SleekShapes
import com.programmingblitz.notoffice563.ui.theme.SleekSizes
import com.programmingblitz.notoffice563.ui.theme.WordDocBlue
import com.programmingblitz.notoffice563.ui.theme.WordDocBlueLight
import com.programmingblitz.notoffice563.ui.theme.WordDocContainer
import com.programmingblitz.notoffice563.ui.theme.WordDocContainerDark
import com.programmingblitz.notoffice563.ui.theme.motion.MotionTokens
import com.programmingblitz.notoffice563.ui.theme.motion.liquidPressable
import com.programmingblitz.notoffice563.util.FileUtils
import com.programmingblitz.notoffice563.util.HapticType
import com.programmingblitz.notoffice563.util.HapticUtil

data class DocumentTypeAppearance(
    val icon: ImageVector,
    val iconColor: Color,
    val containerColor: Color,
    val badgeLabel: String
)

@Composable
fun getDocumentAppearance(typeString: String): DocumentTypeAppearance {
    val type = runCatching { DocumentType.valueOf(typeString) }.getOrDefault(DocumentType.UNKNOWN)
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    return when (type) {
        DocumentType.WORD_DOCX -> DocumentTypeAppearance(
            icon = Icons.Filled.Description,
            iconColor = if (isDark) WordDocBlueLight else WordDocBlue,
            containerColor = if (isDark) WordDocContainerDark else WordDocContainer,
            badgeLabel = "DOCX"
        )
        DocumentType.WORD_DOC -> DocumentTypeAppearance(
            icon = Icons.Filled.Description,
            iconColor = if (isDark) WordDocBlueLight else WordDocBlue,
            containerColor = if (isDark) WordDocContainerDark else WordDocContainer,
            badgeLabel = "DOC"
        )
        DocumentType.EXCEL_XLSX -> DocumentTypeAppearance(
            icon = Icons.Filled.TableChart,
            iconColor = if (isDark) ExcelSheetGreenLight else ExcelSheetGreen,
            containerColor = if (isDark) ExcelSheetContainerDark else ExcelSheetContainer,
            badgeLabel = "XLSX"
        )
        DocumentType.EXCEL_XLS -> DocumentTypeAppearance(
            icon = Icons.Filled.TableChart,
            iconColor = if (isDark) ExcelSheetGreenLight else ExcelSheetGreen,
            containerColor = if (isDark) ExcelSheetContainerDark else ExcelSheetContainer,
            badgeLabel = "XLS"
        )
        DocumentType.PPT_PPTX -> DocumentTypeAppearance(
            icon = Icons.Filled.Slideshow,
            iconColor = if (isDark) PptPresentationAmberLight else PptPresentationAmber,
            containerColor = if (isDark) PptPresentationContainerDark else PptPresentationContainer,
            badgeLabel = "PPTX"
        )
        DocumentType.PPT_PPT -> DocumentTypeAppearance(
            icon = Icons.Filled.Slideshow,
            iconColor = if (isDark) PptPresentationAmberLight else PptPresentationAmber,
            containerColor = if (isDark) PptPresentationContainerDark else PptPresentationContainer,
            badgeLabel = "PPT"
        )
        DocumentType.PDF -> DocumentTypeAppearance(
            icon = Icons.Filled.PictureAsPdf,
            iconColor = if (isDark) PdfRubyRedLight else PdfRubyRed,
            containerColor = if (isDark) PdfRubyContainerDark else PdfRubyContainer,
            badgeLabel = "PDF"
        )
        DocumentType.UNKNOWN -> DocumentTypeAppearance(
            icon = Icons.Filled.Description,
            iconColor = MaterialTheme.colorScheme.primary,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            badgeLabel = "FILE"
        )
    }
}

/**
 * Redesigned Document Row Card:
 * 12dp card shape, 1dp subtle border, elevated surface background,
 * clean typography hierarchy and 48dp action touch targets.
 */
@Composable
fun DocumentListItem(
    file: RecentFileEntity,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onInfoClick: () -> Unit,
    onShareClick: () -> Unit,
    onOpenWithClick: () -> Unit,
    onRemoveRecentClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appearance = getDocumentAppearance(file.fileType)
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("document_item_${file.uri.hashCode()}")
            .clickable(onClick = onClick),
        shape = SleekShapes.card,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Microsoft 365-inspired Document Icon
            val docType = runCatching { DocumentType.valueOf(file.fileType) }.getOrDefault(DocumentType.UNKNOWN)
            OfficeDocumentIcon(
                documentType = docType,
                size = 44.dp
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Document Title & Metadata
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Badge chip
                    Surface(
                        shape = SleekShapes.chip,
                        color = appearance.containerColor.copy(alpha = 0.8f)
                    ) {
                        Text(
                            text = appearance.badgeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = appearance.iconColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "${FileUtils.formatFileSize(file.fileSize)} • ${FileUtils.formatRelativeTime(file.lastOpened)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Favorite button
            IconButton(
                onClick = onFavoriteToggle,
                modifier = Modifier
                    .size(SleekSizes.buttonSmall)
                    .testTag("favorite_button_${file.uri.hashCode()}")
            ) {
                Icon(
                    imageVector = if (file.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                    contentDescription = if (file.isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (file.isFavorite) PptPresentationAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(SleekSizes.iconMedium)
                )
            }

            // Context Menu
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier
                        .size(SleekSizes.buttonSmall)
                        .testTag("more_button_${file.uri.hashCode()}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "More actions",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(SleekSizes.iconMedium)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Details & Notes") },
                        leadingIcon = { Icon(Icons.Filled.Info, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onInfoClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Share") },
                        leadingIcon = { Icon(Icons.Filled.Share, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onShareClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Open with another app") },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onOpenWithClick()
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Remove from recent", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        onClick = {
                            showMenu = false
                            onRemoveRecentClick()
                        }
                    )
                }
            }
        }
    }
}

/**
 * Grid Item Card for Browse and Favorites sections:
 * 12dp card with 1dp border, centered document type icon and clean typography.
 */
@Composable
fun DocumentGridItem(
    file: RecentFileEntity,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appearance = getDocumentAppearance(file.fileType)

    Card(
        modifier = modifier
            .testTag("document_grid_item_${file.uri.hashCode()}")
            .liquidPressable(
                pressedScale = MotionTokens.ScaleCardPressed,
                hapticType = HapticType.SELECTION,
                onClick = onClick
            ),
        shape = SleekShapes.card,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = SleekShapes.chip,
                    color = appearance.containerColor
                ) {
                    Text(
                        text = appearance.badgeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = appearance.iconColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (file.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (file.isFavorite) PptPresentationAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Centered Microsoft 365-inspired icon
            val docTypeGrid = runCatching { DocumentType.valueOf(file.fileType) }.getOrDefault(DocumentType.UNKNOWN)
            OfficeDocumentIcon(
                documentType = docTypeGrid,
                size = 54.dp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = file.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = FileUtils.formatFileSize(file.fileSize),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
