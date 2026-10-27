/**
 * =========================================================================================
 * NotOffice563 — Unified Local Document Workspace for Android
 * =========================================================================================
 *
 * 1. WHAT NOTOFFICE563 IS:
 * NotOffice563 is an independent local Android productivity workspace designed for opening,
 * viewing, organizing, searching, and managing Microsoft Office documents (.docx, .doc,
 * .xlsx, .xls, .pptx, .ppt) and Adobe PDF files (.pdf) in one place.
 *
 * 2. HOW TO OPEN THE PROJECT:
 * - Launch Android Studio (Electric Eel, Hedgehog, Iguana, or later).
 * - Choose "Open" and select the root directory of this project.
 * - Allow Gradle to sync dependencies automatically.
 *
 * 3. HOW TO RUN ON AN EMULATOR:
 * - In Android Studio, open Device Manager (Tools > Device Manager).
 * - Create or start an Android Virtual Device running Android 10 (API 29) or higher.
 * - Click the green "Run 'app'" button (Shift+F10) in the toolbar.
 *
 * 4. HOW TO RUN ON A PHYSICAL ANDROID DEVICE:
 * - Enable "Developer options" and "USB debugging" on your Android device (Settings > About Phone > tap Build 7 times).
 * - Connect the device to your computer via USB.
 * - Select the connected device in the Android Studio run configurations dropdown.
 * - Click Run.
 *
 * 5. HOW STORAGE ACCESS FRAMEWORK (SAF) IS USED:
 * - Storage Access Framework (SAF) via ActivityResultContracts.OpenDocument is used for all file picking.
 * - The app takes persistable URI permissions (FLAG_GRANT_READ_URI_PERMISSION & FLAG_GRANT_WRITE_URI_PERMISSION)
 *   via ContentResolver.takePersistableUriPermission(uri, ...).
 * - This grants long-term access to the user's chosen documents without demanding broad or dangerous storage
 *   permissions (e.g. MANAGE_EXTERNAL_STORAGE), strictly complying with Google Play Privacy policies.
 *
 * 6. WHICH FEATURES ARE FULLY IMPLEMENTED:
 * - PDF Viewer: Native Android PdfRenderer with page bitmap rendering, zoom factors, jump-to-page, and local annotations.
 * - Word Viewer (.docx): OpenXML parser extracting paragraphs, headings, bullet lists, bold/italic runs, word counts.
 * - Basic Word Editing: Title and plain-text editing with direct compilation and export to a new .docx archive.
 * - Spreadsheet Viewer (.xlsx): Multi-sheet OpenXML parser extracting shared string table, worksheet cells, row numbers,
 *   column letters (A-Z), and cell formula/value inspector.
 * - Presentation Viewer (.pptx): Slide deck extractor parsing slide titles, bullet points, and speaker notes.
 * - Storage Overview: Real-time query of device storage capacity (used/free GB) via StatFs.
 * - Persistence & Metadata: Room database tracking recent files, favorites, reading positions, and page notes.
 * - Preferences: AndroidX DataStore managing themes (System/Light/Dark), sort order, view modes (List/Grid), and limits.
 * - Universal Search: Real-time query across all registered documents by filename, extension, and category.
 *
 * 7. WHICH FEATURES ARE SIMPLIFIED / FALLBACKS:
 * - Legacy Formats (.doc, .xls, .ppt): Older binary compound formats use honest fallback screens with
 *   "Open with another app", "Share", and file details, preventing crashes on unsupported legacy structures.
 * - Pixel-perfect layout: Office documents are parsed for readable content and structured typography rather
 *   than trying to replicate a multi-thousand-feature desktop office rendering engine.
 * - Annotations: PDF annotations are saved locally associated with (URI + page) in Room, preserving the original file.
 *
 * 8. EXTENSION POINTS:
 * - Cloud providers (Google Drive, OneDrive, Nextcloud) via custom SAF DocumentsProvider adapters.
 * - Advanced formula evaluation engine for Excel sheets.
 * - Export to PDF from DOCX/XLSX/PPTX.
 *
 * 9. DISCLAIMER:
 * NotOffice563 is an independent project and is NOT affiliated with, sponsored by, or endorsed
 * by Microsoft Corporation.
 * =========================================================================================
 */
