# NotOffice563

> **A modern office suite for documents, spreadsheets, presentations, and PDFs — built to make productivity simple.**
>
> *Created by V. K. Singh*  
> *© 2026 Programming Blitz.*

---

## 1. Why NotOffice563?

Mobile productivity users frequently encounter documents in various office formats—Word documents, Excel spreadsheets, PowerPoint presentations, and Adobe PDFs. Typically, accessing and organizing these files requires installing multiple proprietary suites, navigating cumbersome cloud login paywalls, or switching between disconnected apps with inconsistent interfaces.

**NotOffice563** solves this by delivering an independent, local-first Android workspace where users can create, open, view, edit, organize, search, and manage all common office documents and PDFs from a single, unified interface wrapped in an authentic **iOS Liquid Glass** design language.

---

## 2. Features

- **Liquid Glass Design System**:
  - Continuous 32dp capsule floating dock with realistic spring physics and sliding frosted pill indicator.
  - Specular rim highlights, multi-layered translucency, and frosted background refraction.
  - Dedicated Dark Glass and Light Glass palettes with vibrant azure accents and soft glow shadows.
  - Tactile haptic feedback and interactive spring bounce on buttons, cards, and toggles.

- **Android System "Open With" Integration (`ACTION_VIEW`)**:
  - Registered intent filters for all supported document MIME types (DOCX, DOC, XLSX, XLS, PPTX, PPT, PDF).
  - Directly selectable from Android Files, Gmail, Google Drive, WhatsApp, and third-party file managers.
  - Safely receives temporary incoming URI permissions and persists long-term grants via `takePersistableUriPermission`.
  - Routes directly to the dedicated document editor/viewer based on actual content type.

- **Document Creation Engine**:
  - In-app document starter templates for:
    - **Word (.docx)**: Blank Document, Project Proposal, Meeting Minutes, Weekly Status Report.
    - **Excel (.xlsx)**: Blank Spreadsheet, Monthly Budget Planner, Task Tracker, Inventory Log.
    - **PowerPoint (.pptx)**: Blank Presentation, Company Overview, Project Pitch Deck, Quarterly Review.
    - **PDF (.pdf)**: Blank Document, Research Notes, Invoice Template.
  - Custom file naming and immediate launch into the editor.

- **Document Viewers & Editors**:
  - **PDF Viewer**: High-fidelity bitmap rendering using Android `PdfRenderer` with multi-level zoom, page jumps, thumbnail navigation, and local text notes per `(file + page)`.
  - **Word Editor & Viewer (.docx)**: OpenXML parser reading headings, paragraphs, formatted text, and bullet points. Direct inline editing with compilation and saving back to DOCX.
  - **Spreadsheet Viewer (.xlsx)**: Multi-sheet OpenXML parser displaying 2D scrollable grids with column letters (A-Z), row numbers (1-N), sheet selector tabs, and cell inspector.
  - **Presentation Viewer (.pptx)**: Visual slide deck viewer presenting slide cards with slide titles, body bullet points, slide navigation, and speaker notes.
  - **Legacy Format Fallback (.doc, .xls, .ppt)**: Clean format detection with graceful fallback screen, metadata inspector, and seamless "Open with another app" intent handoff.

- **Unified Dashboard & File Manager**:
  - Live device storage breakdown (used vs. free GB via `StatFs`).
  - Format category chips with dynamic document counts.
  - Starred/favorites quick tray.
  - Dual layout modes: 2-column visual grid cards or compact list rows.
  - Default file sorting: Date Modified, Name, Size, or File Type.
  - Instant universal search across file names, extensions, and formats.

- **Privacy & Security Architecture**:
  - **100% On-Device Processing**: Document parsing, editing, and rendering run locally without cloud telemetry or data harvesting.
  - **Storage Access Framework (SAF)**: Uses Android's zero-permission document picker—no dangerous `MANAGE_EXTERNAL_STORAGE` permission required.
  - **Optional Profile**: Google Sign-In via Jetpack Credential Manager for user account identity while keeping all documents strictly isolated on device.

