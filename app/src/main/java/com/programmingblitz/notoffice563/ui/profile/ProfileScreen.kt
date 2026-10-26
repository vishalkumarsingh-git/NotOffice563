package com.programmingblitz.notoffice563.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.programmingblitz.notoffice563.domain.model.UserAccount
import com.programmingblitz.notoffice563.ui.theme.AccentBlue
import com.programmingblitz.notoffice563.ui.theme.IncomeGreen
import com.programmingblitz.notoffice563.ui.theme.SleekShapes
import com.programmingblitz.notoffice563.ui.theme.SleekSizes
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassCard
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassSurface
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassTokens

/**
 * Profile Screen — Focused exclusively on account identity, Google Sign-In state,
 * and account permissions, without duplicating general settings.
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
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()
    val setupNotice by viewModel.setupNotice.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassTopBar(
                title = "Account Profile",
                subtitle = if (currentUser != null) "Google Account Connected" else "Local Workspace Mode",
                navigationIcon = {
                    com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassIconButton(
                        onClick = onOpenDrawer,
                        icon = Icons.Filled.Menu,
                        contentDescription = "Open navigation drawer",
                        size = 36.dp
                    )
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Account Identity Hero Card
            item {
                AccountCard(
                    currentUser = currentUser,
                    isLoading = isLoading,
                    setupNotice = setupNotice,
                    isDark = isDark,
                    onSignIn = { viewModel.signInWithGoogle(context) },
                    onSignOut = { viewModel.requestSignOutConfirmation() }
                )
            }

            // 2. Account Security & Privacy
            item {
                LiquidGlassCard(isDark = isDark) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(AccentBlue.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Shield,
                                    contentDescription = null,
                                    tint = AccentBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Account Security & Privacy",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) LiquidGlassTokens.DarkTextPrimary else LiquidGlassTokens.LightTextPrimary
                                )
                                Text(
                                    text = "Strict local document protection",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isDark) LiquidGlassTokens.DarkTextSecondary else LiquidGlassTokens.LightTextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "• Google Sign-In is optional for profile identification.",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDark) LiquidGlassTokens.DarkTextSecondary else LiquidGlassTokens.LightTextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Local Document Isolation: Your Word, Excel, PowerPoint, and PDF files are stored locally and never synced or uploaded.",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDark) LiquidGlassTokens.DarkTextSecondary else LiquidGlassTokens.LightTextSecondary
                        )
                    }
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
        is ProfileDialogState.Error -> {
            com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassDialog(
                onDismissRequest = { viewModel.dismissDialog() },
                title = "Authentication Notice",
                isDark = isDark,
                confirmButton = {
                    com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassButton(
                        text = "OK",
                        onClick = { viewModel.dismissDialog() },
                        height = 38.dp
                    )
                }
            ) {
                Text(
                    text = dialog.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDark) LiquidGlassTokens.DarkTextSecondary else LiquidGlassTokens.LightTextSecondary
                )
            }
        }
        is ProfileDialogState.ConfirmSignOut -> {
            com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassDialog(
                onDismissRequest = { viewModel.dismissDialog() },
                title = "Sign Out from Google?",
                isDark = isDark,
                confirmButton = {
                    com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassButton(
                        text = "Sign Out",
                        onClick = { viewModel.confirmSignOut() },
                        accentColor = MaterialTheme.colorScheme.error,
                        height = 38.dp
                    )
                },
                dismissButton = {
                    com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassButton(
                        text = "Cancel",
                        onClick = { viewModel.dismissDialog() },
                        isPrimary = false,
                        height = 38.dp
                    )
                }
            ) {
                Text(
                    text = "Signing out will return to Local Workspace Mode. Your local documents and notes remain intact.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDark) LiquidGlassTokens.DarkTextSecondary else LiquidGlassTokens.LightTextSecondary
                )
            }
        }
        else -> {}
    }
}

@Composable
private fun AccountCard(
    currentUser: UserAccount?,
    isLoading: Boolean,
    setupNotice: String?,
    isDark: Boolean,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    LiquidGlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("profile_account_card"),
        shape = LiquidGlassTokens.ShapeLarge,
        isDark = isDark,
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (currentUser != null) {
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
                    color = if (isDark) LiquidGlassTokens.DarkTextPrimary else LiquidGlassTokens.LightTextPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!currentUser.email.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = currentUser.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDark) LiquidGlassTokens.DarkTextSecondary else LiquidGlassTokens.LightTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassButton(
                    text = "Sign Out",
                    onClick = onSignOut,
                    isPrimary = false,
                    height = 40.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sign_out_button")
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0x33FFFFFF) else Color(0x1F000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Neutral Avatar",
                        tint = if (isDark) LiquidGlassTokens.DarkTextSecondary else LiquidGlassTokens.LightTextSecondary,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Local Workspace Mode",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) LiquidGlassTokens.DarkTextPrimary else LiquidGlassTokens.LightTextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Google sign-in is optional. Local document browsing, viewing, editing, and saving work 100% offline without signing in.",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) LiquidGlassTokens.DarkTextSecondary else LiquidGlassTokens.LightTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassButton(
                    text = if (isLoading) "Connecting..." else "Sign in with Google",
                    onClick = onSignIn,
                    isLoading = isLoading,
                    accentColor = AccentBlue,
                    height = 46.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("google_sign_in_button")
                )

                if (!setupNotice.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(LiquidGlassTokens.ShapeSmall)
                            .background(if (isDark) Color(0x261E283E) else Color(0x1F000000))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                tint = AccentBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = setupNotice,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDark) LiquidGlassTokens.DarkTextSecondary else LiquidGlassTokens.LightTextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
