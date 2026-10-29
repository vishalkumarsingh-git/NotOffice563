package com.programmingblitz.notoffice563.ui.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.programmingblitz.notoffice563.document.evaluator.SpreadsheetFormulaEvaluator
import com.programmingblitz.notoffice563.document.parser.XlsxParser
import com.programmingblitz.notoffice563.ui.theme.ExcelSheetGreen
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassBadge
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassIconButton
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassSegmentedControl
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassSurface
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassTokens

/**
 * High-performance virtualized 2D spreadsheet grid inspired by Google Sheets & Microsoft Excel.
 *
 * Architecture Highlights:
 * 1. CPU-Only Visible Windowing: Only cells visible in the viewport slice are created and composed on CPU.
 * 2. Minecraft-Style Infinite Generation: Horizontally expands columns (A..Z, AA..ZZ...) and vertically expands rows
 *    on demand as user scrolls toward edges.
 * 3. Frozen Panes: Column letters (A, B, C...) are pinned at top; Row numbers (1, 2, 3...) are pinned at left;
 *    Top-left corner box (◩) is fixed at (0, 0).
 * 4. Formula Bar: Shows active formula or text, cell reference badge, and commitment checkmark.
 */
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

    // Active Formula Bar State
    var formulaInput by remember { mutableStateOf(state.selectedCellValue) }

    // Keep formula input in sync when cell selection changes
    remember(state.selectedCellRef, state.selectedCellValue) {
        formulaInput = state.selectedCellValue
    }

    // Scroll states for 2D spreadsheet navigation
    val horizontalScrollState = rememberScrollState()
    val lazyListState = rememberLazyListState()

    Column(modifier = modifier.fillMaxSize()) {
        // 1. Top Liquid Glass Status Bar with Sheet Tabs
        LiquidGlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
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
                            text = "${currentSheet?.name ?: "Sheet1"} • Infinite Grid • ${state.parsedSpreadsheet.sheets.size} sheet${if (state.parsedSpreadsheet.sheets.size == 1) "" else "s"}",
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
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

        // 2. Liquid Glass Formula & Cell Inspector Bar (with Commit Checkmark)
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
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cell Coordinate Badge (e.g. A1, B4, Z100)
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

                Spacer(modifier = Modifier.width(8.dp))

                // Formula fx Icon
                Icon(
                    imageVector = Icons.Filled.Functions,
                    contentDescription = "Formula",
                    tint = ExcelSheetGreen,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Interactive Formula Bar Input
                Box(modifier = Modifier.weight(1f)) {
                    if (formulaInput.isEmpty()) {
                        Text(
                            text = "Enter text or =formula...",
                            style = TextStyle(
                                fontSize = 13.sp,
                                color = if (isDark) LiquidGlassTokens.DarkTextTertiary else LiquidGlassTokens.LightTextTertiary
                            )
                        )
                    }
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
                        modifier = Modifier.fillMaxWidth().testTag("spreadsheet_formula_input")
                    )
                }

                // Quick Commit Checkmark Button
                if (formulaInput.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    LiquidGlassIconButton(
                        onClick = {
                            onSelectCell(state.selectedCellRef.ifEmpty { "A1" }, formulaInput)
                            onCellValChange?.invoke(formulaInput)
                        },
                        icon = Icons.Filled.Check,
                        contentDescription = "Commit cell value",
                        isActive = true,
                        activeColor = ExcelSheetGreen,
                        size = 30.dp,
                        modifier = Modifier.testTag("commit_formula_button")
                    )
                }
            }
        }

        // 3. High-Performance Virtualized 2D Grid with Frozen Column & Row Headers
        val cellWidth = 92.dp
        val rowHeight = 34.dp
        val rowHeaderWidth = 44.dp
        val headerRowHeight = 30.dp

        val gridBg = if (isDark) LiquidGlassTokens.DarkBaseBackground else Color(0xFFF8FAFC)
        val headerBg = if (isDark) Color(0xFF131A29) else Color(0xFFE8EEF5)
        val headerText = if (isDark) LiquidGlassTokens.DarkTextSecondary else Color(0xFF475569)
        val gridBorder = if (isDark) LiquidGlassTokens.DocumentGridBorderDark else LiquidGlassTokens.DocumentGridBorderLight
        val cellBg = if (isDark) LiquidGlassTokens.DocumentCanvasDark else LiquidGlassTokens.DocumentCanvasLight
        val cellText = if (isDark) LiquidGlassTokens.DarkTextPrimary else Color(0xFF0F172A)

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(gridBg)
                .border(
                    1.dp,
                    if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f),
                    RoundedCornerShape(12.dp)
                )
        ) {
            val density = LocalDensity.current
            val cellWidthPx = with(density) { cellWidth.toPx() }
            val rowHeightPx = with(density) { rowHeight.toPx() }
            val availableWidthPx = with(density) { (maxWidth - rowHeaderWidth).toPx().coerceAtLeast(100f) }
            val availableHeightPx = with(density) { (maxHeight - headerRowHeight).toPx().coerceAtLeast(100f) }

            // Dynamic virtual dimensions (infinite generation on scroll)
            val sheetColCount = currentSheet?.maxColumns ?: 1
            val sheetRowCount = currentSheet?.rows?.size ?: 1
            var virtualColumns by remember(currentSheet) { mutableIntStateOf(maxOf(26, sheetColCount + 10)) }
            var virtualRows by remember(currentSheet) { mutableIntStateOf(maxOf(100, sheetRowCount + 50)) }

            // Horizontal windowing: compute exact visible column slice
            val scrollX = horizontalScrollState.value
            val firstVisibleCol = (scrollX / cellWidthPx).toInt().coerceAtLeast(0)
            val startCol = (firstVisibleCol - 1).coerceAtLeast(0)
            val visibleColCount = (availableWidthPx / cellWidthPx).toInt() + 3
            val endCol = (startCol + visibleColCount).coerceAtMost(virtualColumns)

            // Minecraft-style infinite generation: expand columns as user approaches right border
            LaunchedEffect(endCol, virtualColumns) {
                if (endCol >= virtualColumns - 5) {
                    virtualColumns += 26
                }
            }

            // Vertical windowing: expand rows as user approaches bottom border
            val firstVisibleRow = lazyListState.firstVisibleItemIndex
            LaunchedEffect(firstVisibleRow, virtualRows) {
                if (firstVisibleRow + 25 >= virtualRows) {
                    virtualRows += 100
                }
            }

            // Left and right spacers to ensure native scrollbar and total content width are 100% accurate
            val leftSpacerWidthDp = with(density) { (startCol * cellWidthPx).toDp() }
            val rightSpacerWidthDp = with(density) { ((virtualColumns - endCol).coerceAtLeast(0) * cellWidthPx).toDp() }

            Box(modifier = Modifier.fillMaxSize()) {
                // A. MAIN CELL GRID (scrolls vertically via LazyColumn and horizontally via horizontalScroll)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = rowHeaderWidth, top = headerRowHeight)
                ) {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier
                            .fillMaxSize()
                            .horizontalScroll(horizontalScrollState)
                    ) {
                        items(
                            count = virtualRows,
                            key = { r -> "grid_r_$r" }
                        ) { rowIndex ->
                            Row(
                                modifier = Modifier
                                    .height(rowHeight)
                                    .fillMaxWidth()
                            ) {
                                if (leftSpacerWidthDp > 0.dp) {
                                    Spacer(modifier = Modifier.width(leftSpacerWidthDp))
                                }

                                // Compose ONLY visible columns in this row
                                for (c in startCol until endCol) {
                                    val colLetter = XlsxParser.indexToColumnLetter(c)
                                    val cellRef = "$colLetter${rowIndex + 1}"
                                    val cellVal = currentSheet?.rows?.getOrNull(rowIndex)?.getOrNull(c) ?: ""
                                    val isSelected = state.selectedCellRef == cellRef

                                    val rawVal = if (isSelected && formulaInput != state.selectedCellValue) formulaInput else cellVal
                                    val displayVal = if (rawVal.startsWith("=")) {
                                        SpreadsheetFormulaEvaluator.evaluate(rawVal, currentSheet?.rows ?: emptyList())
                                    } else {
                                        rawVal
                                    }

                                    val isNumber = displayVal.toDoubleOrNull() != null

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
                                            .padding(horizontal = 6.dp),
                                        contentAlignment = if (isNumber) Alignment.CenterEnd else Alignment.CenterStart
                                    ) {
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

                                        // Excel corner selection dot
                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomEnd)
                                                    .size(5.dp)
                                                    .background(ExcelSheetGreen)
                                            )
                                        }
                                    }
                                }

                                if (rightSpacerWidthDp > 0.dp) {
                                    Spacer(modifier = Modifier.width(rightSpacerWidthDp))
                                }
                            }
                        }
                    }
                }

                // B. PINNED COLUMN HEADERS (Top pane - frozen vertically, scrolls horizontally)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .fillMaxWidth()
                        .height(headerRowHeight)
                        .background(headerBg)
                        .border(0.5.dp, gridBorder)
                ) {
                    // Pinned Top-Left Corner Box
                    Box(
                        modifier = Modifier
                            .width(rowHeaderWidth)
                            .fillMaxHeight()
                            .background(headerBg)
                            .border(0.5.dp, gridBorder),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "◩",
                            style = TextStyle(fontSize = 11.sp, color = headerText)
                        )
                    }

                    // Horizontally scrolling Column Letters
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .horizontalScroll(horizontalScrollState)
                    ) {
                        if (leftSpacerWidthDp > 0.dp) {
                            Spacer(modifier = Modifier.width(leftSpacerWidthDp))
                        }

                        for (c in startCol until endCol) {
                            val colLetter = XlsxParser.indexToColumnLetter(c)
                            val isColSelected = state.selectedCellRef.startsWith(colLetter) &&
                                    state.selectedCellRef.drop(colLetter.length).all { it.isDigit() }

                            Box(
                                modifier = Modifier
                                    .width(cellWidth)
                                    .fillMaxHeight()
                                    .background(if (isColSelected) ExcelSheetGreen.copy(alpha = 0.15f) else headerBg)
                                    .border(0.5.dp, gridBorder),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = colLetter,
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        fontWeight = if (isColSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        color = if (isColSelected) ExcelSheetGreen else headerText
                                    )
                                )
                            }
                        }

                        if (rightSpacerWidthDp > 0.dp) {
                            Spacer(modifier = Modifier.width(rightSpacerWidthDp))
                        }
                    }
                }

                // C. PINNED ROW HEADERS (Left pane - frozen horizontally, hardware-synchronized vertically)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = headerRowHeight)
                        .width(rowHeaderWidth)
                        .fillMaxHeight()
                        .clipToBounds()
                        .background(headerBg)
                ) {
                    val visibleRowCount = (availableHeightPx / rowHeightPx).toInt() + 3
                    val lastRowIndex = (firstVisibleRow + visibleRowCount).coerceAtMost(virtualRows)
                    val rowScrollOffsetPx = lazyListState.firstVisibleItemScrollOffset

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                translationY = -rowScrollOffsetPx.toFloat()
                            }
                    ) {
                        for (r in firstVisibleRow until lastRowIndex) {
                            val rowNum = r + 1
                            val isRowSelected = state.selectedCellRef.endsWith("$rowNum") &&
                                    state.selectedCellRef.dropLast("$rowNum".length).all { it.isLetter() }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(rowHeight)
                                    .background(if (isRowSelected) ExcelSheetGreen.copy(alpha = 0.15f) else headerBg)
                                    .border(0.5.dp, gridBorder),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$rowNum",
                                    style = TextStyle(
                                        fontSize = 11.sp,
                                        fontWeight = if (isRowSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isRowSelected) ExcelSheetGreen else headerText
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
