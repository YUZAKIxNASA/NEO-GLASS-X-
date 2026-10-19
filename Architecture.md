# NEONWAVE — Technical Architecture Document

## 1. Executive Technical Summary
NEONWAVE is built as a single-activity native Android application utilizing Kotlin and Jetpack Compose for declarative UI rendering. The architecture adheres to MVVM (Model-View-ViewModel) and Clean Architecture principles, ensuring strict decoupling between the presentation layer, domain logic, media player engine, and persistence layer.

---

## 2. Verified Technical Stack

### Core Toolchain & Platform
- **Target OS**: Android (API Level 24 minimum, API Level 36 compile & target SDK)
- **Programming Language**: Kotlin 2.2.10
- **Android Gradle Plugin (AGP)**: 9.1.1
- **UI Framework**: Jetpack Compose with Material 3 (BOM 2024.09.00)
- **Architecture**: MVVM with AndroidX ViewModel & StateFlow / Coroutines
- **JSON Serialization**: Moshi 1.15.2 / Retrofit 2.12.0
- **Local Persistence Engine**: Room 2.7.0 (KSP-ready)
- **Networking & HTTP**: OkHttp 4.10.0 / Retrofit 2.12.0
- **Testing Frameworks**: JUnit 4.13.2, Robolectric 4.16.1, Compose UI Test JUnit4

### Verified Dependency Inventory (`gradle/libs.versions.toml`)
- `androidx-core-ktx` (1.18.0)
- `androidx-activity-compose` (1.10.1)
- `androidx-lifecycle-runtime-compose` (2.8.7)
- `androidx-lifecycle-viewmodel-compose` (2.8.7)
- `androidx-compose-material3`
- `androidx-compose-material-icons-core` & `extended`
- `androidx-room-runtime` & `androidx-room-ktx` (2.7.0)
- `retrofit` & `converter-moshi` (2.12.0)
- `okhttp` & `logging-interceptor` (4.10.0)
- `kotlinx-coroutines-android` & `core` (1.10.2)
- `coil-compose` (2.7.0 available in catalog)
- `androidx-navigation-compose` (2.8.9 available in catalog)

---

## 3. Verified Directory Structure

```text
/
├── .env.example                               # Environment secrets configuration
├── .gitignore
├── build.gradle.kts                           # Root Gradle project configuration
├── gradle.properties                          # JVM & Gradle memory settings
├── metadata.json                              # AI Studio application metadata
├── settings.gradle.kts                        # Root project naming & plugin repositories
├── gradle/
│   ├── libs.versions.toml                     # Centralized version catalog
│   └── wrapper/gradle-wrapper.properties
├── app/
│   ├── build.gradle.kts                       # App-level plugins, dependencies, signing
│   ├── proguard-rules.pro                     # R8/ProGuard configuration
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml            # Application manifest & activity declaration
│       │   ├── java/com/example/
│       │   │   ├── MainActivity.kt            # Application entry point Activity
│       │   │   └── ui/theme/
│       │   │       ├── Color.kt               # Palette color tokens
│       │   │       ├── Theme.kt               # MaterialTheme wrapper & dynamic color
│       │   │       └── Type.kt                # Typography definitions
│       │   └── res/
│       │       ├── values/
│       │       │   ├── colors.xml             # XML color definitions
│       │       │   ├── strings.xml            # App name and string resources
│       │       │   └── themes.xml             # Base XML window themes
│       │       └── mipmap-*/                  # Application launcher icon mipmaps
│       └── test/java/com/example/
│           ├── ExampleUnitTest.kt             # Local JVM unit test
│           └── ExampleRobolectricTest.kt      # Context-aware Robolectric unit test
```

---

## 4. Architectural Layers & Target Component Design

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer (Compose)              │
│  ┌───────────────┐ ┌────────────────┐ ┌───────────────────┐ │
│  │  HomeScreen   │ │ DownloaderView │ │    LibraryView    │ │
│  └───────┬───────┘ └───────┬────────┘ └─────────┬─────────┘ │
│          │                 │                    │           │
│          └────────┬────────┴────────────────────┘           │
│                   ▼                                         │
│       ┌───────────────────────┐   ┌───────────────────────┐ │
│       │  FullPlayerBottomSheet│   │   PersistentMiniPlayer│ │
│       └───────────┬───────────┘   └───────────┬───────────┘ │
└───────────────────┼───────────────────────────┼─────────────┘
                    │                           │
                    ▼                           ▼
