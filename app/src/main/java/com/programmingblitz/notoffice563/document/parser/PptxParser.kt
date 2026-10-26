package com.programmingblitz.notoffice563.document.parser

import android.util.Xml
import com.programmingblitz.notoffice563.domain.model.ParsedPresentation
import com.programmingblitz.notoffice563.domain.model.PresentationSlide
import org.xmlpull.v1.XmlPullParser
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.zip.ZipInputStream

object PptxParser {

    fun parse(inputStream: InputStream): ParsedPresentation {
        val slideEntries = mutableMapOf<String, ByteArray>()
        val notesEntries = mutableMapOf<String, ByteArray>()

        ZipInputStream(inputStream).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                val name = entry.name
                if (name.startsWith("ppt/slides/slide") && name.endsWith(".xml")) {
                    slideEntries[name] = zip.readBytes()
                } else if (name.startsWith("ppt/notesSlides/notesSlide") && name.endsWith(".xml")) {
                    notesEntries[name] = zip.readBytes()
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }

        // Sort slide entries by number
        val sortedKeys = slideEntries.keys.sortedBy { key ->
            key.filter { it.isDigit() }.toIntOrNull() ?: 0
        }

        val slides = mutableListOf<PresentationSlide>()

        for ((index, key) in sortedKeys.withIndex()) {
            val bytes = slideEntries[key] ?: continue
            val slideNumber = index + 1

            var title = ""
            val bullets = mutableListOf<String>()

            runCatching {
                val parser = Xml.newPullParser()
                parser.setInput(ByteArrayInputStream(bytes), "UTF-8")
                var eventType = parser.eventType

                var inParagraph = false
                var currentPText = StringBuilder()

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    when (eventType) {
                        XmlPullParser.START_TAG -> {
                            if (parser.name == "p") {
                                inParagraph = true
                                currentPText = StringBuilder()
                            } else if (parser.name == "t" && inParagraph) {
                                currentPText.append(parser.nextText())
                            }
                        }
                        XmlPullParser.END_TAG -> {
                            if (parser.name == "p") {
                                val text = currentPText.toString().trim()
                                if (text.isNotEmpty()) {
                                    if (title.isEmpty()) {
                                        title = text
                                    } else {
                                        bullets.add(text)
                                    }
                                }
                                inParagraph = false
                            }
                        }
                    }
                    eventType = parser.next()
                }
            }

            if (title.isEmpty() && bullets.isNotEmpty()) {
                title = bullets.removeAt(0)
            } else if (title.isEmpty()) {
                title = "Slide $slideNumber"
            }

            // Check if slide note exists
            val noteKey = "ppt/notesSlides/notesSlide$slideNumber.xml"
            var noteText = ""
            val noteBytes = notesEntries[noteKey]
            if (noteBytes != null) {
                runCatching {
                    val parser = Xml.newPullParser()
                    parser.setInput(ByteArrayInputStream(noteBytes), "UTF-8")
                    var eventType = parser.eventType
                    val noteBuilder = StringBuilder()
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType == XmlPullParser.START_TAG && parser.name == "t") {
                            noteBuilder.append(parser.nextText()).append(" ")
                        }
                        eventType = parser.next()
                    }
                    noteText = noteBuilder.toString().trim()
                }
            }

            slides.add(
                PresentationSlide(
                    slideNumber = slideNumber,
                    title = title,
                    bodyBullets = bullets,
                    notes = noteText
                )
            )
        }

        return ParsedPresentation(
            slides = if (slides.isNotEmpty()) slides else listOf(
                PresentationSlide(1, "Empty Presentation", listOf("No slide content detected."))
            )
        )
    }
}
