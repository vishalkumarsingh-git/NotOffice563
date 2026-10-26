package com.programmingblitz.notoffice563.ui.navigation

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.programmingblitz.notoffice563.domain.model.DocumentCategory

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector? = null,
    val unselectedIcon: ImageVector? = null
) {
    val icon: ImageVector
        get() = selectedIcon ?: Icons.Filled.Home

    object Home : Screen(
        route = "home",
        title = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )

    object Files : Screen(
        route = "files?category={category}",
        title = "Files",
        selectedIcon = Icons.Filled.Folder,
        unselectedIcon = Icons.Outlined.Folder
    ) {
        fun createRoute(category: DocumentCategory? = null): String {
            return if (category != null && category != DocumentCategory.ALL) {
                "files?category=${category.name}"
            } else {
                "files"
            }
        }
    }

    object Recent : Screen(
        route = "recent",
        title = "Recent",
        selectedIcon = Icons.Filled.History,
        unselectedIcon = Icons.Outlined.History
    )

    object Settings : Screen(
        route = "settings",
        title = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )

    object Profile : Screen(
        route = "profile",
        title = "Profile",
        selectedIcon = Icons.Filled.AccountCircle,
        unselectedIcon = Icons.Outlined.AccountCircle
    )

    object Search : Screen(
        route = "search",
        title = "Search"
    )

    object Viewer : Screen(
        route = "viewer/{encodedUri}",
        title = "Viewer"
    ) {
        fun createRoute(uri: String): String {
            val encoded = Uri.encode(uri)
            return "viewer/$encoded"
        }
    }

    companion object {
        val bottomNavItems: List<Screen>
            get() = listOf(Home, Files, Recent, Settings, Profile)

        val sidebarItems: List<Screen>
            get() = listOf(Home, Files, Recent, Settings, Profile)
    }
}
