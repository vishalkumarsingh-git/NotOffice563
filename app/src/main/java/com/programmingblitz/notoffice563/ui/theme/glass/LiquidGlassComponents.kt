package com.programmingblitz.notoffice563.ui.theme.glass

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import com.programmingblitz.notoffice563.ui.theme.motion.MotionTokens
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.programmingblitz.notoffice563.util.HapticUtil

/**
 * Custom iOS 26 Liquid Glass Surface container.
 * Renders multi-layer optical translucency, specular rim highlights, and soft layered depth.
 */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = LiquidGlassTokens.ShapeMedium,
    isDark: Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f,
    elevation: Dp = 2.dp,
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val borderBrush = LiquidGlassTokens.getGlassBorderBrush(isDark)
    val surfaceBrush = LiquidGlassTokens.getGlassSurfaceBrush(isDark)

    Box(
        modifier = modifier
            .shadow(elevation, shape, clip = false)
            .clip(shape)
            .background(surfaceBrush)
            .border(borderWidth, borderBrush, shape)
    ) {
        content()
    }
}

/**
 * Custom Liquid Glass Card.
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = LiquidGlassTokens.ShapeMedium,
    isDark: Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) com.programmingblitz.notoffice563.ui.theme.motion.MotionTokens.ScaleCardPressed else 1f,
        animationSpec = if (isPressed) com.programmingblitz.notoffice563.ui.theme.motion.MotionTokens.SpecPressIn else com.programmingblitz.notoffice563.ui.theme.motion.MotionTokens.SpecPressOut,
        label = "glassCardScale"
    )

    val clickableModifier = if (onClick != null) {
        Modifier.pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    tryAwaitRelease()
                    isPressed = false
                },
                onTap = {
                    HapticUtil.performSelection(haptic)
                    onClick()
                }
            )
        }
    } else Modifier

    LiquidGlassSurface(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(clickableModifier),
        shape = shape,
        isDark = isDark,
        elevation = if (isDark) 3.dp else 1.5.dp,
        content = content
    )
}

/**
 * Custom Liquid Glass Button with tactile spring response and specular glow.
 */
@Composable
fun LiquidGlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    isPrimary: Boolean = true,
    accentColor: Color = LiquidGlassTokens.AzureGlass,
    height: Dp = 48.dp
) {
    val haptic = LocalHapticFeedback.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled && !isLoading) com.programmingblitz.notoffice563.ui.theme.motion.MotionTokens.ScaleButtonPressed else 1f,
        animationSpec = if (isPressed) com.programmingblitz.notoffice563.ui.theme.motion.MotionTokens.SpecPressIn else com.programmingblitz.notoffice563.ui.theme.motion.MotionTokens.SpecPressOut,
        label = "glassBtnScale"
    )

    val shape = LiquidGlassTokens.ShapeCapsule

    val backgroundBrush = if (isPrimary) {
        Brush.verticalGradient(
            colors = listOf(
                accentColor,
                accentColor.copy(alpha = 0.85f)
            )
        )
    } else {
        if (isDark) {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0x33FFFFFF),
                    Color(0x1AFFFFFF)
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xE6FFFFFF),
                    Color(0xCCF0F4FA)
                )
            )
        }
    }

    val contentColor = if (isPrimary) {
        Color.White
    } else {
        if (isDark) LiquidGlassTokens.DarkTextPrimary else LiquidGlassTokens.LightTextPrimary
    }

    val borderStroke = if (isPrimary) {
        BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
    } else {
        BorderStroke(
            1.dp,
            if (isDark) Color.White.copy(alpha = 0.18f) else Color.Black.copy(alpha = 0.08f)
        )
    }

    Box(
        modifier = modifier
            .height(height)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(backgroundBrush)
            .border(borderStroke, shape)
            .pointerInput(enabled && !isLoading) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = {
                        HapticUtil.performLightImpact(haptic)
                        onClick()
                    }
                )
            }
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = contentColor
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = contentColor,
                        letterSpacing = 0.sp
                    )
                )
            }
        }
    }
}

/**
 * Custom Liquid Glass Top Bar.
 */
@Composable
fun LiquidGlassTopBar(
    title: String,
    subtitle: String? = null,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    LiquidGlassSurface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
        isDark = isDark,
        elevation = 3.dp,
        borderWidth = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (navigationIcon != null) {
                navigationIcon()
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) LiquidGlassTokens.DarkTextPrimary else LiquidGlassTokens.LightTextPrimary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (isDark) LiquidGlassTokens.DarkTextSecondary else LiquidGlassTokens.LightTextSecondary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (actions != null) {
                Spacer(modifier = Modifier.width(8.dp))
                actions()
            }
        }
    }
}

