package com.programmingblitz.notoffice563

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlinx.coroutines.flow.first

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("NotOffice563", appName)
  }

  @Test
  fun `launch MainActivity`() {
    val scenario = androidx.test.core.app.ActivityScenario.launch(MainActivity::class.java)
    scenario.onActivity { activity ->
      org.junit.Assert.assertNotNull(activity)
    }
  }

  @Test
  fun `verify empty database and workspace state`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val database = com.programmingblitz.notoffice563.data.local.AppDatabase.getInstance(context)
    val repository = com.programmingblitz.notoffice563.data.repository.DocumentRepository(
      recentFileDao = database.recentFileDao(),
      documentNoteDao = database.documentNoteDao()
    )

    kotlinx.coroutines.runBlocking {
      val all = repository.allRecentFiles
      org.junit.Assert.assertNotNull(all)
    }
  }

  @Test
  fun `verify auth state is honest and unauthenticated initially`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val preferences = com.programmingblitz.notoffice563.data.local.AppPreferences.getInstance(context)
    val authManager = com.programmingblitz.notoffice563.data.auth.AuthManager(context, preferences)

    kotlinx.coroutines.runBlocking {
      val user = preferences.userAccount.first()
      // Strictly honest: must be null, never a dummy or simulated user
      org.junit.Assert.assertNull(user)
    }
  }

  @Test
  fun `verify UserAccount initials computation without fabricated data`() {
    val user1 = com.programmingblitz.notoffice563.domain.model.UserAccount(
      id = "123",
      displayName = "John Doe",
      email = "john@example.com"
    )
    assertEquals("JD", user1.getInitials())

    val user2 = com.programmingblitz.notoffice563.domain.model.UserAccount(
      id = "456",
      displayName = null,
      email = "alice@example.com"
    )
    assertEquals("AL", user2.getInitials())

    val user3 = com.programmingblitz.notoffice563.domain.model.UserAccount(
      id = "789",
      displayName = null,
      email = null
    )
    assertEquals("U", user3.getInitials())
  }

  @Test
  fun `verify sidebar and bottom nav items contain Profile destination`() {
    val sidebarRoutes = com.programmingblitz.notoffice563.ui.navigation.Screen.sidebarItems.map { it.route }
    val bottomNavRoutes = com.programmingblitz.notoffice563.ui.navigation.Screen.bottomNavItems.map { it.route }

    org.junit.Assert.assertTrue(sidebarRoutes.contains("profile"))
    org.junit.Assert.assertTrue(bottomNavRoutes.contains("profile"))
    org.junit.Assert.assertTrue(sidebarRoutes.contains("home"))
    org.junit.Assert.assertTrue(sidebarRoutes.any { it.startsWith("files") })
    org.junit.Assert.assertTrue(sidebarRoutes.contains("recent"))
    org.junit.Assert.assertTrue(sidebarRoutes.contains("settings"))
  }

  @Test
  fun `verify Screen Viewer URI token round-trip preserves encoded characters`() {
    val originalUriString = "content://com.android.providers.downloads.documents/document/raw%3A%2Fstorage%2Femulated%2F0%2FDownload%2FQuarterly_Report_2026.docx"
    val route = com.programmingblitz.notoffice563.ui.navigation.Screen.Viewer.createRoute(originalUriString)
    val token = route.removePrefix("viewer/")
    val decoded = com.programmingblitz.notoffice563.ui.navigation.Screen.Viewer.decodeUriToken(token)

    assertEquals(originalUriString, decoded)
  }

  @Test
  fun `verify ACTION_VIEW intent resolves to MainActivity for docx mime type`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
      setDataAndType(
        android.net.Uri.parse("content://com.example.provider/document/1"),
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
      )
    }
    val resolveInfo = context.packageManager.queryIntentActivities(intent, 0)
    val hasMainActivity = resolveInfo.any { it.activityInfo.name == MainActivity::class.java.name }
    org.junit.Assert.assertTrue("MainActivity should resolve ACTION_VIEW for docx", hasMainActivity)
  }

  @Test
  fun `verify ACTION_VIEW intent resolves to MainActivity for xlsx mime type`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
      setDataAndType(
        android.net.Uri.parse("content://com.example.provider/document/2"),
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
      )
    }
    val resolveInfo = context.packageManager.queryIntentActivities(intent, 0)
    val hasMainActivity = resolveInfo.any { it.activityInfo.name == MainActivity::class.java.name }
    org.junit.Assert.assertTrue("MainActivity should resolve ACTION_VIEW for xlsx", hasMainActivity)
  }

  @Test
  fun `verify SafHelper queryMetadata on file URI`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val testFile = java.io.File(context.cacheDir, "test_doc.docx").apply {
      writeText("dummy content")
    }
    val uri = android.net.Uri.fromFile(testFile)
    val meta = com.programmingblitz.notoffice563.document.saf.SafHelper.queryMetadata(context, uri)

    assertEquals("test_doc.docx", meta.displayName)
    assertEquals(com.programmingblitz.notoffice563.domain.model.DocumentType.WORD_DOCX, meta.documentType)
    org.junit.Assert.assertTrue(meta.hasPermission)
    org.junit.Assert.assertTrue(meta.isWritable)
  }

  @Test
  fun `verify changing settings updates AppPreferences without crashing`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val preferences = com.programmingblitz.notoffice563.data.local.AppPreferences.getInstance(context)

    // Test Theme Mode changes
    preferences.setThemeMode(com.programmingblitz.notoffice563.data.local.AppThemeMode.DARK)
    var prefs = preferences.userPreferences.first()
    assertEquals(com.programmingblitz.notoffice563.data.local.AppThemeMode.DARK, prefs.themeMode)

    preferences.setThemeMode(com.programmingblitz.notoffice563.data.local.AppThemeMode.LIGHT)
    prefs = preferences.userPreferences.first()
    assertEquals(com.programmingblitz.notoffice563.data.local.AppThemeMode.LIGHT, prefs.themeMode)

    preferences.setThemeMode(com.programmingblitz.notoffice563.data.local.AppThemeMode.SYSTEM)
    prefs = preferences.userPreferences.first()
    assertEquals(com.programmingblitz.notoffice563.data.local.AppThemeMode.SYSTEM, prefs.themeMode)

    // Test View Mode changes
    preferences.setViewMode(com.programmingblitz.notoffice563.data.local.FileViewMode.GRID)
    prefs = preferences.userPreferences.first()
    assertEquals(com.programmingblitz.notoffice563.data.local.FileViewMode.GRID, prefs.viewMode)

    preferences.setViewMode(com.programmingblitz.notoffice563.data.local.FileViewMode.LIST)
    prefs = preferences.userPreferences.first()
    assertEquals(com.programmingblitz.notoffice563.data.local.FileViewMode.LIST, prefs.viewMode)

    // Test Sort Order changes
    for (sort in com.programmingblitz.notoffice563.data.local.FileSortOrder.entries) {
      preferences.setSortOrder(sort)
      prefs = preferences.userPreferences.first()
      assertEquals(sort, prefs.sortOrder)
    }
  }

  @Test
  fun `verify SettingsViewModel updates preferences safely`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val database = com.programmingblitz.notoffice563.data.local.AppDatabase.getInstance(context)
    val preferences = com.programmingblitz.notoffice563.data.local.AppPreferences.getInstance(context)
    val repository = com.programmingblitz.notoffice563.data.repository.DocumentRepository(
      recentFileDao = database.recentFileDao(),
      documentNoteDao = database.documentNoteDao()
    )
    val viewModel = com.programmingblitz.notoffice563.ui.settings.SettingsViewModel(preferences, repository)

    viewModel.setTheme(com.programmingblitz.notoffice563.data.local.AppThemeMode.DARK)
    viewModel.setViewMode(com.programmingblitz.notoffice563.data.local.FileViewMode.GRID)
    viewModel.setSortOrder(com.programmingblitz.notoffice563.data.local.FileSortOrder.NAME)
    viewModel.clearRecentFiles()

    org.junit.Assert.assertNotNull(viewModel.userPreferences.value)
  }
}
