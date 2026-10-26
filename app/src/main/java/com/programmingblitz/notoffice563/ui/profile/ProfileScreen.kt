package com.programmingblitz.notoffice563.ui.profile

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.programmingblitz.notoffice563.data.local.AppThemeMode
import com.programmingblitz.notoffice563.data.local.FileSortOrder
import com.programmingblitz.notoffice563.data.local.FileViewMode
import com.programmingblitz.notoffice563.domain.model.UserAccount
import com.programmingblitz.notoffice563.ui.theme.AccentBlue
import com.programmingblitz.notoffice563.ui.theme.IncomeGreen
import com.programmingblitz.notoffice563.ui.theme.SleekShapes
import com.programmingblitz.notoffice563.ui.theme.SleekSizes
import com.programmingblitz.notoffice563.util.HapticUtil

/**
 * Redesigned Profile & Settings Screen with clean, minimalist aesthetics.
 * Strictly honest: Never fabricates names, emails, or fake authentication.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val userPrefs by viewModel.userPreferences.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()
    val setupNotice by viewModel.setupNotice.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            HapticUtil.performSelection(haptic)
                            onOpenDrawer()
                        },
                        modifier = Modifier
                            .testTag("open_drawer_button")
                            .size(SleekSizes.buttonSmall)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Open navigation drawer",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Profile & Settings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (currentUser != null) "Google Account Connected" else "Local Workspace Mode",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Account Section
            item {
                AccountCard(
                    currentUser = currentUser,
                    isLoading = isLoading,
                    setupNotice = setupNotice,
                    onSignIn = {
                        HapticUtil.performAction(haptic)
                        viewModel.signInWithGoogle(context)
                    },
                    onSignOut = {
                        HapticUtil.performAction(haptic)
                        viewModel.requestSignOutConfirmation()
                    }
                )
            }

            // 2. Appearance Section
            item {
                ProfileSectionCard(
                    title = "Appearance",
                    subtitle = "Select application visual theme",
                    icon = Icons.Filled.Palette
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeOptionTile(
                            label = "System",
                            selected = userPrefs.themeMode == AppThemeMode.SYSTEM,
                            onClick = {
                                HapticUtil.performAction(haptic)
                                viewModel.setTheme(AppThemeMode.SYSTEM)
                            },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionTile(
                            label = "Light",
                            selected = userPrefs.themeMode == AppThemeMode.LIGHT,
                            onClick = {
                                HapticUtil.performAction(haptic)
                                viewModel.setTheme(AppThemeMode.LIGHT)
                            },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionTile(
                            label = "Dark",
                            selected = userPrefs.themeMode == AppThemeMode.DARK,
                            onClick = {
                                HapticUtil.performAction(haptic)
                                viewModel.setTheme(AppThemeMode.DARK)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 3. Workspace Preferences Section
            item {
                ProfileSectionCard(
                    title = "Preferences",
                    subtitle = "Configure document layout and sorting",
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
                        ThemeOptionTile(
                            label = "List View",
                            selected = userPrefs.viewMode == FileViewMode.LIST,
                            onClick = {
                                HapticUtil.performAction(haptic)
                                viewModel.setViewMode(FileViewMode.LIST)
                            },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionTile(
                            label = "Grid View",
                            selected = userPrefs.viewMode == FileViewMode.GRID,
                            onClick = {
                                HapticUtil.performAction(haptic)
                                viewModel.setViewMode(FileViewMode.GRID)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Default Sort Order",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        FileSortOrder.entries.forEach { sort ->
                            val isSelected = userPrefs.sortOrder == sort
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(SleekShapes.input)
                                    .clickable {
                                        HapticUtil.performSelection(haptic)
                                        viewModel.setSortOrder(sort)
                                    }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .border(
                                            width = if (isSelected) 5.dp else 1.5.dp,
                                            color = if (isSelected) AccentBlue else MaterialTheme.colorScheme.outline,
                                            shape = CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(10.dp))
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
            }

            // 4. Privacy & Architecture Section
            item {
                ProfileSectionCard(
                    title = "Privacy & Data Guarantee",
                    subtitle = "100% On-Device local processing",
                    icon = Icons.Filled.Shield
                ) {
                    Text(
                        text = "• Local Storage Only: Documents opened in NotOffice563 remain entirely on your device storage.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Android SAF Permissions: File access is granted strictly through Android's Storage Access Framework.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Zero File Telemetry: Signing in with Google does not upload, sync, or transmit any user documents to cloud servers.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 5. About Section
            item {
                ProfileSectionCard(
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
                        text = "An independent open document viewer and lightweight editor. Not affiliated with Microsoft Corporation.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Dock spacing
            item {
                Spacer(modifier = Modifier.height(96.dp))
            }
        }
    }

    // Dialogs
    when (val dialog = dialogState) {
        is ProfileDialogState.MissingConfig -> {
            // Handled non-blockingly via inline setup notice in AccountCard
        }
        is ProfileDialogState.Error -> {
            AlertDialog(
                onDismissRequest = { viewModel.dismissDialog() },
                shape = SleekShapes.dialog,
                title = {
                    Text(
                        text = "Authentication Notice",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = dialog.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.dismissDialog() },
                        shape = SleekShapes.chip
                    ) {
                        Text("OK")
                    }
                }
            )
        }
        is ProfileDialogState.ConfirmSignOut -> {
            AlertDialog(
                onDismissRequest = { viewModel.dismissDialog() },
                shape = SleekShapes.dialog,
                title = {
                    Text(
                        text = "Sign out from Google?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "Signing out will disconnect your Google account and return to Local Workspace Mode. Your local documents and notes remain intact.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            HapticUtil.performAction(haptic)
                            viewModel.confirmSignOut()
                        },
                        shape = SleekShapes.chip,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text("Sign Out")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissDialog() }) {
                        Text("Cancel")
                    }
                }
            )
        }
        null -> {}
    }
}

/**
 * Account Hero Card: Displays real profile info when signed in,
 * or neutral placeholder + prominent Google Sign In button when signed out.
 */
