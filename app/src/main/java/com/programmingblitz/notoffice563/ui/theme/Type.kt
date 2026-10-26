package com.programmingblitz.notoffice563.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// =========================================================================================
// NotOffice563 Design System Tokens: Typography, Sizing, Shapes & Spacing
// =========================================================================================

private val AppFont = FontFamily.SansSerif

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = AppFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = 0.sp
    ),
    displayMedium = TextStyle(
        fontFamily = AppFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = AppFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = AppFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = AppFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = AppFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = AppFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = AppFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp
    ),
    titleSmall = TextStyle(
        fontFamily = AppFont,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = AppFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = AppFont,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    bodySmall = TextStyle(
        fontFamily = AppFont,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    ),
    labelLarge = TextStyle(
        fontFamily = AppFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    labelMedium = TextStyle(
        fontFamily = AppFont,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = AppFont,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    )
)

/**
 * Standardized typography and sizing scale for NotOffice563.
 * Strictly enforces touch targets (min 48dp) and readable icon scales.
 */
object SleekSizes {
    val iconMicro = 16.dp
    val iconSmall = 18.dp
    val iconMedium = 20.dp
    val iconLarge = 24.dp
    val iconHero = 32.dp

    val buttonSmall = 48.dp
    val buttonMedium = 48.dp
    val buttonDefault = 48.dp
    val buttonLarge = 56.dp

    val textCaption = 12.sp
    val textBody = 14.sp
    val textTitle = 16.sp
    val textHeading = 22.sp
    val textHero = 32.sp
}

/**
 * Standardized corner radius tokens: 8dp for chips/inputs, 12dp for cards, 20dp for sheets/dialogs, 24dp for pills.
 */
object SleekRadius {
    val chip = 8.dp
    val input = 8.dp
    val card = 12.dp
    val cardLarge = 16.dp
    val sheet = 20.dp
    val dialog = 20.dp
    val pill = 24.dp
}

/**
 * Standardized RoundedCornerShapes for NotOffice563.
 */
object SleekShapes {
    val chip = RoundedCornerShape(8.dp)
    val input = RoundedCornerShape(8.dp)
    val card = RoundedCornerShape(12.dp)
    val cardLarge = RoundedCornerShape(16.dp)
    val sheet = RoundedCornerShape(20.dp)
    val dialog = RoundedCornerShape(20.dp)
    val pill = RoundedCornerShape(24.dp)
}

/**
 * Standardized layout spacing scale based on 4/8/12/16/24 dp grid.
 */
object SleekSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp

    val screenPadding = 16.dp
    val cardPadding = 16.dp
}
