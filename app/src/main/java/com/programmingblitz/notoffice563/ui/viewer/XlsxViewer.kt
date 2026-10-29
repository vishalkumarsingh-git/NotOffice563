package com.programmingblitz.notoffice563.ui.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.programmingblitz.notoffice563.document.parser.XlsxParser
import com.programmingblitz.notoffice563.ui.theme.ExcelSheetContainer
import com.programmingblitz.notoffice563.ui.theme.ExcelSheetGreen
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassBadge
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassSegmentedControl
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassSurface
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassTokens

@Composable
fun XlsxViewer(
    state: ViewerContentState.XlsxContent,
    onSelectSheet: (Int) -> Unit,
    onSelectCell: (String, String) -> Unit,
    onCellValChange: ((String) -> Unit)? = null,
    onSaveOriginal: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val currentSheet = state.parsedSpreadsheet.sheets.getOrNull(state.currentSheetIndex)
        ?: state.parsedSpreadsheet.sheets.firstOrNull()

    val horizontalScrollState = rememberScrollState()

    // Active Formula Bar State
    var formulaInput by remember { mutableStateOf(state.selectedCellValue) }

    // Keep formula input in sync when cell selection changes
    remember(state.selectedCellRef, state.selectedCellValue) {
        formulaInput = state.selectedCellValue
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Top Liquid Glass Status Bar with Sheet Tabs
        LiquidGlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            shape = LiquidGlassTokens.ShapeMedium,
            isDark = isDark,
            elevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LiquidGlassBadge(
                            text = "EXCEL WORKBOOK",
                            color = ExcelSheetGreen,
                            containerColor = ExcelSheetGreen.copy(alpha = 0.18f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${state.parsedSpreadsheet.sheets.size} sheets • ${currentSheet?.maxColumns ?: 0} cols",
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = if (isDark) LiquidGlassTokens.DarkTextSecondary else LiquidGlassTokens.LightTextSecondary
                            )
                        )
                    }

                    if (onSaveOriginal != null) {
                        com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassButton(
                            text = "Save",
                            onClick = onSaveOriginal,
                            accentColor = ExcelSheetGreen,
                            height = 32.dp,
                            modifier = Modifier.testTag("xlsx_save_button")
                        )
                    }
                }

                if (state.parsedSpreadsheet.sheets.size > 1) {
                    Spacer(modifier = Modifier.height(10.dp))
                    LiquidGlassSegmentedControl(
                        items = state.parsedSpreadsheet.sheets.map { it.name },
                        selectedIndex = state.currentSheetIndex,
                        onSelectIndex = onSelectSheet,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Liquid Glass Formula & Cell Inspector Bar
        LiquidGlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp),
            shape = LiquidGlassTokens.ShapeCapsule,
            isDark = isDark,
            elevation = 3.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cell Coordinate Badge (e.g. A1, B4)
                Box(
                    modifier = Modifier
                        .clip(LiquidGlassTokens.ShapeCapsule)
                        .background(ExcelSheetGreen.copy(alpha = 0.2f))
                        .border(1.dp, ExcelSheetGreen.copy(alpha = 0.5f), LiquidGlassTokens.ShapeCapsule)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.selectedCellRef.ifEmpty { "A1" },
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ExcelSheetGreen
                        )
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Formula fx Icon
                Icon(
                    imageVector = Icons.Filled.Functions,
                    contentDescription = "Formula",
                    tint = ExcelSheetGreen,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Interactive Formula Bar Input
                BasicTextField(
                    value = formulaInput,
                    onValueChange = {
                        formulaInput = it
                        onSelectCell(state.selectedCellRef.ifEmpty { "A1" }, it)
                        onCellValChange?.invoke(it)
                    },
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDark) LiquidGlassTokens.DarkTextPrimary else LiquidGlassTokens.LightTextPrimary
                    ),
                    cursorBrush = SolidColor(ExcelSheetGreen),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        onSelectCell(state.selectedCellRef.ifEmpty { "A1" }, formulaInput)
                        onCellValChange?.invoke(formulaInput)
                    }),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Spreadsheet Grid (Real, Excel-style Grid Engine)
        if (currentSheet != null) {
            val cellWidth = 100.dp
            val rowHeaderWidth = 46.dp
            val rowHeight = 36.dp

            val gridBg = if (isDark) LiquidGlassTokens.DarkBaseBackground else Color(0xFFF8FAFC)
            val headerBg = if (isDark) Color(0xFF131A29) else Color(0xFFE2E8F0)
            val headerText = if (isDark) LiquidGlassTokens.DarkTextSecondary else Color(0xFF475569)
            val gridBorder = if (isDark) LiquidGlassTokens.DocumentGridBorderDark else LiquidGlassTokens.DocumentGridBorderLight
            val cellBg = if (isDark) LiquidGlassTokens.DocumentCanvasDark else LiquidGlassTokens.DocumentCanvasLight
            val cellText = if (isDark) LiquidGlassTokens.DarkTextPrimary else Color(0xFF0F172A)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(gridBg)
                    .border(1.dp, if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(horizontalScrollState)
                ) {
                    // Header Row (Corner + Column Letters A, B, C...)
                    Row(modifier = Modifier.background(headerBg)) {
                        Box(
                            modifier = Modifier
                                .width(rowHeaderWidth)
                                .height(rowHeight)
                                .border(0.5.dp, gridBorder),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "◩",
                                style = TextStyle(fontSize = 12.sp, color = headerText)
                            )
                        }

                        for (c in 0 until currentSheet.maxColumns) {
                            val colLetter = XlsxParser.indexToColumnLetter(c)
                            Box(
                                modifier = Modifier
                                    .width(cellWidth)
                                    .height(rowHeight)
                                    .border(0.5.dp, gridBorder)
                                    .background(headerBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = colLetter,
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = headerText
                                    )
                                )
                            }
                        }
                    }

                    // Grid Rows Body
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        itemsIndexed(currentSheet.rows) { rowIndex, rowList ->
                            Row(modifier = Modifier.height(rowHeight)) {
                                // Row Number Header
                                Box(
                                    modifier = Modifier
                                        .width(rowHeaderWidth)
                                        .height(rowHeight)
                                        .background(headerBg)
                                        .border(0.5.dp, gridBorder),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${rowIndex + 1}",
                                        style = TextStyle(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = headerText
                                        )
                                    )
                                }

                                // Cells in Row
                                for (colIndex in 0 until currentSheet.maxColumns) {
                                    val cellVal = rowList.getOrNull(colIndex) ?: ""
                                    val cellRef = "${XlsxParser.indexToColumnLetter(colIndex)}${rowIndex + 1}"
                                    val isSelected = state.selectedCellRef == cellRef

                                    Box(
                                        modifier = Modifier
                                            .width(cellWidth)
                                            .height(rowHeight)
                                            .background(
                                                if (isSelected) ExcelSheetGreen.copy(alpha = 0.15f) else cellBg
                                            )
                                            .border(
                                                width = if (isSelected) 2.dp else 0.5.dp,
                                                color = if (isSelected) ExcelSheetGreen else gridBorder
                                            )
                                            .clickable {
                                                onSelectCell(cellRef, cellVal)
                                            }
                                            .padding(horizontal = 8.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        val rawVal = if (isSelected && formulaInput != state.selectedCellValue) formulaInput else cellVal
                                        val displayVal = if (rawVal.startsWith("=")) {
                                            com.programmingblitz.notoffice563.document.evaluator.SpreadsheetFormulaEvaluator.evaluate(rawVal, currentSheet.rows)
                                        } else {
                                            rawVal
                                        }

                                        Text(
                                            text = displayVal,
                                            style = TextStyle(
                                                fontSize = 12.sp,
                                                color = if (isSelected) ExcelSheetGreen else cellText,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            ),
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
}
