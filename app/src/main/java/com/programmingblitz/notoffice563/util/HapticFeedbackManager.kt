package com.programmingblitz.notoffice563.util

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView

/**
 * Semantic Haptic Event Categories.
 * Provides distinct tactile sensations tailored to productivity actions and Liquid Glass feedback.
 */
enum class HapticType {
    /** Subtle click for tab changes, segmented controls, dock selections */
    SELECTION,
    /** Light crisp impact for buttons, icons, formatting toggles */
    LIGHT_IMPACT,
    /** Medium firm impact for major structural changes (sheet add, slide delete) */
    MEDIUM_IMPACT,
    /** Positive confirmation pulse when a document, export, or note is saved */
    SUCCESS,
    /** Subtle double pulse for warnings */
    WARNING,
    /** Sharp distinct alert for validation failures or storage errors */
    ERROR,
    /** Sustained pulse for long-press contextual selection */
    LONG_PRESS,
    /** Threshold crossing tactile tick for slider or drag gestures */
    DRAG_THRESHOLD
}

/**
 * Centralized Haptic Feedback Manager for NotOffice563.
 *
 * Implements hardware-aware fallback across Compose LocalHapticFeedback and Android Platform View haptics.
 * Never executes in recomposition loops; only triggered at genuine user-interaction boundaries.
 */
class HapticFeedbackManager(
    private val composeHaptic: HapticFeedback?,
    private val view: View? = null
) {

    fun perform(type: HapticType) {
        try {
            when (type) {
                HapticType.SELECTION -> {
                    if (view != null && view.isHapticFeedbackEnabled) {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    } else {
                        composeHaptic?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                }
                HapticType.LIGHT_IMPACT -> {
                    if (view != null && view.isHapticFeedbackEnabled) {
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    } else {
                        composeHaptic?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                }
                HapticType.MEDIUM_IMPACT -> {
                    if (view != null && view.isHapticFeedbackEnabled) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        } else {
                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        }
                    } else {
                        composeHaptic?.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                }
                HapticType.SUCCESS -> {
                    if (view != null && view.isHapticFeedbackEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    } else {
                        composeHaptic?.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                }
                HapticType.WARNING -> {
                    if (view != null && view.isHapticFeedbackEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        view.performHapticFeedback(HapticFeedbackConstants.REJECT)
                    } else {
                        composeHaptic?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                }
                HapticType.ERROR -> {
                    if (view != null && view.isHapticFeedbackEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        view.performHapticFeedback(HapticFeedbackConstants.REJECT)
                    } else {
                        composeHaptic?.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                }
                HapticType.LONG_PRESS -> {
                    if (view != null && view.isHapticFeedbackEnabled) {
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    } else {
                        composeHaptic?.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                }
                HapticType.DRAG_THRESHOLD -> {
                    if (view != null && view.isHapticFeedbackEnabled) {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    } else {
                        composeHaptic?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                }
            }
        } catch (_: Exception) {
            // Fail silently on devices without haptic actuators or with disabled system touch feedback
        }
    }
}

val LocalHapticManager: ProvidableCompositionLocal<HapticFeedbackManager> =
    staticCompositionLocalOf { HapticFeedbackManager(null, null) }

@Composable
@ReadOnlyComposable
fun rememberHapticFeedbackManager(): HapticFeedbackManager {
    val composeHaptic = LocalHapticFeedback.current
    val view = LocalView.current
    return HapticFeedbackManager(composeHaptic, view)
}
