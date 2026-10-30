package com.programmingblitz.notoffice563.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.programmingblitz.notoffice563.domain.model.UserAccount
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassBadge
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassTokens
import com.programmingblitz.notoffice563.ui.theme.motion.liquidPressable
import com.programmingblitz.notoffice563.util.HapticUtil

/**
 * iOS 26 Liquid Glass Navigation Sidebar Drawer for NotOffice563.
 *
 * Designed with translucent layered glass, specular rim highlights, circular glowing
 * app branding, a frosted profile card with genuine Google Auth state, fluid active glass
 * destination pills, and an on-device security status badge.
 */
@Composable
fun NotOfficeSidebar(
    currentRoute: String?,
    currentUser: UserAccount?,
    onNavigate: (Screen) -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val surfaceBrush = LiquidGlassTokens.getGlassSurfaceBrush(isDark)
    val specularBorder = LiquidGlassTokens.getGlassBorderBrush(isDark)
    val drawerShape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp)

    ModalDrawerSheet(
        modifier = modifier
            .width(316.dp)
            .fillMaxHeight(),
        drawerShape = drawerShape,
        drawerContainerColor = Color.Transparent,
        drawerTonalElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .background(surfaceBrush)
                .border(BorderStroke(1.dp, specularBorder), drawerShape)
        ) {
            // Subtle ambient specular glow in the top-right corner
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(160.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                if (isDark) Color(0x18007AFF) else Color(0x225AC8FA),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 18.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    // 1. App Header with Glowing Circular Liquid Glass Badge
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Circular Liquid Glass App Badge
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .shadow(
                                    elevation = 8.dp,
                                    shape = CircleShape,
                                    ambientColor = LiquidGlassTokens.AzureGlass.copy(alpha = 0.35f),
                                    spotColor = LiquidGlassTokens.AzureGlass.copy(alpha = 0.5f)
                                )
                                .clip(CircleShape)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            LiquidGlassTokens.AzureGlassLight,
                                            LiquidGlassTokens.AzureGlass,
                                            Color(0xFF0056B3)
                                        )
                                    )
                                )
                                .border(
                                    1.2.dp,
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xE6FFFFFF),
                                            Color(0x40FFFFFF)
                                        )
                                    ),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Description,
                                contentDescription = "NotOffice563 Logo",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "NotOffice563",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) LiquidGlassTokens.DarkTextPrimary else LiquidGlassTokens.LightTextPrimary,
                                    letterSpacing = 0.2.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                LiquidGlassBadge(
                                    text = "LIQUID",
                                    color = LiquidGlassTokens.AzureGlass
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Document Workspace",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = if (isDark) LiquidGlassTokens.DarkTextSecondary else LiquidGlassTokens.LightTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. Profile Mini-Card in Liquid Glass (navigates to Profile on tap)
                    SidebarProfileCard(
                        currentUser = currentUser,
                        isDark = isDark,
                        onClick = {
                            HapticUtil.performSelection(haptic)
                            onCloseDrawer()
                            onNavigate(Screen.Profile)
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Specular Translucent Divider
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        if (isDark) Color(0x33FFFFFF) else Color(0x26002048),
                                        if (isDark) Color(0x10FFFFFF) else Color(0x0D002048),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3. Navigation Destination Items with Liquid Glass Pills
                    Screen.sidebarItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        val targetBg = if (isSelected) {
                            if (isDark) Color(0x38007AFF) else Color(0x24007AFF)
                        } else {
                            Color.Transparent
                        }
                        val animatedBg by animateColorAsState(targetValue = targetBg, label = "sbItemBg")

                        val borderStroke = if (isSelected) {
                            BorderStroke(
                                1.dp,
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0x80007AFF),
                                        Color(0x33007AFF)
                                    )
                                )
                            )
                        } else {
                            BorderStroke(1.dp, Color.Transparent)
                        }

                        val shape = LiquidGlassTokens.ShapeCapsule

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clip(shape)
                                .background(animatedBg)
                                .border(borderStroke, shape)
                                .liquidPressable {
                                    HapticUtil.performSelection(haptic)
                                    onCloseDrawer()
                                    onNavigate(screen)
                                }
                                .testTag("sidebar_item_${screen.route}")
                                .padding(horizontal = 12.dp, vertical = 9.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val iconVector = if (isSelected) {
                                    screen.selectedIcon ?: screen.icon
                                } else {
                                    screen.unselectedIcon ?: screen.icon
                                }

                                // Circular glass icon container
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) {
                                                Brush.verticalGradient(
                                                    listOf(
                                                        LiquidGlassTokens.AzureGlassLight,
                                                        LiquidGlassTokens.AzureGlass
                                                    )
                                                )
                                            } else {
                                                if (isDark) {
                                                    Brush.verticalGradient(
                                                        listOf(Color(0x22FFFFFF), Color(0x12FFFFFF))
                                                    )
                                                } else {
                                                    Brush.verticalGradient(
                                                        listOf(Color(0xEEFFFFFF), Color(0xCCEDF3FB))
                                                    )
                                                }
                                            }
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) Color(0x80FFFFFF) else (if (isDark) Color(0x18FFFFFF) else Color(0x18002048)),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = iconVector,
                                        contentDescription = screen.title,
                                        tint = if (isSelected) Color.White else (if (isDark) LiquidGlassTokens.DarkTextSecondary else LiquidGlassTokens.LightTextSecondary),
                                        modifier = Modifier.size(19.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Text(
                                    text = screen.title,
                                    fontSize = 15.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                    color = if (isSelected) {
                                        if (isDark) Color.White else LiquidGlassTokens.AzureGlass
                                    } else {
                                        if (isDark) LiquidGlassTokens.DarkTextPrimary else LiquidGlassTokens.LightTextPrimary
                                    },
                                    modifier = Modifier.weight(1f)
                                )

                                if (isSelected) {
                                    // Glowing Liquid Glass active indicator pip
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .shadow(
                                                elevation = 4.dp,
                                                shape = CircleShape,
                                                ambientColor = Color(0xFF5AC8FA),
                                                spotColor = Color(0xFF5AC8FA)
                                            )
                                            .clip(CircleShape)
                                            .background(Color(0xFF5AC8FA))
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Drawer Footer with Liquid Glass Security Status
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    // Specular Divider
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        if (isDark) Color(0x24FFFFFF) else Color(0x20002048),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Translucent Security Pill
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(LiquidGlassTokens.ShapeCapsule)
                            .background(
                                if (isDark) Color(0x24141C2E) else Color(0xDDF0F4FA)
                            )
                            .border(
                                1.dp,
                                if (isDark) Color(0x1FFFFFFF) else Color(0x18002048),
                                LiquidGlassTokens.ShapeCapsule
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Shield,
                                contentDescription = null,
                                tint = LiquidGlassTokens.EmeraldGlass,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "100% On-Device • SAF Access",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isDark) LiquidGlassTokens.DarkTextSecondary else LiquidGlassTokens.LightTextSecondary,
                                modifier = Modifier.weight(1f)
                            )
                            // Green Active Dot
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(LiquidGlassTokens.EmeraldGlass)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "NotOffice563 • Created by V. K. Singh",
                        fontSize = 10.sp,
                        color = if (isDark) LiquidGlassTokens.DarkTextTertiary else LiquidGlassTokens.LightTextTertiary,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                    Text(
                        text = "© 2026 Programming Blitz.",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDark) LiquidGlassTokens.DarkTextTertiary else LiquidGlassTokens.LightTextTertiary,
                        modifier = Modifier.padding(start = 6.dp, top = 2.dp)
                    )
                }
            }
        }
    }
}

