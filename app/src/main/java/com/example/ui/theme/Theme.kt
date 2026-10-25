package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Financer Pro dark color scheme: deep slate with subtle blue undertones
private val DarkColorScheme = darkColorScheme(
    primary = AccentBlueLight,
    onPrimary = SlateNavyDark,
    primaryContainer = AccentBlueContainerDark,
    onPrimaryContainer = AccentBlueContainer,
    inversePrimary = AccentBlue,
    secondary = TextSecondaryDark,
    onSecondary = SlateNavyDark,
    secondaryContainer = SurfaceVariantDark,
    onSecondaryContainer = TextPrimaryDark,
    tertiary = ExcelSheetGreenLight,
    onTertiary = Color(0xFF022C22),
    tertiaryContainer = ExcelSheetContainerDark,
    onTertiaryContainer = Color(0xFFDCFCE7),
    error = ExpenseRed,
    onError = Color(0xFF450A0A),
    errorContainer = ExpenseRedContainerDark,
    onErrorContainer = Color(0xFFFEE2E2),
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    surfaceTint = AccentBlueLight,
    inverseSurface = Color(0xFFE6E8EF),
    inverseOnSurface = Color(0xFF171923),
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark
)

// Financer Pro light color scheme: cool near-white with Classic Blue accents
private val LightColorScheme = lightColorScheme(
    primary = AccentBlue,
    onPrimary = Color.White,
    primaryContainer = AccentBlueContainer,
    onPrimaryContainer = OnAccentBlueContainer,
    inversePrimary = AccentBlueLight,
    secondary = SlateBlue,
    onSecondary = Color.White,
    secondaryContainer = SurfaceVariantLight,
    onSecondaryContainer = TextPrimaryLight,
    tertiary = ExcelSheetGreen,
    onTertiary = Color.White,
    tertiaryContainer = ExcelSheetContainer,
    onTertiaryContainer = Color(0xFF064E3B),
    error = ExpenseRed,
    onError = Color.White,
    errorContainer = ExpenseRedContainer,
    onErrorContainer = Color(0xFF7F1D1D),
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    surfaceTint = AccentBlue,
    inverseSurface = Color(0xFF252A35),
    inverseOnSurface = Color(0xFFF7F8FC),
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight
)

@Composable
fun NotOfficeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
