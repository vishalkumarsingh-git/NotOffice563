package com.programmingblitz.notoffice563.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassButton
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassIconButton
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassTokens
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.programmingblitz.notoffice563.data.local.AppThemeMode
import com.programmingblitz.notoffice563.data.local.FileSortOrder
import com.programmingblitz.notoffice563.data.local.FileViewMode
import com.programmingblitz.notoffice563.ui.theme.AccentBlue
import com.programmingblitz.notoffice563.ui.theme.AccentBlueContainer
import com.programmingblitz.notoffice563.ui.theme.ExpenseRed
import com.programmingblitz.notoffice563.ui.theme.OnAccentBlueContainer
import com.programmingblitz.notoffice563.ui.theme.SleekShapes
import com.programmingblitz.notoffice563.ui.theme.SleekSizes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onOpenDrawer: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val prefs by viewModel.userPreferences.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.messageEvents.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if (onOpenDrawer != null) {
                        IconButton(
                            onClick = onOpenDrawer,
                            modifier = Modifier.testTag("open_drawer_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Open navigation drawer",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                },
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Appearance Section
            SettingsSectionCard(
                title = "Appearance",
                subtitle = "Choose light, dark, or system matching theme",
                icon = Icons.Filled.Brightness4
            ) {
                Text(
                    text = "Theme Mode",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeSelectTile(
                        label = "System",
                        selected = prefs.themeMode == AppThemeMode.SYSTEM,
                        onClick = { viewModel.setTheme(AppThemeMode.SYSTEM) },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeSelectTile(
                        label = "Light",
                        selected = prefs.themeMode == AppThemeMode.LIGHT,
                        onClick = { viewModel.setTheme(AppThemeMode.LIGHT) },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeSelectTile(
                        label = "Dark",
                        selected = prefs.themeMode == AppThemeMode.DARK,
                        onClick = { viewModel.setTheme(AppThemeMode.DARK) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 2. Workspace Preferences
            SettingsSectionCard(
                title = "Workspace Display",
                subtitle = "Configure file listing and default order",
                icon = Icons.Filled.Tune
            ) {
                Text(
                    text = "Default View Mode",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeSelectTile(
                        label = "List View",
                        selected = prefs.viewMode == FileViewMode.LIST,
                        onClick = { viewModel.setViewMode(FileViewMode.LIST) },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeSelectTile(
                        label = "Grid View",
                        selected = prefs.viewMode == FileViewMode.GRID,
                        onClick = { viewModel.setViewMode(FileViewMode.GRID) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Default Sort Order",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    FileSortOrder.entries.forEach { sort ->
                        val isSelected = prefs.sortOrder == sort
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(SleekShapes.input)
                                .clickable { viewModel.setSortOrder(sort) }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = if (isSelected) 6.dp else 1.5.dp,
                                        color = if (isSelected) AccentBlue else MaterialTheme.colorScheme.outline,
                                        shape = CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = sort.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) AccentBlue else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // 3. Storage & History
            SettingsSectionCard(
                title = "Document History",
                subtitle = "Manage your cached recent document records",
                icon = Icons.Filled.History
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Clear Recent History",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Clear document list cache without deleting original files",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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

            // 4. Privacy & Architecture
            SettingsSectionCard(
                title = "Privacy & Local Guarantee",
                subtitle = "Strictly on-device storage with zero external tracking",
                icon = Icons.Filled.Shield
            ) {
                Text(
                    text = "• 100% On-Device: All document rendering, parsing, and notes are processed locally.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• No Cloud Sync: Documents never leave your device storage.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• Storage Access Framework: Granular read/write permissions via Android SAF.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 5. About NotOffice563
            SettingsSectionCard(
                title = "About NotOffice563",
                subtitle = "Independent Android document workspace",
                icon = Icons.Filled.Info
            ) {
                Text(
                    text = "NotOffice563 v1.0.0",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Designed with a clean, restrained Material 3 language for a focused document workspace.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "An independent open document workspace. Not affiliated with Microsoft Corporation.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            shape = SleekShapes.dialog,
            title = {
                Text(
                    text = "Clear Recent History?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to clear your document history? Original files on device storage will remain untouched.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                LiquidGlassButton(
                    text = "Clear",
                    onClick = {
                        showClearConfirmDialog = false
                        viewModel.clearRecentFiles()
                    },
                    accentColor = LiquidGlassTokens.CrimsonGlass,
                    isPrimary = true
                )
            },
            dismissButton = {
                LiquidGlassButton(
                    text = "Cancel",
                    onClick = { showClearConfirmDialog = false },
                    isPrimary = false
                )
            }
        )
    }
}

/**
 * Reusable Section Card for settings:
 * 16dp rounded card with 1dp border, circular tinted icon, bold title and clean content.
 */
@Composable
private fun SettingsSectionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    Card(
        shape = SleekShapes.cardLarge,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Circular icon container
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AccentBlue.copy(alpha = if (isDark) 0.2f else 0.1f))
                        .border(1.dp, AccentBlue.copy(alpha = 0.25f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = AccentBlue,
                        modifier = Modifier.size(SleekSizes.iconSmall)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(14.dp))

            content()
        }
    }
}

/**
 * Sleek tile for selecting mode (System, Light, Dark, List, Grid).
 */
@Composable
private fun ThemeSelectTile(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerBg = if (selected) AccentBlueContainer else MaterialTheme.colorScheme.surface
    val borderStroke = if (selected) {
        BorderStroke(1.5.dp, AccentBlue)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    }

    Surface(
        onClick = onClick,
        shape = SleekShapes.input,
        color = containerBg,
        border = borderStroke,
        modifier = modifier.height(SleekSizes.buttonSmall)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) OnAccentBlueContainer else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