┌─────────────────────────────────────────────────────────────┐
│                     ViewModel Layer                         │
│  ┌──────────────────────┐        ┌───────────────────────┐  │
│  │   PlayerViewModel    │        │  DownloaderViewModel  │  │
│  │ (PlaybackState,Queue)│        │  (Extract, Progress)  │  │
│  └──────────┬───────────┘        └───────────┬───────────┘  │
│             │                                │              │
│             ▼                                ▼              │
│  ┌──────────────────────┐        ┌───────────────────────┐  │
│  │   LibraryViewModel   │        │     HomeViewModel     │  │
│  └──────────┬───────────┘        └───────────┬───────────┘  │
└─────────────┼────────────────────────────────┼──────────────┘
              │                                │
              ▼                                ▼
┌─────────────────────────────────────────────────────────────┐
│                      Domain / Business Layer                │
│  - MediaPlaybackManager (Play, Pause, Seek, Speed, Queue)   │
│  - MediaDownloadPipeline (Stream Resolver, File Downloader) │
│  - MediaMetadataExtractor & Tag Parser                      │
└─────────────────────────────┬───────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      Data & Storage Layer                   │
│  - Room Database (TrackEntity, DownloadEntity, PlaylistDao) │
│  - Android MediaStore / Internal Scoped Storage             │
│  - Network Client (Retrofit/OkHttp Link Resolver)           │
│  - Android MediaPlayer / AudioTrack Session                 │
└─────────────────────────────────────────────────────────────┘
```

---

## 5. Media Pipeline & Playback Engine

### 5.1 Audio & Video Playback Engine
- **Playback Architecture**: Powered by Android's native `MediaPlayer` / `AudioTrack` with audio session ID integration.
- **Player State Management**:
  - `PlaybackState`: `IDLE`, `BUFFERING`, `PLAYING`, `PAUSED`, `COMPLETED`, `ERROR`.
  - `QueueManager`: Maintains index, active track, shuffle order, and repeat loop (`OFF`, `ALL`, `ONE`).
  - `AudioSession/DSP`: Exposes audio session ID for real-time visualizer waveform rendering and equalizer controls.

### 5.2 Download & Link Analysis Pipeline
- **Resolver Pipeline**: OkHttp client parses online media links, extracts direct audio/video streams, fetches metadata (title, artist, duration, cover art URL).
- **Download Execution**: Foreground/Background download stream pipeline with chunks written directly to app-specific external/internal storage (`context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)`).
- **Indexing**: Once download finishes, metadata and file path are committed to the local Room database.

---

## 6. Verified APIs vs Planned Contracts

- **Verified Existing**: Android Activity lifecycle and Compose root in `MainActivity.kt`.
- **Planned Presentation Contracts**:
  - `PlayerUiState(currentTrack, isPlaying, positionMs, durationMs, shuffle, repeat, queue)`
  - `DownloadUiState(url, isAnalyzing, resolvedStreams, activeDownloads, downloadProgress)`
  - `LibraryUiState(tracks, filterMode, searchQuery, favorites)`
- **Planned Local Storage Contracts**:
  - `TrackEntity(id, title, artist, album, durationMs, filePath, artworkUri, isFavorite, format, addedAt)`
  - `TrackDao(getAllTracks(), searchTracks(), getFavorites(), insertTrack(), updateFavorite(), deleteTrack())`

---

## 7. Unknowns & Pending Validations
- *External Link Resolver*: The exact external video/audio extraction service/API has not been bound in the template; an open, standards-compliant extractor contract will be structured in Phase 5.
- *Native Audio DSP*: Visualizer requires `android.media.audiofx.Visualizer` or synthetic FFT analysis from the media buffer.
