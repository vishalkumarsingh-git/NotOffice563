package com.programmingblitz.notoffice563.ui.theme.motion

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Centralized Motion Tokens for NotOffice563.
 *
 * Combines iOS 26 Liquid Glass fluid momentum with Microsoft 365 fast,
 * predictable productivity response.
 */
object MotionTokens {

    // --- TIMING DURATIONS (ms) ---
    const val DurationImmediatePress = 80
    const val DurationIconChange = 140
    const val DurationSelection = 160
    const val DurationPopup = 180
    const val DurationMenu = 200
    const val DurationDialog = 200
    const val DurationSheet = 260
    const val DurationNavigation = 240
    const val DurationDockMovement = 220
    const val DurationSuccess = 220
    const val DurationError = 280

    // --- EASING CURVES ---
    val EaseFluidGlass = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1.0f)
    val EaseDecel = LinearOutSlowInEasing
    val EaseAccel = FastOutLinearInEasing
    val EaseStandard = FastOutSlowInEasing

    // --- SCALE FACTORS ---
    const val ScaleButtonPressed = 0.965f
    const val ScaleCardPressed = 0.985f
    const val ScaleIconButtonPressed = 0.92f
    const val ScaleDockItemActive = 1.02f
    const val ScaleResting = 1.0f

    // --- SPRING SPECS ---
    fun <T> springSmooth() = spring<T>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    fun <T> springInteractive() = spring<T>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    fun springDockDp() = spring<Dp>(
        dampingRatio = 0.82f,
        stiffness = 380f
    )

    // --- TWEEN SPECS ---
    val SpecPressIn = tween<Float>(DurationImmediatePress, easing = EaseAccel)
    val SpecPressOut = tween<Float>(DurationImmediatePress + 50, easing = EaseDecel)
    val SpecFadeIn = tween<Float>(DurationSelection, easing = EaseStandard)
    val SpecFadeOut = tween<Float>(DurationSelection - 30, easing = EaseStandard)
    val SpecColor = tween<androidx.compose.ui.graphics.Color>(DurationSelection, easing = EaseStandard)
    val SpecContent = tween<Float>(DurationPopup, easing = EaseStandard)
    val SpecDialogEntrance = tween<Float>(DurationDialog, easing = EaseDecel)
}
