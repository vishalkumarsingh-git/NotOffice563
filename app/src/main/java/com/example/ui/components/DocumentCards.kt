package com.example.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.unit.sp
import com.example.data.local.RecentFileEntity
import com.example.domain.model.DocumentType
import com.example.ui.theme.ExcelSheetContainer
import com.example.ui.theme.ExcelSheetContainerDark
import com.example.ui.theme.ExcelSheetGreenLight
import com.example.ui.theme.ExcelSheetGreen
import com.example.ui.theme.PdfRubyContainer
import com.example.ui.theme.PdfRubyContainerDark
import com.example.ui.theme.PdfRubyRedLight
import com.example.ui.theme.PdfRubyRed
import com.example.ui.theme.PptPresentationAmber
import com.example.ui.theme.PptPresentationAmberLight
import com.example.ui.theme.PptPresentationContainer
import com.example.ui.theme.PptPresentationContainerDark
import com.example.ui.theme.WordDocBlue
import com.example.ui.theme.WordDocBlueLight
import com.example.ui.theme.WordDocContainer
import com.example.ui.theme.WordDocContainerDark
import com.example.util.FileUtils

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
            iconColor = WordDocBlue,
            containerColor = WordDocContainer,
            badgeLabel = "DOCX"
        )
        DocumentType.WORD_DOC -> DocumentTypeAppearance(
            icon = Icons.Filled.Description,
            iconColor = WordDocBlue,
            containerColor = WordDocContainer,
            badgeLabel = "DOC"
        )
        DocumentType.EXCEL_XLSX -> DocumentTypeAppearance(
            icon = Icons.Filled.TableChart,
            iconColor = ExcelSheetGreen,
            containerColor = ExcelSheetContainer,
            badgeLabel = "XLSX"
        )
        DocumentType.EXCEL_XLS -> DocumentTypeAppearance(
            icon = Icons.Filled.TableChart,
            iconColor = ExcelSheetGreen,
            containerColor = ExcelSheetContainer,
            badgeLabel = "XLS"
        )
        DocumentType.PPT_PPTX -> DocumentTypeAppearance(
            icon = Icons.Filled.Slideshow,
            iconColor = PptPresentationAmber,
            containerColor = PptPresentationContainer,
            badgeLabel = "PPTX"
        )
        DocumentType.PPT_PPT -> DocumentTypeAppearance(
            icon = Icons.Filled.Slideshow,
            iconColor = PptPresentationAmber,
            containerColor = PptPresentationContainer,
            badgeLabel = "PPT"
        )
        DocumentType.PDF -> DocumentTypeAppearance(
            icon = Icons.Filled.PictureAsPdf,
            iconColor = PdfRubyRed,
            containerColor = PdfRubyContainer,
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Distinct format icon container
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(appearance.containerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = appearance.icon,
                    contentDescription = appearance.badgeLabel,
                    tint = appearance.iconColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // File Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = file.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = appearance.containerColor
                    ) {
                        Text(
                            text = appearance.badgeLabel,
                            color = appearance.iconColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }

                    if (file.fileSize > 0) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = FileUtils.formatFileSize(file.fileSize),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    val dateText = FileUtils.formatDateShort(file.lastOpened)
                    if (dateText.isNotEmpty()) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = dateText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Favorite button
            IconButton(
                onClick = onFavoriteToggle,
                modifier = Modifier.testTag("favorite_button_${file.uri.hashCode()}")
            ) {
                Icon(
                    imageVector = if (file.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                    contentDescription = if (file.isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (file.isFavorite) PptPresentationAmber else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Context Menu
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.testTag("more_button_${file.uri.hashCode()}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "More actions",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("File Information") },
                        onClick = {
                            showMenu = false
                            onInfoClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Share") },
                        onClick = {
                            showMenu = false
                            onShareClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Open with another app") },
                        onClick = {
                            showMenu = false
                            onOpenWithClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Remove from recent") },
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
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
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
                    shape = RoundedCornerShape(4.dp),
                    color = appearance.containerColor
                ) {
                    Text(
                        text = appearance.badgeLabel,
                        color = appearance.iconColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
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
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(appearance.containerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = appearance.icon,
                    contentDescription = null,
                    tint = appearance.iconColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = file.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (file.fileSize > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = FileUtils.formatFileSize(file.fileSize),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
