package com.programmingblitz.notoffice563.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.programmingblitz.notoffice563.ui.theme.AccentBlue
import com.programmingblitz.notoffice563.ui.theme.SleekShapes
import com.programmingblitz.notoffice563.ui.theme.SleekSizes
import com.programmingblitz.notoffice563.ui.theme.blendAccent

/**
 * Animated Floating Dock Bar:
 * Translucent glass capsule, animated active pill indicator with glowing circular icon badge,
 * and smooth expanding title label on selection.
 */
@Composable
fun NotOfficeDockBar(
    currentRoute: String?,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val capsuleBrush = com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassTokens.getCapsuleBrush(isDark)
    val borderBrush = com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassTokens.getGlassBorderBrush(isDark)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("main_bottom_nav_bar"),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .clip(SleekShapes.pill)
                .background(capsuleBrush)
                .border(BorderStroke(1.2.dp, borderBrush), SleekShapes.pill)
                .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Screen.bottomNavItems.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    AnimatedDockTile(
                        icon = if (isSelected) (screen.selectedIcon ?: screen.icon) else (screen.unselectedIcon ?: screen.icon),
                        title = screen.title,
                        isSelected = isSelected,
                        testTag = "nav_item_${screen.route}",
                        onClick = {
                            com.programmingblitz.notoffice563.util.HapticUtil.performSelection(haptic)
                            onNavigate(screen)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedDockTile(
    icon: ImageVector,
    title: String,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val animatedPaddingHorizontal by animateDpAsState(
        targetValue = if (isSelected) 14.dp else 10.dp,
        animationSpec = com.programmingblitz.notoffice563.ui.theme.motion.MotionTokens.springSmooth(),
        label = "tilePadding"
    )

    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) com.programmingblitz.notoffice563.ui.theme.motion.MotionTokens.ScaleDockItemActive else com.programmingblitz.notoffice563.ui.theme.motion.MotionTokens.ScaleResting,
        animationSpec = com.programmingblitz.notoffice563.ui.theme.motion.MotionTokens.springInteractive(),
        label = "tileScale"
    )

    val activePillBg = AccentBlue
    val activePillBorder = AccentBlue.copy(alpha = 0.85f)

    val containerBg by animateColorAsState(
        targetValue = if (isSelected) activePillBg else Color.Transparent,
        animationSpec = tween(com.programmingblitz.notoffice563.ui.theme.motion.MotionTokens.DurationDockMovement, easing = com.programmingblitz.notoffice563.ui.theme.motion.MotionTokens.EaseStandard),
        label = "containerBgAnim"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .testTag(testTag)
            .height(SleekSizes.buttonSmall)
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .clip(SleekShapes.pill)
            .background(containerBg)
            .border(
                border = BorderStroke(
                    width = if (isSelected) 1.2.dp else 0.dp,
                    color = if (isSelected) activePillBorder else Color.Transparent
                ),
                shape = SleekShapes.pill
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = animatedPaddingHorizontal, vertical = 6.dp)
    ) {
        // Glowing Icon Badge Circle
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) {
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.35f),
                                Color.White.copy(alpha = 0.05f)
                            )
                        )
                    } else {
                        if (isDark) {
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.08f),
                                    Color.White.copy(alpha = 0.04f)
                                )
                            )
                        } else {
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFF1F5F9),
                                    Color(0xFFE2E8F0)
                                )
                            )
                        }
                    }
                )
                .border(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) {
                        Color.White.copy(alpha = 0.9f)
                    } else {
                        if (isDark) Color.White.copy(alpha = 0.15f) else Color(0xFFCBD5E1)
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) {
                    Color.White
                } else {
                    if (isDark) Color.White.copy(alpha = 0.75f) else Color(0xFF475569)
                },
                modifier = Modifier.size(if (isSelected) SleekSizes.iconMedium else SleekSizes.iconSmall)
            )
        }

        AnimatedVisibility(
            visible = isSelected,
            enter = expandHorizontally(
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy)
            ) + fadeIn(animationSpec = tween(180, easing = FastOutSlowInEasing)),
            exit = shrinkHorizontally(
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy)
            ) + fadeOut(animationSpec = tween(120))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = SleekSizes.textCaption,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
