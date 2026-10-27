package com.programmingblitz.notoffice563.util

import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

/**
 * Utility for triggering subtle, device-compliant haptic feedback on meaningful user interactions.
 * Never called from recomposition or state observation loops.
 */
object HapticUtil {

    /**
     * Subtle tick for tab changes, drawer items, and selection toggles.
     */
    fun performSelection(haptic: HapticFeedback?) {
        try {
            haptic?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        } catch (_: Exception) {
        }
    }

    /**
     * Crisp pulse for primary actions (Save, Open Document, Theme Switch).
     */
    fun performAction(haptic: HapticFeedback?) {
        try {
            haptic?.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (_: Exception) {
        }
    }

    /**
     * Positive feedback when an operation completes successfully.
     */
    fun performSuccess(haptic: HapticFeedback?) {
        try {
            haptic?.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (_: Exception) {
        }
    }

    /**
     * Distinct feedback when a validation error or save error occurs.
     */
    fun performError(haptic: HapticFeedback?) {
        try {
            haptic?.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (_: Exception) {
        }
    }

    /**
     * Light impact for small toggles (Bold, Italic, Underline).
     */
    fun performLightImpact(haptic: HapticFeedback?) {
        try {
            haptic?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        } catch (_: Exception) {
        }
    }
}
