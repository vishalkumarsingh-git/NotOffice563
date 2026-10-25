package com.example.ui.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.document.parser.XlsxParser
import com.example.ui.theme.ExcelSheetContainer
import com.example.ui.theme.ExcelSheetGreen

@Composable
fun XlsxViewer(
    state: ViewerContentState.XlsxContent,
    onSelectSheet: (Int) -> Unit,
    onSelectCell: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentSheet = state.parsedSpreadsheet.sheets.getOrNull(state.currentSheetIndex)
        ?: state.parsedSpreadsheet.sheets.firstOrNull()

    val horizontalScrollState = rememberScrollState()

    Column(modifier = modifier.fillMaxSize()) {
        // Sheet Tabs Selector
        if (state.parsedSpreadsheet.sheets.size > 1) {
            ScrollableTabRow(
                selectedTabIndex = state.currentSheetIndex,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                state.parsedSpreadsheet.sheets.forEachIndexed { index, sheet ->
                    Tab(
                        selected = state.currentSheetIndex == index,
                        onClick = { onSelectSheet(index) },
                        text = {
                            Text(
                                text = sheet.name,
                                fontWeight = if (state.currentSheetIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
        }

        // Cell Inspector / Formula Bar
        Surface(
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = ExcelSheetContainer
                ) {
                    Text(
                        text = state.selectedCellRef.ifEmpty { "A1" },
                        color = ExcelSheetGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = state.selectedCellValue.ifEmpty { "fx (empty)" },
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (state.selectedCellValue.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Notice banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ExcelSheetContainer)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Info, contentDescription = null, tint = ExcelSheetGreen, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Scroll horizontally and vertically to browse sheet cells. Tap any cell to inspect.",
                fontSize = 11.sp,
                color = ExcelSheetGreen
            )
        }

        // Spreadsheet Grid
        if (currentSheet != null) {
            val cellWidth = 100.dp
            val rowHeaderWidth = 46.dp
            val rowHeight = 36.dp

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .horizontalScroll(horizontalScrollState)
            ) {
                // Column Header Row: (corner + A, B, C...)
                Row(
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    // Top-left corner
                    Box(
                        modifier = Modifier
                            .width(rowHeaderWidth)
                            .height(rowHeight)
                            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("", fontSize = 11.sp)
                    }

                    // Column letters
                    for (c in 0 until currentSheet.maxColumns) {
                        val colLetter = XlsxParser.indexToColumnLetter(c)
                        Box(
                            modifier = Modifier
                                .width(cellWidth)
                                .height(rowHeight)
                                .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = colLetter,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Grid Body
                LazyColumn(modifier = Modifier.weight(1f)) {
                    itemsIndexed(currentSheet.rows) { rowIndex, rowList ->
                        Row(modifier = Modifier.height(rowHeight)) {
                            // Row number
                            Box(
                                modifier = Modifier
                                    .width(rowHeaderWidth)
                                    .height(rowHeight)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${rowIndex + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Row cells
                            for (colIndex in 0 until currentSheet.maxColumns) {
                                val cellVal = rowList.getOrNull(colIndex) ?: ""
                                val cellRef = "${XlsxParser.indexToColumnLetter(colIndex)}${rowIndex + 1}"
                                val isSelected = state.selectedCellRef == cellRef

                                Box(
                                    modifier = Modifier
                                        .width(cellWidth)
                                        .height(rowHeight)
                                        .background(
                                            if (isSelected) ExcelSheetContainer else MaterialTheme.colorScheme.surface
                                        )
                                        .border(
                                            width = if (isSelected) 1.5.dp else 0.5.dp,
                                            color = if (isSelected) ExcelSheetGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                        )
                                        .clickable { onSelectCell(cellRef, cellVal) }
                                        .padding(horizontal = 6.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = cellVal,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