/**
 * Custom Liquid Glass Segmented Control with sliding frosted pill.
 */
@Composable
fun LiquidGlassSegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    Box(
        modifier = modifier
            .clip(LiquidGlassTokens.ShapeCapsule)
            .background(if (isDark) Color(0x33141A29) else Color(0x26002048))
            .border(
                1.dp,
                if (isDark) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.6f),
                LiquidGlassTokens.ShapeCapsule
            )
            .padding(3.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex
                val activeBg by animateColorAsState(
                    targetValue = if (isSelected) LiquidGlassTokens.AzureGlass else Color.Transparent,
                    label = "tabBg"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else (if (isDark) LiquidGlassTokens.DarkTextSecondary else LiquidGlassTokens.LightTextSecondary),
                    label = "tabText"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(LiquidGlassTokens.ShapeCapsule)
                        .background(activeBg)
                        .clickable {
                            HapticUtil.performSelection(haptic)
                            onSelectIndex(index)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item,
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            color = textColor
                        ),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * Custom Liquid Glass Text Field with specular focus glow.
 */
@Composable
fun LiquidGlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    onClear: (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(LiquidGlassTokens.ShapeSmall)
            .background(
                if (isDark) Color(0x261E283E) else Color(0xD9FFFFFF)
            )
            .border(
                1.dp,
                if (isDark) Color.White.copy(alpha = 0.18f) else Color.Black.copy(alpha = 0.08f),
                LiquidGlassTokens.ShapeSmall
            )
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = if (isDark) LiquidGlassTokens.DarkTextSecondary else LiquidGlassTokens.LightTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
            }

            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = if (isDark) LiquidGlassTokens.DarkTextTertiary else LiquidGlassTokens.LightTextTertiary
                        )
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        color = if (isDark) LiquidGlassTokens.DarkTextPrimary else LiquidGlassTokens.LightTextPrimary
                    ),
                    cursorBrush = SolidColor(LiquidGlassTokens.AzureGlass),
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (value.isNotEmpty() && onClear != null) {
                LiquidGlassIconButton(
                    onClick = onClear,
                    icon = Icons.Filled.Close,
                    contentDescription = "Clear text",
                    size = 28.dp
                )
            }
        }
    }
}

/**
 * Custom Liquid Glass Badge.
 */
@Composable
fun LiquidGlassBadge(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = LiquidGlassTokens.AzureGlass,
    containerColor: Color = color.copy(alpha = 0.15f)
) {
    Box(
        modifier = modifier
            .clip(LiquidGlassTokens.ShapeCapsule)
            .background(containerColor)
            .border(0.75.dp, color.copy(alpha = 0.35f), LiquidGlassTokens.ShapeCapsule)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                letterSpacing = 0.2.sp
            )
        )
    }
}

/**
 * Custom Liquid Glass Floating Toolbar container.
 */
@Composable
fun LiquidGlassFloatingToolbar(
    modifier: Modifier = Modifier,
    isDark: Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f,
    content: @Composable RowScope.() -> Unit
) {
    LiquidGlassSurface(
        modifier = modifier,
        shape = LiquidGlassTokens.ShapeCapsule,
        isDark = isDark,
        elevation = 6.dp,
        borderWidth = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            content = content
        )
    }
}

/**
 * Custom Liquid Glass Icon Button with touch feedback and specular highlight.
 */
@Composable
fun LiquidGlassIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    activeColor: Color = LiquidGlassTokens.AzureGlass,
    enabled: Boolean = true,
    size: Dp = 38.dp
) {
    val haptic = LocalHapticFeedback.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) com.programmingblitz.notoffice563.ui.theme.motion.MotionTokens.ScaleIconButtonPressed else 1f,
        animationSpec = if (isPressed) com.programmingblitz.notoffice563.ui.theme.motion.MotionTokens.SpecPressIn else com.programmingblitz.notoffice563.ui.theme.motion.MotionTokens.SpecPressOut,
        label = "glassIconBtnScale"
    )

    val bgBrush = if (isActive) {
        Brush.verticalGradient(listOf(activeColor, activeColor.copy(alpha = 0.85f)))
    } else {
        if (isDark) {
            Brush.verticalGradient(listOf(Color(0x33FFFFFF), Color(0x1AFFFFFF)))
        } else {
            Brush.verticalGradient(listOf(Color(0xF0FFFFFF), Color(0xDCF0F4FA)))
        }
    }

    val iconColor = if (isActive) {
        Color.White
    } else {
        if (!enabled) {
            if (isDark) LiquidGlassTokens.DarkTextTertiary else LiquidGlassTokens.LightTextTertiary
        } else {
            if (isDark) LiquidGlassTokens.DarkTextPrimary else LiquidGlassTokens.LightTextPrimary
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(bgBrush)
            .border(
                1.dp,
                if (isActive) Color.White.copy(alpha = 0.4f) else (if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.08f)),
                CircleShape
            )
            .pointerInput(enabled) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = {
                        HapticUtil.performLightImpact(haptic)
                        onClick()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size(18.dp)
        )
    }
}

