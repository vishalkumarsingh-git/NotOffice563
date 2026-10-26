package com.programmingblitz.notoffice563.ui.theme.glass

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * iOS 26 Liquid Glass Design System Tokens.
 *
 * Provides translucent optical layers, specular rim highlights, layered depth,
 * luminous vibrant accents, and smooth organic curvature for NotOffice563.
 */
object LiquidGlassTokens {

    // --- ACCENT CRYSTALLINE PALETTE ---
    val AzureGlass = Color(0xFF007AFF)
    val AzureGlassLight = Color(0xFF5AC8FA)
    val AzureGlassGlow = Color(0x33007AFF)

    val EmeraldGlass = Color(0xFF34C759)
    val EmeraldGlassLight = Color(0xFF30D158)
    val EmeraldGlassGlow = Color(0x3334C759)

    val AmberGlass = Color(0xFFFF9500)
    val AmberGlassLight = Color(0xFFFF9F0A)
    val AmberGlassGlow = Color(0x33FF9500)

    val CrimsonGlass = Color(0xFFFF3B30)
    val CrimsonGlassLight = Color(0xFFFF453A)
    val CrimsonGlassGlow = Color(0x33FF3B30)

    val VioletGlass = Color(0xFFAF52DE)
    val VioletGlassLight = Color(0xFFBF5AF2)
    val VioletGlassGlow = Color(0x33AF52DE)

    // --- DARK GLASS SURFACES ---
    val DarkBaseBackground = Color(0xFF080B11)
    val DarkGlassSurface = Color(0xCC111625)
    val DarkGlassSurfaceElevated = Color(0xD9181F33)
    val DarkGlassCapsule = Color(0xE6141A2B)
    val DarkGlassBorderTop = Color(0x40FFFFFF)
    val DarkGlassBorderBottom = Color(0x1AFFFFFF)
    val DarkGlassInnerHighlight = Color(0x1FFFFFFF)
    val DarkTextPrimary = Color(0xFFF8FAFD)
    val DarkTextSecondary = Color(0xFF9AA5B8)
    val DarkTextTertiary = Color(0xFF606D82)

    // --- LIGHT GLASS SURFACES ---
    val LightBaseBackground = Color(0xFFF0F4FA)
    val LightGlassSurface = Color(0xCCFFFFFF)
    val LightGlassSurfaceElevated = Color(0xE6FFFFFF)
    val LightGlassCapsule = Color(0xF2FBFDFF)
    val LightGlassBorderTop = Color(0xB3FFFFFF)
    val LightGlassBorderBottom = Color(0x26002048)
    val LightGlassInnerHighlight = Color(0x80FFFFFF)
    val LightTextPrimary = Color(0xFF0D1424)
    val LightTextSecondary = Color(0xFF526079)
    val LightTextTertiary = Color(0xFF8695AA)

    // --- DOCUMENT CANVAS (Strictly solid, glare-free surfaces) ---
    val DocumentCanvasLight = Color(0xFFFFFFFF)
    val DocumentCanvasDark = Color(0xFF161B26)
    val DocumentGridBorderLight = Color(0xFFE2E8F0)
    val DocumentGridBorderDark = Color(0xFF242C3D)

    // --- SMOOTH CORNER RADII ---
    val RadiusMicro = 8.dp
    val RadiusSmall = 12.dp
    val RadiusMedium = 16.dp
    val RadiusLarge = 22.dp
    val RadiusCapsule = 999.dp

    val ShapeMicro = RoundedCornerShape(RadiusMicro)
    val ShapeSmall = RoundedCornerShape(RadiusSmall)
    val ShapeMedium = RoundedCornerShape(RadiusMedium)
    val ShapeLarge = RoundedCornerShape(RadiusLarge)
    val ShapeCapsule = RoundedCornerShape(RadiusCapsule)

    // --- SPECULAR BORDER GRADIENTS ---
    fun getGlassBorderBrush(isDark: Boolean): Brush {
        return if (isDark) {
            Brush.verticalGradient(
                colors = listOf(DarkGlassBorderTop, DarkGlassBorderBottom),
                startY = 0f,
                endY = Float.POSITIVE_INFINITY
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(LightGlassBorderTop, LightGlassBorderBottom),
                startY = 0f,
                endY = Float.POSITIVE_INFINITY
            )
        }
    }

    fun getGlassSurfaceBrush(isDark: Boolean): Brush {
        return if (isDark) {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xD9161D2E),
                    Color(0xCC0E1321)
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xE6FFFFFF),
                    Color(0xD9F3F6FD)
                )
            )
        }
    }

    fun getCapsuleBrush(isDark: Boolean): Brush {
        return if (isDark) {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xF0182035),
                    Color(0xEA111626)
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xF5FFFFFF),
                    Color(0xECEDF3FB)
                )
            )
        }
    }
}
