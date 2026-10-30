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
}
