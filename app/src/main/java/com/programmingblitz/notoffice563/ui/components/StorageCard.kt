package com.programmingblitz.notoffice563.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.programmingblitz.notoffice563.ui.theme.AccentBlue
import com.programmingblitz.notoffice563.ui.theme.SleekShapes
import com.programmingblitz.notoffice563.ui.theme.SleekSizes
import com.programmingblitz.notoffice563.ui.theme.getHeroCardGradient
import com.programmingblitz.notoffice563.util.DeviceStorageInfo

/**
 * Storage and workspace hero card:
 * Subtle surface gradient, 16dp rounded corners, 1dp outline border,
 * glowing circular storage icon badge, and refined metrics display.
 */
@Composable
fun StorageCard(
    storageInfo: DeviceStorageInfo,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("storage_card"),
        shape = com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassTokens.ShapeLarge,
        isDark = isDark,
        elevation = if (isDark) 4.dp else 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Circular icon badge
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AccentBlue.copy(alpha = if (isDark) 0.22f else 0.12f))
                                .border(1.dp, AccentBlue.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Storage,
                                contentDescription = null,
                                tint = AccentBlue,
                                modifier = Modifier.size(SleekSizes.iconSmall)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Device Storage",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassTokens.DarkTextPrimary else com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassTokens.LightTextPrimary
                            )
                            Text(
                                text = "Local storage capacity",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDark) com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassTokens.DarkTextSecondary else com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassTokens.LightTextSecondary
                            )
                        }
                    }

                    // Liquid Glass Percentage badge
                    com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassBadge(
                        text = "${(storageInfo.usedPercentage * 100).toInt()}% Used",
                        color = AccentBlue,
                        containerColor = AccentBlue.copy(alpha = if (isDark) 0.22f else 0.12f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { storageInfo.usedPercentage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(SleekShapes.chip),
                    color = AccentBlue,
                    trackColor = if (isDark) Color(0x33FFFFFF) else Color(0x1F000000)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${storageInfo.usedGbFormatted} used",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassTokens.DarkTextSecondary else com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassTokens.LightTextSecondary
                    )
                    Text(
                        text = "${storageInfo.availableGbFormatted} free",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = if (isDark) com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassTokens.DarkTextPrimary else com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassTokens.LightTextPrimary
                    )
                }
            }
        }
    }
}