/**
 * Custom Liquid Glass Dialog replacement with smooth entrance animation.
 */
@Composable
fun LiquidGlassDialog(
    onDismissRequest: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    isDark: Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f,
    confirmButton: (@Composable () -> Unit)? = null,
    dismissButton: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismissRequest) {
        var isVisible by remember { mutableStateOf(false) }
        androidx.compose.runtime.LaunchedEffect(Unit) {
            isVisible = true
        }

        val animScale by animateFloatAsState(
            targetValue = if (isVisible) 1f else 0.94f,
            animationSpec = com.programmingblitz.notoffice563.ui.theme.motion.MotionTokens.springInteractive(),
            label = "dialogScale"
        )
        val animAlpha by animateFloatAsState(
            targetValue = if (isVisible) 1f else 0f,
            animationSpec = com.programmingblitz.notoffice563.ui.theme.motion.MotionTokens.SpecFadeIn,
            label = "dialogAlpha"
        )

        LiquidGlassSurface(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .graphicsLayer {
                    scaleX = animScale
                    scaleY = animScale
                    alpha = animAlpha
                },
            shape = LiquidGlassTokens.ShapeLarge,
            isDark = isDark,
            elevation = 16.dp,
            borderWidth = 1.2.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = title,
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) LiquidGlassTokens.DarkTextPrimary else LiquidGlassTokens.LightTextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(14.dp))
                content()
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (dismissButton != null) {
                        dismissButton()
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    if (confirmButton != null) {
                        confirmButton()
                    }
                }
            }
        }
    }
}

/**
 * iOS 26 Liquid Glass Circular Floating Action Button.
 * Features a circular shape, layered translucency, specular rim highlights,
 * inner specular reflection sheen, elevated drop shadow, and tactile spring physics.
 */
@Composable
fun LiquidGlassFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Filled.Add,
    contentDescription: String? = null,
    accentColor: Color = LiquidGlassTokens.AzureGlass,
    accentColorLight: Color = LiquidGlassTokens.AzureGlassLight,
    size: Dp = 56.dp
) {
    val haptic = LocalHapticFeedback.current

    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) MotionTokens.ScaleButtonPressed else 1f,
        animationSpec = if (isPressed) MotionTokens.SpecPressIn else MotionTokens.SpecPressOut,
        label = "glassFabScale"
    )

    // Layered liquid glass gradient
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            accentColorLight,
            accentColor,
            Color(0xFF0056B3)
        )
    )

    // Specular border: bright highlight at top, subtle at bottom
    val specularBorder = Brush.verticalGradient(
        colors = listOf(
            Color(0xE6FFFFFF),
            Color(0x66FFFFFF),
            Color(0x20FFFFFF)
        )
    )

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                shadowElevation = 12.dp.toPx()
                shape = CircleShape
                clip = false
            }
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                ambientColor = accentColor.copy(alpha = 0.45f),
                spotColor = accentColor.copy(alpha = 0.65f)
            )
            .clip(CircleShape)
            .background(gradientBrush)
            .border(1.25.dp, specularBorder, CircleShape)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = {
                        HapticUtil.performLightImpact(haptic)
                        onClick()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Subtle top specular reflection half-arc
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(0.75f)
                .height(size * 0.42f)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x55FFFFFF),
                            Color(0x00FFFFFF)
                        )
                    )
                )
        )

        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(26.dp)
        )
    }
}

/**
 * iOS 26 Liquid Glass Filter / Category Chip.
 * Capsule shape with smooth glass materials, specular border, and spring press response.
 */
