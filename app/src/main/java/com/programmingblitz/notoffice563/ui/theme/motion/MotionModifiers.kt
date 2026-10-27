package com.programmingblitz.notoffice563.ui.theme.motion

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import com.programmingblitz.notoffice563.util.HapticFeedbackManager
import com.programmingblitz.notoffice563.util.HapticType
import com.programmingblitz.notoffice563.util.HapticUtil

/**
 * Reusable Motion Modifiers for NotOffice563 Liquid Glass & Productivity Components.
 */

/**
 * Applies tactile Liquid Glass press response: smooth scaling and semantic haptic feedback
 * on tap without interfering with layout dimensions.
 */
fun Modifier.liquidPressable(
    enabled: Boolean = true,
    pressedScale: Float = MotionTokens.ScaleButtonPressed,
    hapticType: HapticType = HapticType.LIGHT_IMPACT,
    onClick: (() -> Unit)? = null
): Modifier = composed {
    if (!enabled || onClick == null) return@composed this

    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else MotionTokens.ScaleResting,
        animationSpec = if (isPressed) MotionTokens.SpecPressIn else MotionTokens.SpecPressOut,
        label = "liquidPressScale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(enabled, onClick) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    tryAwaitRelease()
                    isPressed = false
                },
                onTap = {
                    when (hapticType) {
                        HapticType.SELECTION -> HapticUtil.performSelection(haptic)
                        HapticType.LIGHT_IMPACT -> HapticUtil.performLightImpact(haptic)
                        HapticType.MEDIUM_IMPACT -> HapticUtil.performAction(haptic)
                        HapticType.SUCCESS -> HapticUtil.performSuccess(haptic)
                        HapticType.ERROR -> HapticUtil.performError(haptic)
                        else -> HapticUtil.performSelection(haptic)
                    }
                    onClick()
                }
            )
        }
}

/**
 * Subtle localized horizontal shake animation for validation and input error feedback.
 * Shakes only the affected component (text field, card, or dialog) without jarring the entire screen.
 */
fun Modifier.errorShake(
    trigger: Any?
): Modifier = composed {
    val offsetX = remember { Animatable(0f) }

    LaunchedEffect(trigger) {
        if (trigger != null) {
            offsetX.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = MotionTokens.DurationError
                    0f at 0 using FastOutSlowInEasing
                    -6f at 50 using FastOutSlowInEasing
                    6f at 100 using FastOutSlowInEasing
                    -4f at 150 using FastOutSlowInEasing
                    4f at 200 using FastOutSlowInEasing
                    0f at MotionTokens.DurationError using FastOutSlowInEasing
                }
            )
        }
    }

    this.graphicsLayer {
        translationX = offsetX.value
    }
}