@Composable
private fun AccountCard(
    currentUser: UserAccount?,
    isLoading: Boolean,
    setupNotice: String?,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("profile_account_card"),
        shape = SleekShapes.cardLarge,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (currentUser != null) {
                // SIGNED-IN STATE (Real details only)
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .border(2.dp, AccentBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (!currentUser.photoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = currentUser.photoUrl,
                            contentDescription = "Google Profile Picture",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentUser.getInitials(),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = currentUser.displayName ?: currentUser.email ?: "Google Account",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!currentUser.email.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = currentUser.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Status chip
                Surface(
                    shape = SleekShapes.pill,
                    color = IncomeGreen.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, IncomeGreen.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(IncomeGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Connected with Google",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = IncomeGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sign Out Action
                OutlinedButton(
                    onClick = onSignOut,
                    modifier = Modifier
                        .testTag("sign_out_button")
                        .fillMaxWidth()
                        .height(SleekSizes.buttonDefault),
                    shape = SleekShapes.chip,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Sign out",
                        modifier = Modifier.size(SleekSizes.iconSmall)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign out", fontWeight = FontWeight.SemiBold)
                }
            } else {
                // SIGNED-OUT STATE (Clean neutral avatar, prominent Google button)
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Neutral Avatar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Not signed in",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Google sign-in is optional. All local document viewing, editing, and note features are fully functional without signing in.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Prominent Sign in with Google Button
                Button(
                    onClick = onSignIn,
                    enabled = !isLoading,
                    modifier = Modifier
                        .testTag("google_sign_in_button")
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = SleekShapes.chip,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Connecting...",
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        GoogleLogoIcon(modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Sign in with Google",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                if (!setupNotice.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        shape = SleekShapes.card,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = setupNotice,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Standard Google 'G' 4-color logo drawn cleanly with Compose Canvas paths.
 */
@Composable
fun GoogleLogoIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val size = this.size.minDimension
        val strokeWidth = size * 0.2f
        val radius = (size - strokeWidth) / 2f
        val center = Offset(size / 2f, size / 2f)

        // Blue right-arm & upper right arc
        val blueColor = Color(0xFF4285F4)
        val redColor = Color(0xFFEA4335)
        val yellowColor = Color(0xFFFBBC05)
        val greenColor = Color(0xFF34A853)

        val arcRect = Rect(center.x - radius, center.y - radius, center.x + radius, center.y + radius)

        // Red (top arc ~ 210 to 330 deg)
        drawArc(
            color = redColor,
            startAngle = 200f,
            sweepAngle = 140f,
            useCenter = false,
            style = Stroke(width = strokeWidth)
        )

        // Yellow (left arc ~ 140 to 200 deg)
        drawArc(
            color = yellowColor,
            startAngle = 130f,
            sweepAngle = 80f,
            useCenter = false,
            style = Stroke(width = strokeWidth)
        )

        // Green (bottom arc ~ 30 to 140 deg)
        drawArc(
            color = greenColor,
            startAngle = 45f,
            sweepAngle = 95f,
            useCenter = false,
            style = Stroke(width = strokeWidth)
        )

        // Blue (right arc and center horizontal bar)
        drawArc(
            color = blueColor,
            startAngle = -20f,
            sweepAngle = 65f,
            useCenter = false,
            style = Stroke(width = strokeWidth)
        )

        // Center crossbar
        drawLine(
            color = blueColor,
            start = Offset(center.x - strokeWidth * 0.2f, center.y),
            end = Offset(center.x + radius + strokeWidth / 2f, center.y),
            strokeWidth = strokeWidth
        )
    }
}

@Composable
private fun ProfileSectionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = SleekShapes.cardLarge,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(SleekShapes.card)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(SleekSizes.iconSmall)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(14.dp))

            content()
        }
    }
}

@Composable
private fun ThemeOptionTile(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg by animateColorAsState(
        targetValue = if (selected) AccentBlue else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "themeBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
        label = "themeText"
    )

    Surface(
        shape = SleekShapes.chip,
        color = bg,
        border = BorderStroke(
            1.dp,
            if (selected) AccentBlue else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = modifier
            .height(42.dp)
            .clip(SleekShapes.chip)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = textColor
            )
        }
    }
}