package com.programmingblitz.notoffice563

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.programmingblitz.notoffice563.data.local.AppDatabase
import com.programmingblitz.notoffice563.data.local.AppPreferences
import com.programmingblitz.notoffice563.data.local.AppThemeMode
import com.programmingblitz.notoffice563.data.repository.DocumentRepository
import com.programmingblitz.notoffice563.domain.model.DocumentCategory
import com.programmingblitz.notoffice563.ui.files.FilesScreen
import com.programmingblitz.notoffice563.ui.files.FilesViewModel
import com.programmingblitz.notoffice563.ui.home.HomeScreen
import com.programmingblitz.notoffice563.ui.home.HomeViewModel
import com.programmingblitz.notoffice563.ui.navigation.Screen
import com.programmingblitz.notoffice563.ui.recent.RecentScreen
import com.programmingblitz.notoffice563.ui.recent.RecentViewModel
import com.programmingblitz.notoffice563.ui.search.SearchScreen
import com.programmingblitz.notoffice563.ui.search.SearchViewModel
import com.programmingblitz.notoffice563.ui.settings.SettingsScreen
import com.programmingblitz.notoffice563.ui.settings.SettingsViewModel
import com.programmingblitz.notoffice563.ui.theme.NotOfficeTheme
import com.programmingblitz.notoffice563.ui.viewer.ViewerScreen
import com.programmingblitz.notoffice563.ui.viewer.ViewerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import androidx.activity.compose.BackHandler
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import com.programmingblitz.notoffice563.data.auth.AuthManager
import com.programmingblitz.notoffice563.ui.navigation.NotOfficeSidebar
import com.programmingblitz.notoffice563.ui.profile.ProfileScreen
import com.programmingblitz.notoffice563.ui.profile.ProfileViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val incomingUri = handleIncomingIntent(intent)

        setContent {
            val context = LocalContext.current
            val database = remember { AppDatabase.getInstance(context) }
            val preferences = remember { AppPreferences(context) }
            val repository = remember {
                DocumentRepository(
                    recentFileDao = database.recentFileDao(),
                    documentNoteDao = database.documentNoteDao()
                )
            }

            val userPrefs by preferences.userPreferences.collectAsStateWithLifecycle(
                initialValue = com.programmingblitz.notoffice563.data.local.UserPreferences()
            )

            val isDarkTheme = when (userPrefs.themeMode) {
                AppThemeMode.SYSTEM -> isSystemInDarkTheme()
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
            }

            // Purge any previously created sample files and deduplicate Room DB entries on app launch
            LaunchedEffect(Unit) {
                withContext(Dispatchers.IO) {
                    try {
                        val sampleFileNames = listOf(
                            "Sample_Research_Notes.pdf",
                            "Sample_Project_Proposal.docx",
                            "Sample_Quarterly_Budget.xlsx",
                            "Sample_Company_Overview.pptx"
                        )
                        for (name in sampleFileNames) {
                            val f = File(context.filesDir, name)
                            if (f.exists()) {
                                f.delete()
                            }
                        }
                        val currentFiles = repository.allRecentFiles.first()
                        val seenNames = mutableSetOf<String>()
                        for (file in currentFiles) {
                            val key = file.displayName.trim().lowercase()
                            if (seenNames.contains(key) || file.displayName.startsWith("Sample_") || file.uri.contains("Sample_")) {
                                repository.removeRecent(file.uri)
                            } else {
                                seenNames.add(key)
                            }
                        }
                    } catch (_: Exception) {
                    }
                }
            }

            NotOfficeTheme(darkTheme = isDarkTheme) {
                MainAppContent(
                    repository = repository,
                    preferences = preferences,
                    incomingUri = incomingUri
                )
            }
        }
    }

    private fun handleIncomingIntent(intent: Intent?): String? {
        if (intent == null) return null
        if (intent.action == Intent.ACTION_VIEW || intent.action == Intent.ACTION_EDIT) {
            val data: Uri? = intent.data
            return data?.toString()
        }
        return null
    }
}

