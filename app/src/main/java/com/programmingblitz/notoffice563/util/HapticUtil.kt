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
            // Gracefully ignore on devices without vibration hardware or disabled system haptics
        }
    }

    /**
     * Crisp pulse for primary actions (Google Sign In, Sign Out, Open Document, Theme Switch).
     */
    fun performAction(haptic: HapticFeedback?) {
        try {
            haptic?.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (_: Exception) {
        }
    }
}