@Composable
fun LiquidGlassFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    activeColor: Color = LiquidGlassTokens.AzureGlass
) {
    val haptic = LocalHapticFeedback.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) MotionTokens.ScaleButtonPressed else 1f,
        animationSpec = if (isPressed) MotionTokens.SpecPressIn else MotionTokens.SpecPressOut,
        label = "glassChipScale"
    )

    val backgroundBrush = if (selected) {
        Brush.verticalGradient(
            colors = listOf(
                activeColor,
                activeColor.copy(alpha = 0.82f)
            )
        )
    } else {
        if (isDark) {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0x331C2438),
                    Color(0x22131929)
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xCCFFFFFF),
                    Color(0xAAF0F4FA)
                )
            )
        }
    }

    val borderStroke = if (selected) {
        BorderStroke(1.dp, Color.White.copy(alpha = 0.55f))
    } else {
        BorderStroke(
            1.dp,
            if (isDark) Color.White.copy(alpha = 0.16f) else Color.Black.copy(alpha = 0.08f)
        )
    }

    val textColor = if (selected) {
        Color.White
    } else {
        if (isDark) LiquidGlassTokens.DarkTextSecondary else LiquidGlassTokens.LightTextSecondary
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(LiquidGlassTokens.ShapeCapsule)
            .background(backgroundBrush)
            .border(borderStroke, LiquidGlassTokens.ShapeCapsule)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = {
                        HapticUtil.performSelection(haptic)
                        onClick()
                    }
                )
            }
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    color = textColor,
                    letterSpacing = 0.1.sp
                )
            )
        }
    }
}

/**
 * iOS 26 Liquid Glass Sliding Toggle Switch.
 * Matches image_28547c8a.png:
 * Frosted pill track displaying "OFF" on left and "ON" on right.
 * A 3D convex white/frosted glass pill knob smoothly glides across with spring physics.
 * Displays "ON" in bold black on top of the knob when checked, or "OFF" when unchecked.
 */
@Composable
fun LiquidGlassToggleSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    // Knob offset animation
    val thumbOffsetFraction by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.75f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "glassToggleThumb"
    )

    val trackWidth = 100.dp
    val trackHeight = 40.dp
    val thumbWidth = 52.dp
    val thumbHeight = 34.dp

    val trackShape = RoundedCornerShape(20.dp)
    val thumbShape = RoundedCornerShape(17.dp)

    val trackBg = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF282E3A),
                Color(0xFF1E232E)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFE4E7ED),
                Color(0xFFD6DBE4)
            )
        )
    }

    val trackBorder = Brush.verticalGradient(
        colors = listOf(
            if (isDark) Color(0x33FFFFFF) else Color(0x80FFFFFF),
            if (isDark) Color(0x10FFFFFF) else Color(0x20000000)
        )
    )

    Box(
        modifier = modifier
            .size(width = trackWidth, height = trackHeight)
            .clip(trackShape)
            .background(trackBg)
            .border(1.dp, trackBorder, trackShape)
            .clickable(enabled = enabled) {
                HapticUtil.performSelection(haptic)
                onCheckedChange(!checked)
            }
            .padding(horizontal = 3.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        // Background labels: "OFF" on left, "ON" on right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "OFF",
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0xFF6B7280) else Color(0xFF757B88)
                )
            )
            Text(
                text = "ON",
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0xFF6B7280) else Color(0xFF757B88)
                )
            )
        }

        // Sliding Convex Knob: max travel = trackWidth - 6.dp padding - thumbWidth = 100 - 6 - 52 = 42.dp
        val maxOffsetDp = trackWidth - 6.dp - thumbWidth
        val thumbOffsetDp = maxOffsetDp * thumbOffsetFraction

        Box(
            modifier = Modifier
                .padding(start = thumbOffsetDp)
                .size(width = thumbWidth, height = thumbHeight)
                .shadow(
                    elevation = 4.dp,
                    shape = thumbShape,
                    ambientColor = Color.Black.copy(alpha = 0.25f),
                    spotColor = Color.Black.copy(alpha = 0.35f)
                )
                .clip(thumbShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFFFFF),
                            Color(0xFFF1F3F7),
                            Color(0xFFE2E6EE)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFFFFF),
                            Color(0x80FFFFFF),
                            Color(0x33B0B8C5)
                        )
                    ),
                    shape = thumbShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (thumbOffsetFraction > 0.5f) "ON" else "OFF",
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F141C),
                    letterSpacing = 0.3.sp
                )
            )
        }
    }
}

/**
 * iOS 26 Liquid Glass Segmented Pill Button.
 * Matches image_95d167b2.png and image_c7a46987.png:
 * Dual-action capsule container with frosted glass, specular edge highlight,
 * subtle middle divider, and tactile spring responsiveness on each half (< > or - +).
 */