---

## 3. Supported Formats

| Format | Extension | Support Level | Implementation Details |
| :--- | :--- | :--- | :--- |
| **PDF** | `.pdf` | **Full View & Annotate** | High-resolution bitmap rendering via Android `PdfRenderer`, zoom, jump-to-page, local notes. |
| **Word** | `.docx` | **Full Read & Edit** | Pure OpenXML ZIP & XML parser. Formatted text reading, inline editing, and DOCX compilation. |
| **Excel** | `.xlsx` | **Full Read & Inspect** | OpenXML ZIP & XML parser. 2D scrollable grid, multi-sheet tabs, formula/value cell inspector. |
| **PowerPoint** | `.pptx` | **Content Extraction** | OpenXML ZIP & XML parser. Slide deck viewer with titles, bullets, and speaker notes. |
| **Legacy Word** | `.doc` | **Format Fallback** | Format detection with graceful fallback screen and "Open with another app" handoff. |
| **Legacy Excel** | `.xls` | **Format Fallback** | Format detection with graceful fallback screen and "Open with another app" handoff. |
| **Legacy PPT** | `.ppt` | **Format Fallback** | Format detection with graceful fallback screen and "Open with another app" handoff. |

---

## 4. Architecture

NotOffice563 is structured using modern Android Clean Architecture with MVVM:

```text
Compose UI (Liquid Glass Design System)
    ↓
ViewModel (StateFlow & Coroutines)
    ↓
Repository Layer (DocumentRepository & AppPreferences)
    ↓
Data Layer: Room DB  /  DataStore  /  SAF ContentResolver  /  OpenXML & PDF Engines
```

### Document Ingestion Flow

```text
Android File Manager ("Open With") OR In-App SAF Picker
    ↓
Content URI & Intent Flag Inspection
    ↓
SafHelper (ContentResolver Stream & Persistable URI Grant)
    ↓
MIME & Magic Bytes Format Detection
    ↓
Dedicated Parser (Docx, Xlsx, Pptx, Pdf)
    ↓
Room DB Metadata Cache (RecentFileEntity, DocumentNoteEntity)
```

---

## 5. Download APK

Download the latest successful debug APK from the [NotOffice563 latest GitHub Release](https://github.com/vishalkumarsingh-git/NotOffice563/releases/download/latest/NotOffice563.apk). The link is updated whenever the APK build workflow succeeds.

You can also browse [all workflow runs and build artifacts](https://github.com/vishalkumarsingh-git/NotOffice563/actions).

---

## 6. Running the Project

1. Open Android Studio (Hedgehog, Ladybug, or later).
2. Choose **File > Open** and select the project directory.
3. Wait for Gradle sync to complete.
4. Select an Android Virtual Device (AVD) or physical device running **Android 10 (API 29)** or higher.
5. Click the green **Run 'app'** button (`Shift + F10`) or run `gradle assembleDebug`.

---

## 7. Limitations

- **Complex Office Features**: NotOffice563 emphasizes clean readability, privacy, speed, and local safety over reproducing desktop Microsoft Office rendering engines (e.g. macro execution, complex VBA scripts, or 3D animations).
- **Legacy Binary Formats**: Binary compound document formats (`.doc`, `.xls`, `.ppt`) use graceful fallbacks to external viewing applications.
- **PDF Notes**: Annotations and notes are mapped per `(file URI + page)` in the local Room database to safeguard the original document file against binary corruption.

---

## 8. Disclaimer

**NotOffice563** is an independent application created by V. K. Singh and is **not affiliated with, sponsored by, or endorsed by Microsoft Corporation**. Microsoft, Word, Excel, PowerPoint, and Microsoft 365 are registered trademarks of Microsoft Corporation.

© 2026 Programming Blitz.