/**
 * Honest sidebar account tile styled with authentic iOS 26 Liquid Glass:
 * Shows real Google profile if signed in;
 * Shows a neutral local-mode card with "Sign in with Google" prompt if signed out.
 */
@Composable
private fun SidebarProfileCard(
    currentUser: UserAccount?,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val cardShape = LiquidGlassTokens.ShapeMedium

    val cardBg = if (isDark) {
        Brush.verticalGradient(
            listOf(
                Color(0x381E2942),
                Color(0x22131A2B)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color(0xF0FFFFFF),
                Color(0xDCF0F4FA)
            )
        )
    }

    val cardBorder = if (isDark) {
        BorderStroke(1.dp, Color(0x2BFFFFFF))
    } else {
        BorderStroke(1.dp, Color(0x26002048))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(cardBg)
            .border(cardBorder, cardShape)
            .liquidPressable(onClick = onClick)
            .testTag("sidebar_profile_card")
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentUser != null) {
                // Real authenticated user
                if (!currentUser.photoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = currentUser.photoUrl,
                        contentDescription = "Profile photo",
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, LiquidGlassTokens.AzureGlass, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        LiquidGlassTokens.AzureGlassLight,
                                        LiquidGlassTokens.AzureGlass
                                    )
                                )
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentUser.getInitials(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentUser.displayName ?: currentUser.email ?: "Google Account",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) LiquidGlassTokens.DarkTextPrimary else LiquidGlassTokens.LightTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = currentUser.email ?: "Signed In",
                        fontSize = 11.sp,
                        color = LiquidGlassTokens.AzureGlass,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else {
                // Neutral signed-out placeholder (honest, never fake names)
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            if (isDark) {
                                Brush.verticalGradient(
                                    listOf(Color(0x30FFFFFF), Color(0x18FFFFFF))
                                )
                            } else {
                                Brush.verticalGradient(
                                    listOf(Color(0xE0FFFFFF), Color(0xC0E2EAF5))
                                )
                            }
                        )
                        .border(
                            1.dp,
                            if (isDark) Color(0x30FFFFFF) else Color(0x20002048),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = LiquidGlassTokens.AzureGlass,
                        modifier = Modifier.size(19.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sign in with Google",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LiquidGlassTokens.AzureGlass
                    )
                    Text(
                        text = "Local Workspace Mode",
                        fontSize = 11.sp,
                        color = if (isDark) LiquidGlassTokens.DarkTextSecondary else LiquidGlassTokens.LightTextSecondary
                    )
                }
            }

            // Glass Chevron
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        if (isDark) Color(0x14FFFFFF) else Color(0x0F002048)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = "View Profile",
                    tint = if (isDark) LiquidGlassTokens.DarkTextSecondary else LiquidGlassTokens.LightTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
