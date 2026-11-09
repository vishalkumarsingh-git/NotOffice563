package com.programmingblitz.notoffice563.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassTokens
import com.programmingblitz.notoffice563.util.HapticUtil

/**
 * Beautiful iOS 26 Liquid Glass Floating Dock Bar for NotOffice563:
 *
 * 1. Proportional Edge Alignment: Spans across the screen with a balanced, natural 14dp margin,
 *    eliminating awkward black edge gaps while preserving elegant floating ergonomics.
 * 2. Sliding Glass Pill Indicator: Glides horizontally behind active tabs using realistic
 *    spring physics, giving smooth momentum when switching destinations.
 * 3. Continuous Organic Curvature: 32dp capsule radius with multi-layered specular rim highlights
 *    and translucent frosted refraction.
 * 4. Micro-Interactions: Responsive spring bounce and tactile haptics on tap.
 */
@Composable
fun NotOfficeDockBar(
    currentRoute: String?,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val dockShape = RoundedCornerShape(32.dp)
    val dockSurfaceBrush = LiquidGlassTokens.getCapsuleBrush(isDark)
    val dockBorderBrush = LiquidGlassTokens.getGlassBorderBrush(isDark)

    val items = Screen.bottomNavItems
    val selectedIndex = items.indexOfFirst { isScreenActive(currentRoute, it) }.let {
        if (it == -1) 0 else it
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("main_bottom_nav_bar"),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = dockShape,
            color = Color.Transparent,
            shadowElevation = if (isDark) 18.dp else 12.dp,
            tonalElevation = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(dockShape)
                    .background(dockSurfaceBrush)
                    .border(BorderStroke(1.2.dp, dockBorderBrush), dockShape)
                    .padding(horizontal = 6.dp, vertical = 5.dp)
            ) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val tabWidth = maxWidth / items.size

                    // 1. Sliding Frosted Glass Pill Indicator
                    val animatedPillOffset by animateDpAsState(
                        targetValue = tabWidth * selectedIndex,
                        animationSpec = spring(
                            dampingRatio = 0.76f,
                            stiffness = 380f
                        ),
                        label = "slidingDockPillOffset"
                    )

                    val pillShape = RoundedCornerShape(24.dp)

                    Box(
                        modifier = Modifier
                            .offset(x = animatedPillOffset)
                            .width(tabWidth)
                            .fillMaxHeight()
                            .padding(horizontal = 3.dp, vertical = 2.dp)
                            .shadow(
                                elevation = if (isDark) 8.dp else 6.dp,
                                shape = pillShape,
                                spotColor = if (isDark) Color(0x60007AFF) else Color(0x55007AFF),
                                ambientColor = if (isDark) Color.Transparent else Color(0x200055B3)
                            )
                            .clip(pillShape)
                            .background(
                                if (isDark) {
                                    Brush.verticalGradient(
                                        listOf(
                                            Color(0x44007AFF),
                                            Color(0x240055B3)
                                        )
                                    )
                                } else {
                                    Brush.verticalGradient(
                                        listOf(
                                            Color(0x38007AFF),
                                            Color(0x200062D2)
                                        )
                                    )
                                }
                            )
                            .border(
                                BorderStroke(
                                    1.3.dp,
                                    if (isDark) {
                                        Brush.verticalGradient(
                                            listOf(
                                                Color(0x995AC8FA),
                                                Color(0x3D007AFF)
                                            )
                                        )
                                    } else {
                                        Brush.verticalGradient(
                                            listOf(
                                                Color(0xB8007AFF),
                                                Color(0x52007AFF)
                                            )
                                        )
                                    }
                                ),
                                pillShape
                            )
                    ) {
                        // Ambient radial luminous core inside active sliding pill
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            if (isDark) Color(0x35007AFF) else Color(0x40007AFF),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                    }

                    // 2. Dock Action Buttons
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items.forEachIndexed { index, screen ->
                            val isSelected = index == selectedIndex

                            SlidingDockTabItem(
                                icon = if (isSelected) (screen.selectedIcon ?: screen.icon) else (screen.unselectedIcon ?: screen.icon),
                                title = screen.title,
                                isSelected = isSelected,
                                isDark = isDark,
                                testTag = "nav_item_${screen.route}",
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                onClick = {
                                    HapticUtil.performSelection(haptic)
                                    onNavigate(screen)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Checks whether the current route represents the given screen.
 * Correctly matches query parameter patterns like "files?category=DOCUMENTS".
 */
private fun isScreenActive(currentRoute: String?, screen: Screen): Boolean {
    if (currentRoute == null) return false
    return when (screen) {
        is Screen.Files -> currentRoute.startsWith("files")
        else -> currentRoute == screen.route
    }
}

/**
 * Individual dock button with spring bounce, icon scale, and responsive typography.
 */
@Composable
private fun SlidingDockTabItem(
    icon: ImageVector,
    title: String,
    isSelected: Boolean,
    isDark: Boolean,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedScale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.92f
            isSelected -> 1.05f
            else -> 1.0f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "tabScaleAnim"
    )

    val activeIconColor = if (isDark) LiquidGlassTokens.AzureGlass else Color(0xFF0066E0)
    val activeTextColor = if (isDark) LiquidGlassTokens.AzureGlass else Color(0xFF0056C6)
    val inactiveIconColor = if (isDark) Color.White.copy(alpha = 0.70f) else Color(0xFF5A6679)
    val inactiveTextColor = if (isDark) LiquidGlassTokens.DarkTextSecondary else Color(0xFF5A6679)

    val iconTint by animateColorAsState(
        targetValue = if (isSelected) activeIconColor else inactiveIconColor,
        animationSpec = tween(180),
        label = "iconTintAnim"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) activeTextColor else inactiveTextColor,
        animationSpec = tween(180),
        label = "textColorAnim"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .testTag(testTag)
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .clip(RoundedCornerShape(22.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 4.dp)
    ) {
        // Icon with subtle luminous highlight
        Box(
            modifier = Modifier.size(26.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    if (isDark) activeIconColor.copy(alpha = 0.35f) else Color(0x40007AFF),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )
            }

            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        // Title Label
        Text(
            text = title,
            style = TextStyle(
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = textColor
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
