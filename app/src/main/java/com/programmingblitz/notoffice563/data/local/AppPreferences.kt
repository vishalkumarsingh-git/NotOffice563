package com.programmingblitz.notoffice563.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "notoffice_preferences")

enum class AppThemeMode {
    SYSTEM, LIGHT, DARK
}

enum class FileSortOrder(val title: String) {
    DATE_MODIFIED("Date Modified"),
    NAME("Name"),
    SIZE("Size"),
    TYPE("File Type")
}

enum class FileViewMode {
    LIST, GRID
}

data class UserPreferences(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val sortOrder: FileSortOrder = FileSortOrder.DATE_MODIFIED,
    val viewMode: FileViewMode = FileViewMode.LIST,
    val maxRecentFiles: Int = 25,
    val rememberLastPosition: Boolean = true,
    val confirmDelete: Boolean = true
)

class AppPreferences(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val SORT_ORDER = stringPreferencesKey("sort_order")
        val VIEW_MODE = stringPreferencesKey("view_mode")
        val MAX_RECENT = intPreferencesKey("max_recent")
        val REMEMBER_POSITION = booleanPreferencesKey("remember_position")
        val CONFIRM_DELETE = booleanPreferencesKey("confirm_delete")
        val USER_ID = stringPreferencesKey("user_id")
        val USER_DISPLAY_NAME = stringPreferencesKey("user_display_name")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_PHOTO_URL = stringPreferencesKey("user_photo_url")
        val IS_AUTHENTICATED = booleanPreferencesKey("is_authenticated")
    }

    val userAccount: Flow<com.programmingblitz.notoffice563.domain.model.UserAccount?> = context.dataStore.data.map { prefs ->
        val isAuth = prefs[Keys.IS_AUTHENTICATED] ?: false
        val id = prefs[Keys.USER_ID]
        if (isAuth && !id.isNullOrBlank()) {
            com.programmingblitz.notoffice563.domain.model.UserAccount(
                id = id,
                displayName = prefs[Keys.USER_DISPLAY_NAME],
                email = prefs[Keys.USER_EMAIL],
                photoUrl = prefs[Keys.USER_PHOTO_URL]
            )
        } else {
            null
        }
    }

    suspend fun saveUserAccount(user: com.programmingblitz.notoffice563.domain.model.UserAccount) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_AUTHENTICATED] = true
            prefs[Keys.USER_ID] = user.id
            if (user.displayName != null) {
                prefs[Keys.USER_DISPLAY_NAME] = user.displayName
            } else {
                prefs.remove(Keys.USER_DISPLAY_NAME)
            }
            if (user.email != null) {
                prefs[Keys.USER_EMAIL] = user.email
            } else {
                prefs.remove(Keys.USER_EMAIL)
            }
            if (user.photoUrl != null) {
                prefs[Keys.USER_PHOTO_URL] = user.photoUrl
            } else {
                prefs.remove(Keys.USER_PHOTO_URL)
            }
        }
    }

    suspend fun clearUserAccount() {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_AUTHENTICATED] = false
            prefs.remove(Keys.USER_ID)
            prefs.remove(Keys.USER_DISPLAY_NAME)
            prefs.remove(Keys.USER_EMAIL)
            prefs.remove(Keys.USER_PHOTO_URL)
        }
    }

    val userPreferences: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val themeStr = prefs[Keys.THEME_MODE] ?: AppThemeMode.SYSTEM.name
        val sortStr = prefs[Keys.SORT_ORDER] ?: FileSortOrder.DATE_MODIFIED.name
        val viewStr = prefs[Keys.VIEW_MODE] ?: FileViewMode.LIST.name
        val maxRecent = prefs[Keys.MAX_RECENT] ?: 25
        val rememberPos = prefs[Keys.REMEMBER_POSITION] ?: true
        val confirmDel = prefs[Keys.CONFIRM_DELETE] ?: true

        UserPreferences(
            themeMode = runCatching { AppThemeMode.valueOf(themeStr) }.getOrDefault(AppThemeMode.SYSTEM),
            sortOrder = runCatching { FileSortOrder.valueOf(sortStr) }.getOrDefault(FileSortOrder.DATE_MODIFIED),
            viewMode = runCatching { FileViewMode.valueOf(viewStr) }.getOrDefault(FileViewMode.LIST),
            maxRecentFiles = maxRecent,
            rememberLastPosition = rememberPos,
            confirmDelete = confirmDel
        )
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.name
        }
    }

    suspend fun setSortOrder(order: FileSortOrder) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SORT_ORDER] = order.name
        }
    }

    suspend fun setViewMode(mode: FileViewMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.VIEW_MODE] = mode.name
        }
    }

    suspend fun setMaxRecentFiles(max: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.MAX_RECENT] = max
        }
    }

    suspend fun setRememberPosition(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.REMEMBER_POSITION] = enabled
        }
    }

    suspend fun setConfirmDelete(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.CONFIRM_DELETE] = enabled
        }
    }
}