@Composable
fun MainAppContent(
    repository: DocumentRepository,
    preferences: AppPreferences,
    incomingUri: String?
) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context, preferences) }
    val currentUser by authManager.currentUser.collectAsStateWithLifecycle(initialValue = null)

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Handle back button to close drawer when open
    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    // Direct opening if launched with an external document VIEW intent
    LaunchedEffect(incomingUri) {
        if (incomingUri != null) {
            navController.navigate(Screen.Viewer.createRoute(incomingUri))
        }
    }

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Files.route,
        Screen.Recent.route,
        Screen.Settings.route,
        Screen.Profile.route
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = showBottomBar,
        drawerContent = {
            NotOfficeSidebar(
                currentRoute = currentRoute,
                currentUser = currentUser,
                onNavigate = { screen ->
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onCloseDrawer = {
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (showBottomBar) {
                    com.programmingblitz.notoffice563.ui.navigation.NotOfficeDockBar(
                        currentRoute = currentRoute,
                        onNavigate = { screen ->
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(innerPadding),
                enterTransition = {
                    androidx.compose.animation.fadeIn(
                        animationSpec = androidx.compose.animation.core.tween(
                            com.programmingblitz.notoffice563.ui.theme.motion.MotionTokens.DurationNavigation,
                            easing = com.programmingblitz.notoffice563.ui.theme.motion.MotionTokens.EaseStandard
                        )
                    ) + androidx.compose.animation.slideInHorizontally(
                        initialOffsetX = { fullWidth -> (fullWidth * 0.06f).toInt() },
                        animationSpec = androidx.compose.animation.core.tween(
                            com.programmingblitz.notoffice563.ui.theme.motion.MotionTokens.DurationNavigation,
                            easing = com.programmingblitz.notoffice563.ui.theme.motion.MotionTokens.EaseDecel
                        )
                    )
                },
                exitTransition = {
                    androidx.compose.animation.fadeOut(
                        animationSpec = androidx.compose.animation.core.tween(
                            com.programmingblitz.notoffice563.ui.theme.motion.MotionTokens.DurationNavigation - 40,
                            easing = com.programmingblitz.notoffice563.ui.theme.motion.MotionTokens.EaseStandard
                        )
                    ) + androidx.compose.animation.slideOutHorizontally(
                        targetOffsetX = { fullWidth -> -(fullWidth * 0.04f).toInt() },
                        animationSpec = androidx.compose.animation.core.tween(
                            com.programmingblitz.notoffice563.ui.theme.motion.MotionTokens.DurationNavigation - 40,
                            easing = com.programmingblitz.notoffice563.ui.theme.motion.MotionTokens.EaseAccel
                        )
                    )
                },
                popEnterTransition = {
                    androidx.compose.animation.fadeIn(
                        animationSpec = androidx.compose.animation.core.tween(
                            com.programmingblitz.notoffice563.ui.theme.motion.MotionTokens.DurationNavigation,
                            easing = com.programmingblitz.notoffice563.ui.theme.motion.MotionTokens.EaseStandard
                        )
                    ) + androidx.compose.animation.slideInHorizontally(
                        initialOffsetX = { fullWidth -> -(fullWidth * 0.04f).toInt() },
                        animationSpec = androidx.compose.animation.core.tween(
                            com.programmingblitz.notoffice563.ui.theme.motion.MotionTokens.DurationNavigation,
                            easing = com.programmingblitz.notoffice563.ui.theme.motion.MotionTokens.EaseDecel
                        )
                    )
                },
                popExitTransition = {
                    androidx.compose.animation.fadeOut(
                        animationSpec = androidx.compose.animation.core.tween(
                            com.programmingblitz.notoffice563.ui.theme.motion.MotionTokens.DurationNavigation - 40,
                            easing = com.programmingblitz.notoffice563.ui.theme.motion.MotionTokens.EaseStandard
                        )
                    ) + androidx.compose.animation.slideOutHorizontally(
                        targetOffsetX = { fullWidth -> (fullWidth * 0.06f).toInt() },
                        animationSpec = androidx.compose.animation.core.tween(
                            com.programmingblitz.notoffice563.ui.theme.motion.MotionTokens.DurationNavigation - 40,
                            easing = com.programmingblitz.notoffice563.ui.theme.motion.MotionTokens.EaseAccel
                        )
                    )
                }
            ) {
                // 1. Home Dashboard
                composable(Screen.Home.route) {
                    val homeViewModel: HomeViewModel = viewModel(
                        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                @Suppress("UNCHECKED_CAST")
                                return HomeViewModel(repository) as T
                            }
                        }
                    )
                    HomeScreen(
                        viewModel = homeViewModel,
                        onNavigateToViewer = { uri ->
                            navController.navigate(Screen.Viewer.createRoute(uri))
                        },
                        onNavigateToCategory = { category ->
                            navController.navigate(Screen.Files.createRoute(category))
                        },
                        onNavigateToSearch = {
                            navController.navigate(Screen.Search.route)
                        },
                        onNavigateToRecent = {
                            navController.navigate(Screen.Recent.route)
                        },
                        onOpenDrawer = {
                            scope.launch { drawerState.open() }
                        },
                        onNavigateToProfile = {
                            navController.navigate(Screen.Profile.route)
                        }
                    )
                }

                // 2. Unified Files Browser
                composable(
                    route = Screen.Files.route,
                    arguments = listOf(navArgument("category") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    })
                ) { backStackEntry ->
                    val categoryArg = backStackEntry.arguments?.getString("category")
                    val initialCategory = categoryArg?.let { runCatching { DocumentCategory.valueOf(it) }.getOrNull() } ?: DocumentCategory.ALL

                    val filesViewModel: FilesViewModel = viewModel(
                        key = categoryArg ?: "ALL",
                        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                @Suppress("UNCHECKED_CAST")
                                return FilesViewModel(repository, preferences, initialCategory) as T
                            }
                        }
                    )
                    FilesScreen(
                        viewModel = filesViewModel,
                        onNavigateToViewer = { uri ->
                            navController.navigate(Screen.Viewer.createRoute(uri))
                        },
                        onOpenDrawer = {
                            scope.launch { drawerState.open() }
                        }
                    )
                }

                // 3. Recent Files
                composable(Screen.Recent.route) {
                    val recentViewModel: RecentViewModel = viewModel(
                        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                @Suppress("UNCHECKED_CAST")
                                return RecentViewModel(repository, preferences) as T
                            }
                        }
                    )
                    RecentScreen(
                        viewModel = recentViewModel,
                        onNavigateToViewer = { uri ->
                            navController.navigate(Screen.Viewer.createRoute(uri))
                        },
                        onOpenDrawer = {
                            scope.launch { drawerState.open() }
                        }
                    )
                }

                // 4. Settings
                composable(Screen.Settings.route) {
                    val settingsViewModel: SettingsViewModel = viewModel(
                        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                @Suppress("UNCHECKED_CAST")
                                return SettingsViewModel(preferences, repository) as T
                            }
                        }
                    )
                    SettingsScreen(
                        viewModel = settingsViewModel,
                        onOpenDrawer = {
                            scope.launch { drawerState.open() }
                        }
                    )
                }

                // 5. Profile & Settings Screen
                composable(Screen.Profile.route) {
                    val profileViewModel: ProfileViewModel = viewModel(
                        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                @Suppress("UNCHECKED_CAST")
                                return ProfileViewModel(authManager, preferences) as T
                            }
                        }
                    )
                    ProfileScreen(
                        viewModel = profileViewModel,
                        onOpenDrawer = {
                            scope.launch { drawerState.open() }
                        }
                    )
                }

                // 6. Universal Search
                composable(Screen.Search.route) {
                    val searchViewModel: SearchViewModel = viewModel(
                        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                @Suppress("UNCHECKED_CAST")
                                return SearchViewModel(repository) as T
                            }
                        }
                    )
                    SearchScreen(
                        viewModel = searchViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToViewer = { uri ->
                            navController.navigate(Screen.Viewer.createRoute(uri))
                        }
                    )
                }

                // 7. Document Viewer
                composable(
                    route = Screen.Viewer.route,
                    arguments = listOf(navArgument("encodedUri") { type = NavType.StringType })
                ) { backStackEntry ->
                    val encodedUri = backStackEntry.arguments?.getString("encodedUri") ?: ""
                    val decodedUri = Uri.decode(encodedUri)

                    val viewerViewModel: ViewerViewModel = viewModel(
                        key = decodedUri,
                        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                @Suppress("UNCHECKED_CAST")
                                return ViewerViewModel(decodedUri, repository, preferences) as T
                            }
                        }
                    )

                    ViewerScreen(
                        viewModel = viewerViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onOpenNewDocument = { newUri ->
                            navController.navigate(Screen.Viewer.createRoute(newUri)) {
                                popUpTo(Screen.Home.route)
                            }
                        }
                    )
                }
            }
        }
    }
}