@Composable
fun LiquidGlassSegmentedPill(
    leftIcon: ImageVector,
    onLeftClick: () -> Unit,
    rightIcon: ImageVector,
    onRightClick: () -> Unit,
    modifier: Modifier = Modifier,
    leftContentDescription: String? = null,
    rightContentDescription: String? = null,
    enabled: Boolean = true,
    width: Dp = 88.dp,
    height: Dp = 38.dp
) {
    val haptic = LocalHapticFeedback.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    var leftPressed by remember { mutableStateOf(false) }
    var rightPressed by remember { mutableStateOf(false) }

    val leftScale by animateFloatAsState(
        targetValue = if (leftPressed && enabled) 0.92f else 1f,
        animationSpec = MotionTokens.SpecPressIn,
        label = "pillLeftScale"
    )
    val rightScale by animateFloatAsState(
        targetValue = if (rightPressed && enabled) 0.92f else 1f,
        animationSpec = MotionTokens.SpecPressIn,
        label = "pillRightScale"
    )

    val capsuleShape = LiquidGlassTokens.ShapeCapsule
    val bgBrush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0x38FFFFFF),
                Color(0x18FFFFFF)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xCCFFFFFF),
                Color(0x99DCE6F5)
            )
        )
    }

    val borderBrush = Brush.verticalGradient(
        colors = listOf(
            if (isDark) Color(0x60FFFFFF) else Color(0xCCFFFFFF),
            if (isDark) Color(0x1AFFFFFF) else Color(0x30002048)
        )
    )

    val iconColor = if (isDark) Color.White else Color(0xFF0F172A)

    Box(
        modifier = modifier
            .size(width = width, height = height)
            .shadow(
                elevation = 2.dp,
                shape = capsuleShape,
                ambientColor = Color.Black.copy(alpha = 0.15f)
            )
            .clip(capsuleShape)
            .background(bgBrush)
            .border(1.dp, borderBrush, capsuleShape)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Half
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = leftScale
                        scaleY = leftScale
                    }
                    .pointerInput(enabled) {
                        detectTapGestures(
                            onPress = {
                                leftPressed = true
                                tryAwaitRelease()
                                leftPressed = false
                            },
                            onTap = {
                                HapticUtil.performLightImpact(haptic)
                                onLeftClick()
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = leftIcon,
                    contentDescription = leftContentDescription,
                    tint = if (enabled) iconColor else iconColor.copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp)
                )
            }

            // Translucent subtle vertical divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(20.dp)
                    .background(if (isDark) Color(0x26FFFFFF) else Color(0x33000000))
            )

            // Right Half
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = rightScale
                        scaleY = rightScale
                    }
                    .pointerInput(enabled) {
                        detectTapGestures(
                            onPress = {
                                rightPressed = true
                                tryAwaitRelease()
                                rightPressed = false
                            },
                            onTap = {
                                HapticUtil.performLightImpact(haptic)
                                onRightClick()
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = rightIcon,
                    contentDescription = rightContentDescription,
                    tint = if (enabled) iconColor else iconColor.copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * iOS 26 Liquid Glass Search Pill.
 * Matches image_95d167b2.png:
 * Translucent capsule with inner specular reflection, placeholder text,
 * and circular search/clear icon buttons.
 */
@Composable
fun LiquidGlassSearchPill(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search...",
    onSearch: (() -> Unit)? = null,
    onClear: (() -> Unit)? = null
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val shape = LiquidGlassTokens.ShapeCapsule
    val bgBrush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0x331C2436),
                Color(0x22121724)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xEEFFFFFF),
                Color(0xCCE8EFF9)
            )
        )
    }

    val borderBrush = Brush.verticalGradient(
        colors = listOf(
            if (isDark) Color(0x55FFFFFF) else Color(0xE6FFFFFF),
            if (isDark) Color(0x18FFFFFF) else Color(0x28002048)
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .shadow(2.dp, shape, ambientColor = Color.Black.copy(alpha = 0.12f))
            .clip(shape)
            .background(bgBrush)
            .border(1.dp, borderBrush, shape)
            .padding(start = 18.dp, end = 6.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = TextStyle(
                            fontSize = 15.sp,
                            color = if (isDark) LiquidGlassTokens.DarkTextTertiary else LiquidGlassTokens.LightTextTertiary
                        )
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        color = if (isDark) LiquidGlassTokens.DarkTextPrimary else LiquidGlassTokens.LightTextPrimary
                    ),
                    cursorBrush = SolidColor(LiquidGlassTokens.AzureGlass),
                    keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch?.invoke() }),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (value.isNotEmpty() && onClear != null) {
                LiquidGlassIconButton(
                    onClick = onClear,
                    icon = Icons.Filled.Close,
                    contentDescription = "Clear search",
                    size = 32.dp
                )
                Spacer(modifier = Modifier.width(4.dp))
            }

            LiquidGlassIconButton(
                onClick = { onSearch?.invoke() },
                icon = Icons.Filled.Search,
                contentDescription = "Search",
                size = 36.dp
            )
        }
    }
}



