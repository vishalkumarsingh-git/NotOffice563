package com.example.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// =========================================================================================
// Financer Pro Classic Blue Design Tokens & Palette
// =========================================================================================

// Primary Classic Blue Accent (Financer Pro Signature)
val AccentBlue = Color(0xFF0061A4)
val AccentBlueLight = Color(0xFF66B5E8)
val AccentBlueContainer = Color(0xFFD3E4FF)
val AccentBlueContainerDark = Color(0xFF1B3A55)
val OnAccentBlueContainer = Color(0xFF001D36)

// Slate Accents
val SlateNavyDark = Color(0xFF0B0D13)
val SlateNavy = Color(0xFF151822)
val SlateNavyElevated = Color(0xFF1D212E)
val SlateBlue = Color(0xFF5A6370)
val SlateBorderLight = Color(0xFFE4E7F0)
val SlateBorderDark = Color(0xFF242836)

// Precise tint mixing inspired by Financer Pro
fun blendAccent(primary: Color, base: Color, fraction: Float): Color =
    Color(
        red = (primary.red * fraction + base.red * (1f - fraction)).coerceIn(0f, 1f),
        green = (primary.green * fraction + base.green * (1f - fraction)).coerceIn(0f, 1f),
        blue = (primary.blue * fraction + base.blue * (1f - fraction)).coerceIn(0f, 1f),
        alpha = 1f
    )

// Light theme surfaces (Subtle blue-tinted cool background & pure white cards)
val BackgroundLight = blendAccent(AccentBlue, Color(0xFFF7F8FC), 0.04f)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceVariantLight = Color(0xFFF1F4F9)
val OutlineLight = Color(0xFFE4E7F0)
val OutlineVariantLight = Color(0xFFD8DDE8)
val TextPrimaryLight = Color(0xFF0F172A)
val TextSecondaryLight = Color(0xFF64748B)

// Dark theme surfaces (Deep slate background & elevated dark cards with restrained blue tint)
val BackgroundDark = blendAccent(AccentBlue, Color(0xFF0B0D13), 0.05f)
val SurfaceDark = blendAccent(AccentBlue, Color(0xFF151822), 0.07f)
val SurfaceVariantDark = blendAccent(AccentBlue, Color(0xFF1D212E), 0.09f)
val OutlineDark = Color(0xFF242836)
val OutlineVariantDark = Color(0xFF343A4A)
val TextPrimaryDark = Color(0xFFF8FAFC)
val TextSecondaryDark = Color(0xFF94A3B8)

// =========================================================================================
// Office Document Type Color Coding (Theme-Aware Light & Dark Variants)
// =========================================================================================

// Word Documents (.doc, .docx)
val WordDocBlue = Color(0xFF1D4ED8)
val WordDocBlueLight = Color(0xFF93C5FD)
val WordDocContainer = Color(0xFFEFF6FF)
val WordDocContainerDark = Color(0xFF172554)

// Excel Spreadsheets (.xls, .xlsx)
val ExcelSheetGreen = Color(0xFF059669)
val ExcelSheetGreenLight = Color(0xFF6EE7B7)
val ExcelSheetContainer = Color(0xFFECFDF5)
val ExcelSheetContainerDark = Color(0xFF064E3B)

// PowerPoint Presentations (.ppt, .pptx)
val PptPresentationAmber = Color(0xFFD97706)
val PptPresentationAmberLight = Color(0xFFFCD34D)
val PptPresentationContainer = Color(0xFFFFFBEB)
val PptPresentationContainerDark = Color(0xFF78350F)

// PDF Documents (.pdf)
val PdfRubyRed = Color(0xFFDC2626)
val PdfRubyRedLight = Color(0xFFFCA5A5)
val PdfRubyContainer = Color(0xFFFEF2F2)
val PdfRubyContainerDark = Color(0xFF7F1D1D)

// Semantic Alerts
val IncomeGreen = Color(0xFF16A34A)
val IncomeGreenContainer = Color(0xFFDCFCE7)
val IncomeGreenContainerDark = Color(0xFF064E3B)

val ExpenseRed = Color(0xFFDC2626)
val ExpenseRedContainer = Color(0xFFFEE2E2)
val ExpenseRedContainerDark = Color(0xFF7F1D1D)

val SavingGold = Color(0xFFD97706)
val WarningOrange = Color(0xFFD97706)
val InfoBlue = Color(0xFF0284C7)

// Hero Card Gradient (Financer Pro signature surface gradient)
fun getHeroCardGradient(isDark: Boolean): Brush {
    return if (isDark) {
        Brush.linearGradient(
            colors = listOf(SurfaceVariantDark, SurfaceDark),
            start = Offset(0f, 0f),
            end = Offset(0f, 600f)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color.White, SurfaceVariantLight),
            start = Offset(0f, 0f),
            end = Offset(0f, 600f)
        )
    }
}
