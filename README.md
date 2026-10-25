# NotOffice563

> **A unified local document workspace for Android.**
> *One place for your documents.*

---

## 1. Why NotOffice563?

Mobile productivity users frequently encounter documents in various office formats—Word documents, Excel spreadsheets, PowerPoint presentations, and Adobe PDFs. Typically, accessing and organizing these files requires installing multiple proprietary suites, navigating cumbersome cloud login paywalls, or switching between disconnected apps with inconsistent interfaces.

**NotOffice563** solves this by delivering an independent, local-first Android workspace where users can discover, open, view, organize, search, and manage all common office documents and PDFs from a single, unified interface.

---

## 2. Features

- **Unified Dashboard**:
  - Global document search entry point
  - Real-time device storage capacity overview (used/available GB via `StatFs`)
  - Quick access shortcuts categorized by format with live document counters
  - Recently opened documents list
  - Starred / favorites quick-access tray
  - "+ Open File" Storage Access Framework picker action

- **Storage Access Framework (SAF) Integration**:
  - Zero dangerous permissions: respects Android 10+ privacy architecture
  - Long-term persistable URI permissions via `takePersistableUriPermission`
  - Graceful metadata extraction (`OpenableColumns`, MIME detection, byte size)
  - Native SAF document renaming and deletion

- **Document Viewers**:
  - **PDF Viewer**: Native rendering using Android `PdfRenderer` with page bitmap caching, page indicators, jump-to-page dialog, zoom controls, and local text notes per `(file + page)`.
  - **Word Viewer (.docx)**: Clean OpenXML parser extracting document title, creator metadata, word counts, paragraphs, headings, bold/italic text, and bullet points.
  - **Basic Word Editing**: Edit plain text and document title, then compile and export as a new `.docx` archive.
  - **Spreadsheet Viewer (.xlsx)**: Multi-sheet OpenXML parser extracting sheets, column letters (A-Z), row numbers (1-N), shared strings, and cell formula/value inspector.
  - **Presentation Viewer (.pptx)**: Slide deck extractor presenting slide cards with slide numbers, titles, bullet points, and speaker notes.
  - **Legacy Compatibility Fallback (.doc, .xls, .ppt)**: Honest, clean fallback screen offering "Open with another app", standard Android share, and full file metadata inspection.

- **Universal Search**:
  - Real-time filtering by document title, extension, or format type.

- **Recent Files & Favorites**:
  - Room database metadata persistence (never storing file payloads).
  - Clear individual recent items or wipe recent history with confirmation.
  - Star/unstar documents across screens.

- **Customizable Settings**:
  - Appearance: Light, Dark, or System Default theme
  - File browser: Default sort order (Date, Name, Size, Type) and view mode (List, Grid)
  - Recent files capacity limit (10, 25, 50, 100)
  - Reading progress continuation toggle
  - Deletion confirmation safety toggle

---

## 3. Supported Formats

| Format | Extension | Support Level | Implementation Notes |
| :--- | :--- | :--- | :--- |
| **PDF** | `.pdf` | **Full** | High-resolution bitmap rendering via Android `PdfRenderer`, zoom, jump-to-page, local notes. |
| **Word** | `.docx` | **Full Reading & Basic Edit** | Pure OpenXML ZIP & XML parser. Formatted text reading + exportable basic text editing. |
| **Excel** | `.xlsx` | **Full Reading** | OpenXML ZIP & XML parser. 2D scrollable grid, sheet tabs, cell inspector. |
| **PowerPoint** | `.pptx` | **Content Extraction** | OpenXML ZIP & XML parser. Slide deck viewer with titles, bullets, and speaker notes. |
| **Legacy Word** | `.doc` | **Fallback** | Format detection with graceful fallback screen and "Open with another app". |
| **Legacy Excel** | `.xls` | **Fallback** | Format detection with graceful fallback screen and "Open with another app". |
| **Legacy PPT** | `.ppt` | **Fallback** | Format detection with graceful fallback screen and "Open with another app". |

---

## 4. Architecture

NotOffice563 is structured using modern Android Clean Architecture with MVVM:

```text
Compose UI (Material 3)
    ↓
ViewModel (StateFlow & Coroutines)
    ↓
Repository (DocumentRepository & AppPreferences)
    ↓
Room DB  /  DataStore  /  SAF ContentResolver  /  Document Parsers
```

### Data Flow

```text
Android SAF Document Picker
    ↓
Content URI & Persistable Permissions
    ↓
MIME & Magic Bytes Format Detection
    ↓
Document Parser (PdfRenderer / OpenXML)
    ↓
Room Metadata Persistence (RecentFileEntity, DocumentNoteEntity)
```

---

## 5. Running the Project

1. Open Android Studio (Hedgehog, Iguana, or later).
2. Choose **File > Open** and select the project directory.
3. Wait for Gradle sync to complete.
4. Select an Android Virtual Device (AVD) or physical device running **Android 10 (API 29)** or higher.
5. Click the green **Run 'app'** button (`Shift + F10`).

---

## 6. Limitations

- **Office Rendering**: NotOffice563 emphasizes clean readability, privacy, and fast local performance rather than reproducing full desktop Microsoft Office rendering (e.g. macro execution, complex charts, or animations).
- **Legacy Office Formats**: Binary compound document formats (`.doc`, `.xls`, `.ppt`) use graceful fallbacks to external viewing applications.
- **PDF Annotations**: PDF text notes are associated with `(file URI + page)` in the local Room database to safeguard the original document against file corruption.
- **Formulas**: Spreadsheet formulas show calculated cell cache values without complex recalculation engines.

---

## 7. Roadmap

- [ ] Google Drive, OneDrive, and Nextcloud SAF document provider integrations
- [ ] Export Word/Excel documents directly to PDF
- [ ] Document text search within open DOCX and PDF viewers
- [ ] Text-to-speech reading assistance
- [ ] Document printing via Android `PrintManager`

---

## 8. Disclaimer

**NotOffice563** is an independent application and is **not affiliated with, sponsored by, or endorsed by Microsoft Corporation**. Microsoft, Word, Excel, PowerPoint, and Microsoft 365 are registered trademarks of Microsoft Corporation.
