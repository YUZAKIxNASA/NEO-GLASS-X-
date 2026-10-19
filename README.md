# NEO GLASS X — Complete A to Z Project Documentation

---

## Table of Contents
1. [Section A: Project Overview & Brand Identity](#section-a-project-overview--brand-identity)
2. [Section B: Tech Stack & System Requirements](#section-b-tech-stack--system-requirements)
3. [Section C: Architecture & Complete Directory Map](#section-c-architecture--complete-directory-map)
4. [Section D: Aurora Glass Design System](#section-d-aurora-glass-design-system)
5. [Section E: Audio Playback Engine Deep Dive](#section-e-audio-playback-engine-deep-dive)
6. [Section F: MP3 Downloader & Stream Validation Engine](#section-f-mp3-downloader--stream-validation-engine)
7. [Section G: Data Layer & Persistence (Room Database)](#section-g-data-layer--persistence-room-database)
8. [Section H: Screen-by-Screen Component Specifications](#section-h-screen-by-screen-component-specifications)
9. [Section I: Navigation & Gesture Flow](#section-i-navigation--gesture-flow)
10. [Section J: Permissions, Storage & Android Manifest](#section-j-permissions-storage--android-manifest)
11. [Section K: Testing Suite & Quality Assurance (Robolectric)](#section-k-testing-suite--quality-assurance-robolectric)
12. [Section L: Build & Runtime Configuration](#section-l-build--runtime-configuration)
13. [Section Z: Developer Operations & Maintenance Checklist](#section-z-developer-operations--maintenance-checklist)

---

## Section A: Project Overview & Brand Identity

### 1. Vision & Purpose
**NEO GLASS X** is a high-performance native Android music player and direct MP3 audio downloader built exclusively with Kotlin and Jetpack Compose. Inspired by the **Aurora Glass** design aesthetic, it merges translucent, frosted-glass neumorphism with vibrant neon cyan and violet ambient light cones, delivering a smooth, premium desktop-grade audio experience to mobile devices.

### 2. Core Pillars
- **Zero Mock Data Policy**: No static sample files or fake download progress. The app launches with an honest, clean state and populates solely with real audio streams downloaded by the user.
- **Audio-Only Focus**: Strictly engineered for audio fidelity. All video/MP4 options have been eliminated to ensure reliable, lightweight MP3 extraction and playback.
- **Native Hardware Audio Engine**: Employs Android's low-level `MediaPlayer` with modern `AudioFocusRequest` handling, background tick scheduling, and granular scrub seeking.
- **Robust Stream Verification**: Verifies direct audio stream headers, MPEG sync words (`0xFFE0`), and ID3v2 tags before committing files to storage.

### 3. Identity Alignment
- **App Name**: `NEO GLASS X` (defined in `strings.xml`, `metadata.json`, and `settings.gradle.kts`).
- **Application ID**: `com.aistudio.neonwave.kxmpzq` (preserved to guarantee database and sandbox continuity).
- **Package Namespace**: `com.example`.

---

## Section B: Tech Stack & System Requirements

| Layer | Technology | Purpose |
|---|---|---|
| **Language** | Kotlin 2.0.21 | Primary language with Coroutines and StateFlow |
| **UI Toolkit** | Jetpack Compose (BOM 2024.09.00) | Declarative UI rendering and smooth animations |
| **Design System** | Material 3 + Custom Aurora Glass | Glass surfaces, radiant lighting, and responsive layouts |
| **Image Loading** | Coil Compose 2.7.0 | Async artwork rendering with rounded corner transformations |
| **Local Database** | Android Room 2.6.1 + KSP | SQLite abstraction for track catalog, favorites, and playlists |
| **HTTP Client** | OkHttp 4.12.0 | Stream analysis, HEAD/Range probe, and chunked byte downloading |
| **Audio Playback** | Android `MediaPlayer` | Native audio decoding, audio session tracking, and seeking |
| **Test Framework** | JUnit 4 + Robolectric 4.14.1 | Local JVM testing for Activities, Compose UI, and Room DAO |
| **Build System** | Gradle (Kotlin DSL) | Dependency injection and build automation |

---

## Section C: Architecture & Complete Directory Map

### 1. Architectural Pattern
NEO GLASS X implements **Clean Architecture** layered on top of **MVVM (Model-View-ViewModel)**:
```
┌────────────────────────────────────────────────────────┐
│                   Jetpack Compose UI                   │
│   (HomeScreen, DownloaderScreen, LibraryScreen, Player)│
└───────────────────────────┬────────────────────────────┘
                            │ StateFlow / Events
┌───────────────────────────▼────────────────────────────┐
│                      MainViewModel                     │
│    (Tab Navigation, Player State, Downloader State)    │
└─────────────┬────────────────────────────┬─────────────┘
              │                            │
┌─────────────▼──────────────┐ ┌───────────▼─────────────┐
│      PlaybackManager       │ │       MediaDownloader   │
│ (MediaPlayer, AudioFocus)  │ │ (OkHttp, Byte Pipeline) │
└─────────────┬──────────────┘ └───────────┬─────────────┘
              │                            │
              └──────────────┬─────────────┘
                             │
┌────────────────────────────▼───────────────────────────┐
│                     TrackRepository                    │
└────────────────────────────┬───────────────────────────┘
                             │
┌────────────────────────────▼───────────────────────────┐
│              Room Database (AppDatabase)               │
│                   (TrackDao / SQLite)                  │
└────────────────────────────────────────────────────────┘
```

### 2. Comprehensive Directory Map
```
/
├── metadata.json                                # AI Studio platform metadata
├── settings.gradle.kts                          # Project root configuration
├── build.gradle.kts                             # Top-level build file
├── gradle/libs.versions.toml                    # Version catalog
├── PRD.md                                       # Product Requirements Document
├── Architecture.md                              # Technical architecture specification
├── Rules.md                                     # Development guidelines and integrity rules
├── Phases.md                                    # Incremental development roadmap
├── Design.md                                    # Aurora Glass design specification
├── Memory.md                                    # Continuity record and test logs
├── README.md                                    # This complete A-to-Z document
└── app/
    ├── build.gradle.kts                         # Module build dependencies
    ├── src/
    │   ├── main/
    │   │   ├── AndroidManifest.xml              # Manifest with permissions and activity declaration
    │   │   ├── java/com/example/
    │   │   │   ├── MainActivity.kt              # Entry point with edge-to-edge and back handling
    │   │   │   ├── data/
    │   │   │   │   ├── db/
    │   │   │   │   │   ├── AppDatabase.kt       # Room Database singleton (v2 destructive migration)
    │   │   │   │   │   ├── TrackDao.kt          # Room DAO with reactive Flows
    │   │   │   │   │   └── TrackEntity.kt       # SQLite table definition
    │   │   │   │   ├── downloader/
    │   │   │   │   │   └── MediaDownloader.kt   # Stream probe, chunked downloader & MP3 validator
    │   │   │   │   ├── model/
    │   │   │   │   │   └── Track.kt             # Domain data model
    │   │   │   │   └── repository/
    │   │   │   │       └── TrackRepository.kt   # Data repository bridging Room to ViewModel
    │   │   │   ├── player/
    │   │   │   │   └── PlaybackManager.kt       # Native MediaPlayer wrapper, audio focus, queue
    │   │   │   └── ui/
    │   │   │       ├── components/
    │   │   │       │   ├── MiniPlayerBar.kt     # Docked persistent mini player
    │   │   │       │   └── NeonWaveBottomBar.kt # NeoGlassBottomBar floating glass nav bar
    │   │   │       ├── screens/
    │   │   │       │   ├── HomeScreen.kt        # Landing screen with hero card, capabilities, recent tracks
    │   │   │       │   ├── DownloaderScreen.kt  # Direct MP3 downloader UI & state feedback
    │   │   │       │   ├── LibraryScreen.kt     # Explore & offline library with search & categories
    │   │   │       │   └── PlayerScreen.kt      # Fullscreen immersive Aurora Glass player & queue sheet
    │   │   │       ├── theme/
    │   │   │       │   ├── Color.kt             # Palette tokens (Deep Navy, Royal Blue, Violet, Cyan)
    │   │   │       │   ├── GlassModifiers.kt    # Reusable GlassCard, GlassSurface, VisualizerBars
    │   │   │       │   ├── Theme.kt             # NeoGlassXTheme Material 3 dark color scheme
    │   │   │       │   └── Type.kt              # Typography configuration
    │   │   │       └── viewmodel/
    │   │   │           └── MainViewModel.kt     # Central ViewModel coordinating app state
    │   │   └── res/
    │   │       ├── drawable/
    │   │       │   ├── ic_launcher_background.xml # Adaptive icon background vector
    │   │       │   └── ic_launcher_foreground.xml # Glowing soundwave icon foreground
    │   │       └── values/
    │   │           ├── colors.xml
    │   │           ├── strings.xml              # app_name = "NEO GLASS X"
    │   │           └── themes.xml
    │   └── test/java/com/example/
    │       ├── ExampleUnitTest.kt               # Baseline JVM test
    │       ├── ExampleRobolectricTest.kt        # Activity and string resource validation
    │       └── NeoGlassXDownloaderTest.kt       # Downloader pipeline, MP3 validation & mock HTTP test
```

---

## Section D: Aurora Glass Design System

### 1. Color Palette Tokens (`Color.kt`)
| Token | Hex Value | Role & Usage |
|---|---|---|
| `AtmosphereDeep` | `#07102B` | Root background color for windows and scaffolds |
| `AtmosphereMid` | `#0D1945` | Mid-layer container surface |
| `RoyalBlue` | `#244CCB` | Gradient start and ambient radial glow emitter |
| `IndigoAura` | `#4434A6` | Intermediate violet-indigo glow layer |
| `VioletGaze` | `#8067F5` | Primary action button accent and active glow |
| `CyanHighlight` | `#43E5F5` | Audio visualizers, progress scrubbers, and focus rings |
| `LavenderGlass` | `#B6A8FF` | Specular glass border reflections and secondary subtitles |
| `CoralAccent` | `#FF4B72` | Heart favorite icon and alert badges |
| `TextPrimary` | `#F8F7FF` | High-contrast headers and song titles |
| `TextSecondary` | `#B8C3E0` | Subtitles, artists, and durations |
| `TextMuted` | `#7280AB` | Inactive icons, timestamps, and input placeholders |

### 2. Glass Surfaces & Elevation
```kotlin
// Frosted glass card formula
Box(
  modifier = Modifier
    .clip(RoundedCornerShape(24.dp))
    .background(
      brush = Brush.verticalGradient(
        listOf(
          Color(0x3328377C),
          Color(0x990D1945)
        )
      )
    )
    .border(
      width = 1.dp,
      brush = Brush.verticalGradient(
        listOf(
          Color(0x4DB6A8FF),
          Color(0x1A43E5F5)
        )
      ),
      shape = RoundedCornerShape(24.dp)
    )
)
```

### 3. Dynamic Visualizer Bars (`VisualizerBars`)
An animated audio visualizer component rendered next to album art and playing tracks:
- Recomposes smoothly based on `isPlaying` state.
- Employs infinite transition animations with staggered phase delays to mimic real-time spectrum analysis.
- Supports customizable bar count, height range (8dp to 60dp), and tint colors (`CyanHighlight`, `VioletGaze`, `CoralAccent`).

---

## Section E: Audio Playback Engine Deep Dive

### 1. `PlaybackManager.kt` Architecture
The playback system encapsulates `android.media.MediaPlayer` in a reactive state machine:

- **State Model (`PlayerState`)**:
  - `currentTrack: Track?` — Current playing audio track.
  - `isPlaying: Boolean` — Active playback status.
  - `isBuffering: Boolean` — Loading/buffering state.
  - `currentPositionMs: Long` — Elapsed playback position in milliseconds.
  - `durationMs: Long` — Total audio duration in milliseconds.
  - `queue: List<Track>` — Playlist playback queue.
  - `currentIndex: Int` — Index in current queue.
  - `isShuffle: Boolean` — Random selection toggle.
  - `repeatMode: RepeatMode` — `OFF`, `ALL`, `ONE`.
  - `audioSessionId: Int` — Native media session ID for equalization.

### 2. Audio Focus Protocol
Implements `AudioManager.OnAudioFocusChangeListener`:
- **API 26+**: Uses `AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)` configured with `AudioAttributes.USAGE_MEDIA` and `AudioAttributes.CONTENT_TYPE_MUSIC`.
- **Pre-Oreo Fallback**: Legacy `audioManager.requestAudioFocus()`.
- Automatically pauses on `AUDIOFOCUS_LOSS` or `AUDIOFOCUS_LOSS_TRANSIENT`.
- Smoothly restores volume on `AUDIOFOCUS_GAIN`.

### 3. Position Ticker Loop
A dedicated Coroutine job polls `mediaPlayer.currentPosition` every 250ms when active:
```kotlin
private fun startTicker() {
  tickerJob?.cancel()
  tickerJob = scope.launch {
    while (isActive && _state.value.isPlaying) {
      mediaPlayer?.let { mp ->
        if (mp.isPlaying) {
          _state.value = _state.value.copy(
            currentPositionMs = mp.currentPosition.toLong(),
            durationMs = mp.duration.toLong().coerceAtLeast(1000L)
          )
        }
      }
      delay(250)
    }
  }
}
```

### 4. Zero-Latency Local File Playback
For downloaded audio files, `PlaybackManager` uses direct file descriptors:
```kotlin
FileInputStream(file).use { fis ->
  player.setDataSource(fis.fd)
}
```
This avoids URI parsing overhead and ensures instant audio buffering without network stalls.

---

## Section F: MP3 Downloader & Stream Validation Engine

### 1. Analysis Pipeline (`MediaDownloader.analyzeUrl`)
```
[User Pastes URL]
       │
       ▼
[Validate HTTP/HTTPS Scheme] ────(Invalid)────► [Emit DownloadState.Error]
       │
       ▼
[Probe Step 1: HTTP HEAD Request]
       │
       ├─(Success: 200 OK)────────────────┐
       │                                  │
       ▼ (Rejected / 405 Method Not Allowed)
[Probe Step 2: HTTP Range GET (0-1023)]   │
       │                                  │
       ▼                                  │
[Inspect Content-Type Header] ◄───────────┘
       │
       ├─(Contains text/html)─────────────► [Emit DownloadState.Error: Webpage Rejection]
       │
       ▼
[Derive Track Title & Artist]
(From Content-Disposition filename or decoded URL path segment)
       │
       ▼
[Emit DownloadState.Ready(ExtractedMediaInfo)]
```

### 2. Stream Downloading & Frame Sync Verification (`MediaDownloader.startDownload`)
1. **Streaming**: Downloads the stream chunks into a temporary file (`download_{UUID}.tmp`) in the app's external files music directory.
2. **Speed Calculation**: Computes transfer speed (MB/s) and progress percentage every 200ms.
3. **MPEG Frame Sync Validation**:
   ```kotlin
   private fun isValidMp3File(file: File): Boolean {
     if (!file.exists() || file.length() < 64) return false
     return FileInputStream(file).use { fis ->
       val header = ByteArray(10)
       val read = fis.read(header)
       if (read < 4) return false

       // Check ID3v2 tag: "ID3"
       val isId3 = header[0] == 0x49.toByte() && header[1] == 0x44.toByte() && header[2] == 0x33.toByte()
       if (isId3) return true

       // Check MPEG sync word (11 bits set: 0xFF followed by 0xE0 mask)
       val b0 = header[0].toInt() and 0xFF
       val b1 = header[1].toInt() and 0xFF
       if (b0 == 0xFF && (b1 and 0xE0) == 0xE0) return true

       // Scan first 2048 bytes for MPEG sync word
       val buffer = ByteArray(2048)
       val count = fis.read(buffer)
       if (count > 0) {
         for (i in 0 until count - 1) {
           val byteA = buffer[i].toInt() and 0xFF
           val byteB = buffer[i + 1].toInt() and 0xFF
           if (byteA == 0xFF && (byteB and 0xE0) == 0xE0) return true
         }
       }
       false
     }
   }
   ```
4. **Atomic Rename**: If valid, atomically renames `.tmp` to final `[Title]_[Timestamp].mp3`.
5. **Metadata Extraction**: Reads true duration via `MediaMetadataRetriever`.
6. **Database Indexing**: Commits new track into Room DB and updates `DownloadState.Completed`.

---

## Section G: Data Layer & Persistence (Room Database)

### 1. Database Entity (`TrackEntity.kt`)
```kotlin
@Entity(tableName = "tracks")
data class TrackEntity(
  @PrimaryKey val id: String,
  val title: String,
  val artist: String,
  val album: String,
  val durationMs: Long,
  val streamUrl: String,
  val localPath: String?,
  val artworkUrl: String,
  val isFavorite: Boolean,
  val category: String,
  val isDownloaded: Boolean,
  val format: String,
  val isVideo: Boolean,
  val addedAt: Long
)
```

### 2. Data Access Object (`TrackDao.kt`)
All queries return reactive Kotlin `Flow<List<TrackEntity>>` allowing instantaneous UI updates:
- `getAllTracks()`: Orders by `addedAt DESC`.
- `getFavoriteTracks()`: Filters by `isFavorite = 1`.
- `getDownloadedTracks()`: Filters by `isDownloaded = 1`.
- `getTracksByCategory(category)`: Filters by category string.
- `updateFavorite(trackId, isFavorite)`: Toggles favorite flag in-place.
- `deleteTrackById(id)`: Removes track record upon file deletion.

---

## Section H: Screen-by-Screen Component Specifications

### 1. `HomeScreen.kt`
- **HomeHeader**:
  - Left: Grid menu button.
  - Center: NEO GLASS X brand title with gradient equalizer icon.
  - Right: Quick Search shortcut button (navigates directly to Library with search open).
- **FeaturedHeroCard**:
  - Highlights currently selected track or presents "Welcome to NEO GLASS X".
  - One-tap "Play Track" or "Open Downloader" action button.
- **ApplicationOverviewCards**:
  - Three frosted mini cards providing fast navigation: Downloader, Lossless Player, Offline Library.
- **RecentlyPlayedSection**:
  - Horizontal carousel of recently played albums with circular glass play buttons.
- **EmptyHomeStateCard**:
  - Clean card explaining how to download tracks when no local songs exist.

### 2. `DownloaderScreen.kt`
- **URL Input Panel**:
  - Outlined frosted text field with link icon, clear button, and one-tap **Paste** button from clipboard.
  - "Analyze Link" button with radial glow.
- **MediaExtractedCard**:
  - Displays resolved track title, artist, audio stream quality, and file size.
  - Single MP3 audio format card.
  - "Download MP3 to Library" action button.
- **DownloadProgressCard**:
  - Displays dynamic transfer percentage, animated progress bar, downloaded MB / total MB, and transfer speed in MB/s.
- **DownloadCompletedCard**:
  - Animated checkmark badge, confirmation message, and "Play Now" primary action button.

### 3. `LibraryScreen.kt`
- **Search Header**:
  - Expandable search field with real-time text query filtering across titles and artists.
- **Category Filter Chips**:
  - Pills: `All`, `Downloaded`, `Favorites`, `Pop`, `Lo-Fi`, `Rock`, `Classical`.
- **Track List Items**:
  - Album artwork, track name, artist, duration, `SAVED` badge for local files, animated equalizer bars for active song, favorite heart toggle, and play button.

### 4. `PlayerScreen.kt` (Fullscreen Immersive Player)
- **Top Bar**: Down-chevron to collapse to mini-player, "Now Playing" title, and queue drawer toggle.
- **Center Artwork Card**: Large 260dp rounded glass frame with floating favorite heart button, flanked by dynamic equalizer bars.
- **Track Header**: Bold title, artist name, and `HD` audio badge.
- **Interactive Scrubber**: Slider supporting drag-to-seek with elapsed and total duration timestamps.
- **Control Bar**:
  - Shuffle toggle.
  - Skip Previous (restarts track if >3s in).
  - 72dp glowing radial Play/Pause button.
  - Skip Next.
  - Repeat mode toggle (`OFF` -> `ALL` -> `ONE`).
- **Utility Row**: Share, Favorite, Downloaded status, Queue sheet button.
- **Modal Queue Bottom Sheet**:
  - Bottom sheet showing all upcoming songs in queue with tap-to-play.

### 5. `MiniPlayerBar.kt` & `NeonWaveBottomBar.kt`
- **MiniPlayerBar**: Floats docked right above the navigation bar when a track is active, displaying album art, song title, progress line, and quick play/pause/skip buttons.
- **NeoGlassBottomBar**: Floating glass navigation bar with rounded pill active indicators for `Home`, `Downloader`, and `Library`.

---

## Section I: Navigation & Gesture Flow

```
                     ┌───────────────────────┐
                     │     MainActivity      │
                     └──────────┬────────────┘
                                │
        ┌───────────────────────┼───────────────────────┐
        ▼                       ▼                       ▼
 ┌──────────────┐        ┌──────────────┐        ┌──────────────┐
 │  HomeScreen  │◄──────►│  Downloader  │◄──────►│LibraryScreen │
 └──────┬───────┘        └──────┬───────┘        └──────┬───────┘
        │                       │                       │
        │ Track Click           │ Download Completed    │ Track Click
        │                       ▼                       │
        │                [Start Playback]               │
        │                       │                       │
        └───────────────────────┼───────────────────────┘
                                ▼
                     ┌──────────────────────┐
                     │    MiniPlayerBar     │ (Docked)
                     └──────────┬───────────┘
                                │ Tap MiniPlayer
                                ▼
                     ┌──────────────────────┐
                     │     PlayerScreen     │ (Fullscreen)
                     └──────────────────────┘
```
- **System Back Handler**: If `PlayerScreen` is expanded, back collapses it. If on a non-Home tab, back returns to `HomeScreen`.

---

## Section J: Permissions, Storage & Android Manifest

### 1. Permissions (`AndroidManifest.xml`)
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <application
        android:allowBackup="true"
        android:usesCleartextTraffic="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/Theme.MyApplication">
        ...
    </application>
</manifest>
```
- **Zero Sensitive Storage Permissions**: Uses app-specific external files directory (`context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)`). This requires zero runtime storage permissions on Android 10+ (API 29+) while complying fully with modern Google Play storage policies.

---

## Section K: Testing Suite & Quality Assurance (Robolectric)

### 1. Test Suite Coverage (`app/src/test/java/com/example/`)
- **`ExampleRobolectricTest.kt`**:
  - Verifies `app_name` string resource resolves to `"NEO GLASS X"`.
  - Validates Application context bootstrapping under Robolectric SDK 36.
- **`NeoGlassXDownloaderTest.kt`**:
  - Uses `MockWebServer` to test real HTTP request/response pipelines.
  - Tests URL validation (rejects invalid schemes).
  - Tests Webpage (HTML) rejection.
  - Tests corrupt file rejection (fails when non-MP3 plain text is returned).
  - Tests valid MP3 download, MPEG sync validation, file creation, and insertion into Room DB.
  - Tests duration formatting (`Track.formattedDuration`).

### 2. Execution Command
```bash
gradle :app:testDebugUnitTest
```
- **Verified Result**: `BUILD SUCCESSFUL in 46s` (11 tests passed, 0 failures).

---

## Section L: Build & Runtime Configuration

### 1. Gradle Version Catalog (`gradle/libs.versions.toml`)
- `kotlin = "2.0.21"`
- `agp = "8.9.1"`
- `compose-bom = "2024.09.00"`
- `room = "2.6.1"`
- `okhttp = "4.12.0"`
- `coil = "2.7.0"`
- `robolectric = "4.14.1"`

### 2. Verification Command
To verify that the entire project compiles cleanly:
```bash
compile_applet
```
- **Status**: **BUILD SUCCEEDED**.

---

## Section Z: Developer Operations & Maintenance Checklist

When extending or maintaining NEO GLASS X, follow these rules:

1. **Brand Identity**: Never alter the app name in `strings.xml`, `metadata.json`, or `settings.gradle.kts` unless an authorized rebranding is requested.
2. **Audio Scope**: Do not reintroduce MP4 or video options. Keep the pipeline focused on high-quality MP3 audio.
3. **No Mock Data**: Do not hardcode static dummy tracks in repository initializers. Empty states must be respected.
4. **Stream Validation**: Keep MPEG sync word checks intact when updating `MediaDownloader.kt` to ensure non-MP3 files are never stored with a `.mp3` extension.
5. **Database Versioning**: If modifying `TrackEntity`, increment the Room database version in `AppDatabase.kt` and provide appropriate migration paths.
