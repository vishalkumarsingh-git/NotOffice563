package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

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
  fun `verify sample document creation and database operations`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val database = com.example.data.local.AppDatabase.getInstance(context)
    val repository = com.example.data.repository.DocumentRepository(
      recentFileDao = database.recentFileDao(),
      documentNoteDao = database.documentNoteDao()
    )

    val pdfUri = com.example.util.SampleDocumentCreator.createSamplePdf(context)
    val docxUri = com.example.util.SampleDocumentCreator.createSampleDocx(context)
    val xlsxUri = com.example.util.SampleDocumentCreator.createSampleXlsx(context)
    val pptxUri = com.example.util.SampleDocumentCreator.createSamplePptx(context)

    org.junit.Assert.assertNotNull(pdfUri)
    org.junit.Assert.assertNotNull(docxUri)
    org.junit.Assert.assertNotNull(xlsxUri)
    org.junit.Assert.assertNotNull(pptxUri)

    kotlinx.coroutines.runBlocking {
      val entity = repository.registerOpenedDocument(context, pdfUri)
      org.junit.Assert.assertEquals("PDF", entity.fileType)
      val all = repository.allRecentFiles
      org.junit.Assert.assertNotNull(all)
    }
  }
}
