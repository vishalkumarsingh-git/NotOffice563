package com.programmingblitz.notoffice563.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.programmingblitz.notoffice563.data.local.AppThemeMode
import com.programmingblitz.notoffice563.data.local.FileSortOrder
import com.programmingblitz.notoffice563.data.local.FileViewMode
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassBadge
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassButton
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassDialog
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassFilterChip
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassIconButton
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassSegmentedControl
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassSurface
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassToggleSwitch
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassTokens

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onOpenDrawer: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val prefs by viewModel.userPreferences.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    LaunchedEffect(Unit) {
        viewModel.messageEvents.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = if (isDark) LiquidGlassTokens.DarkBaseBackground else LiquidGlassTokens.LightBaseBackground,
        topBar = {
            // Authentic iOS 26 Liquid Glass Top Bar
            LiquidGlassSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = LiquidGlassTokens.ShapeMedium,
                isDark = isDark,
                elevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onOpenDrawer != null) {
                        LiquidGlassIconButton(
                            onClick = onOpenDrawer,
                            icon = Icons.Filled.Menu,
                            contentDescription = "Open navigation menu",
                            modifier = Modifier.testTag("open_drawer_button"),
                            size = 40.dp
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Settings",
                            style = TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) LiquidGlassTokens.DarkTextPrimary else LiquidGlassTokens.LightTextPrimary
                            )
                        )
                        Text(
                            text = "Liquid Glass Workspace Preferences",
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = if (isDark) LiquidGlassTokens.DarkTextSecondary else LiquidGlassTokens.LightTextSecondary
                            )
                        )
                    }

                    LiquidGlassBadge(
                        text = "v1.0.0",
                        color = LiquidGlassTokens.AzureGlass,
                        containerColor = LiquidGlassTokens.AzureGlassGlow
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Appearance Section (Theme mode & Dark Mode toggle)
            LiquidGlassSettingsCard(
                title = "Appearance & Visuals",
                subtitle = "Choose theme mode or toggle dark glass theme",
                icon = Icons.Filled.Brightness4,
                isDark = isDark
            ) {
                Text(
                    text = "Theme Palette Mode",
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) LiquidGlassTokens.DarkTextPrimary else LiquidGlassTokens.LightTextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Liquid Glass Segmented Control
                val themeOptions = listOf("System", "Light", "Dark")
                val currentThemeIndex = when (prefs.themeMode) {
                    AppThemeMode.SYSTEM -> 0
                    AppThemeMode.LIGHT -> 1
                    AppThemeMode.DARK -> 2
                }
                LiquidGlassSegmentedControl(
                    items = themeOptions,
                    selectedIndex = currentThemeIndex,
                    onSelectIndex = { index ->
                        when (index) {
                            0 -> viewModel.setTheme(AppThemeMode.SYSTEM)
                            1 -> viewModel.setTheme(AppThemeMode.LIGHT)
                            2 -> viewModel.setTheme(AppThemeMode.DARK)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = if (isDark) Color(0x1FFFFFFF) else Color(0x14000000))
                Spacer(modifier = Modifier.height(14.dp))

                // Liquid Glass Sliding Toggle Switch (Image Template)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Dark Glass Theme",
                            style = TextStyle(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) LiquidGlassTokens.DarkTextPrimary else LiquidGlassTokens.LightTextPrimary
                            )
                        )
                        Text(
                            text = "Deep translucent obsidian surfaces with specular rim glow",
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = if (isDark) LiquidGlassTokens.DarkTextSecondary else LiquidGlassTokens.LightTextSecondary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    LiquidGlassToggleSwitch(
                        checked = prefs.themeMode == AppThemeMode.DARK || (prefs.themeMode == AppThemeMode.SYSTEM && isDark),
                        onCheckedChange = { isChecked ->
                            viewModel.setTheme(if (isChecked) AppThemeMode.DARK else AppThemeMode.LIGHT)
                        }
                    )
                }
            }

            // 2. Workspace Display Section
            LiquidGlassSettingsCard(
                title = "Workspace Display",
                subtitle = "Configure file listing layout and default sort order",
                icon = Icons.Filled.Tune,
                isDark = isDark
            ) {
                // View Mode Toggle Switch (Image Template)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Grid Layout Mode",
                            style = TextStyle(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) LiquidGlassTokens.DarkTextPrimary else LiquidGlassTokens.LightTextPrimary
                            )
                        )
                        Text(
                            text = if (prefs.viewMode == FileViewMode.GRID) "2-column visual grid cards" else "Compact structured list rows",
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = if (isDark) LiquidGlassTokens.DarkTextSecondary else LiquidGlassTokens.LightTextSecondary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    LiquidGlassToggleSwitch(
                        checked = prefs.viewMode == FileViewMode.GRID,
                        onCheckedChange = { isChecked ->
                            viewModel.setViewMode(if (isChecked) FileViewMode.GRID else FileViewMode.LIST)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = if (isDark) Color(0x1FFFFFFF) else Color(0x14000000))
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Default File Sort Order",
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) LiquidGlassTokens.DarkTextPrimary else LiquidGlassTokens.LightTextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Sort Order Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FileSortOrder.entries.forEach { sort ->
                        val isSelected = prefs.sortOrder == sort
                        LiquidGlassFilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setSortOrder(sort) },
                            label = sort.title,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 3. Storage & History Section
            LiquidGlassSettingsCard(
                title = "Document History & Cache",
                subtitle = "Manage cached recent documents without affecting storage",
                icon = Icons.Filled.History,
                isDark = isDark
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Clear Recent History",
                            style = TextStyle(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) LiquidGlassTokens.DarkTextPrimary else LiquidGlassTokens.LightTextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Purge recent document records; actual device files remain safe",
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = if (isDark) LiquidGlassTokens.DarkTextSecondary else LiquidGlassTokens.LightTextSecondary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    LiquidGlassButton(
                        text = "Clear",
                        icon = Icons.Filled.DeleteSweep,
                        onClick = { showClearConfirmDialog = true },
                        accentColor = LiquidGlassTokens.CrimsonGlass,
                        isPrimary = false,
                        height = 38.dp
                    )
                }
            }

            // 4. Privacy & Local Architecture
            LiquidGlassSettingsCard(
                title = "Privacy & Security Guarantee",
                subtitle = "Strictly on-device processing via Android Storage Access Framework",
                icon = Icons.Filled.Shield,
                isDark = isDark
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(LiquidGlassTokens.EmeraldGlass)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "100% On-Device: Parsing, editing, and rendering run locally.",
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = if (isDark) LiquidGlassTokens.DarkTextSecondary else LiquidGlassTokens.LightTextSecondary
                            )
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(LiquidGlassTokens.AzureGlass)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Storage Access Framework: Granular file and folder access permissions.",
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = if (isDark) LiquidGlassTokens.DarkTextSecondary else LiquidGlassTokens.LightTextSecondary
                            )
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(LiquidGlassTokens.AmberGlass)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Zero Telemetry: No background document telemetry or cloud uploads.",
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = if (isDark) LiquidGlassTokens.DarkTextSecondary else LiquidGlassTokens.LightTextSecondary
                            )
                        )
                    }
                }
            }

            // 5. About NotOffice563
            LiquidGlassSettingsCard(
                title = "About NotOffice563",
                subtitle = "Next-generation iOS 26 Liquid Glass document suite",
                icon = Icons.Filled.Info,
                isDark = isDark
            ) {
                Text(
                    text = "NotOffice563 Document Workspace",
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = LiquidGlassTokens.AzureGlass
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "A modern mobile office suite crafted with pure Kotlin and Jetpack Compose. Combines real OpenXML (DOCX, XLSX, PPTX) and native PdfRenderer capabilities with an authentic iOS 26 Liquid Glass aesthetic.",
                    style = TextStyle(
                        fontSize = 13.sp,
                        color = if (isDark) LiquidGlassTokens.DarkTextPrimary else LiquidGlassTokens.LightTextPrimary,
                        lineHeight = 18.sp
                    )
                )
            }
        }
    }

    if (showClearConfirmDialog) {
        LiquidGlassDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = "Clear Recent History?",
            isDark = isDark,
            confirmButton = {
                LiquidGlassButton(
                    text = "Clear",
                    onClick = {
                        showClearConfirmDialog = false
                        viewModel.clearRecentFiles()
                    },
                    accentColor = LiquidGlassTokens.CrimsonGlass,
                    isPrimary = true,
                    height = 38.dp
                )
            },
            dismissButton = {
                LiquidGlassButton(
                    text = "Cancel",
                    onClick = { showClearConfirmDialog = false },
                    isPrimary = false,
                    height = 38.dp
                )
            }
        ) {
            Text(
                text = "Are you sure you want to clear your document history? Original files on device storage will remain untouched.",
                style = TextStyle(
                    fontSize = 14.sp,
                    color = if (isDark) LiquidGlassTokens.DarkTextSecondary else LiquidGlassTokens.LightTextSecondary
                )
            )
        }
    }
}

/**
 * Reusable Liquid Glass Section Card for settings.
 */
@Composable
private fun LiquidGlassSettingsCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isDark: Boolean,
    content: @Composable () -> Unit
) {
    LiquidGlassSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = LiquidGlassTokens.ShapeLarge,
        isDark = isDark,
        elevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Circular icon lens
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            if (isDark) Color(0x33007AFF) else Color(0x1F007AFF)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = LiquidGlassTokens.AzureGlass,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = title,
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) LiquidGlassTokens.DarkTextPrimary else LiquidGlassTokens.LightTextPrimary
                        )
                    )
                    Text(
                        text = subtitle,
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = if (isDark) LiquidGlassTokens.DarkTextSecondary else LiquidGlassTokens.LightTextSecondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = if (isDark) Color(0x1FFFFFFF) else Color(0x14000000))
            Spacer(modifier = Modifier.height(14.dp))

            content()
        }
    }
}
