# NEO GLASS X — Technical Architecture Document

## 1. Executive Technical Summary
NEO GLASS X is built as a single-activity native Android application utilizing Kotlin and Jetpack Compose for declarative UI rendering. The architecture adheres to MVVM (Model-View-ViewModel) and Clean Architecture principles, ensuring strict decoupling between the presentation layer, domain logic, media player engine, and persistence layer.

---

## 2. Technology Stack & Verification
- **Language**: Kotlin 2.0+
- **UI Toolkit**: Jetpack Compose with Material 3 + Custom Aurora Glass Design System
- **Image Loading**: Coil Compose (`AsyncImage`)
- **Persistence**: Android Room (`@Database`, `@Entity`, `@Dao`, KSP)
- **Audio Playback Engine**: Android `MediaPlayer` wrapped in `PlaybackManager` with `AudioFocusRequest` & `AudioManager.OnAudioFocusChangeListener`
- **Network & Downloader**: `OkHttpClient` with coroutine-based stream chunking, HEAD/Range probe, and MPEG sync validation
- **Architecture Components**: `AndroidViewModel`, `StateFlow`, `collectAsStateWithLifecycle`
- **Testing**: JUnit4 + Robolectric for local JVM CUJ tests

---

## 3. Directory & Package Structure

```
app/src/main/
├── AndroidManifest.xml
├── java/com/example/
│   ├── MainActivity.kt               # Single Activity entry point with back handler & edge-to-edge
│   ├── data/
│   │   ├── db/
│   │   │   ├── AppDatabase.kt        # Room database singleton
│   │   │   ├── TrackDao.kt           # Room DAO with Flows for tracks, favorites, downloads
│   │   │   └── TrackEntity.kt        # Room entity with converter methods
│   │   ├── downloader/
│   │   │   └── MediaDownloader.kt    # OkHttp stream analyzer, validator & download pipeline
│   │   ├── model/
│   │   │   └── Track.kt              # Core data model for audio tracks
│   │   └── repository/
│   │       └── TrackRepository.kt    # Data repository bridging Room and domain models
│   ├── player/
│   │   └── PlaybackManager.kt        # MediaPlayer wrapper, queue, audio focus & ticker
│   └── ui/
│       ├── components/
│       │   ├── MiniPlayerBar.kt      # Docked bottom mini player
│       │   └── NeonWaveBottomBar.kt  # NeoGlassBottomBar floating glass navigation bar
│       ├── screens/
│       │   ├── DownloaderScreen.kt   # MP3 downloader UI, link input & download progress
│       │   ├── HomeScreen.kt         # Brand header, hero banner, recent tracks & overview
│       │   ├── LibraryScreen.kt      # Explore & offline library with search & category filters
│       │   └── PlayerScreen.kt       # Fullscreen immersive Aurora Glass player & queue sheet
│       ├── theme/
│       │   ├── Color.kt              # Aurora Glass color tokens (Deep Navy, Royal Blue, Violet, Cyan)
│       │   ├── GlassModifiers.kt     # GlassCard, GlassSurface, VisualizerBars
│       │   ├── Theme.kt              # NeoGlassXTheme Material 3 dark color scheme
│       │   └── Type.kt               # Typography definitions
│       └── viewmodel/
│           └── MainViewModel.kt      # Central ViewModel managing state, tabs, player & downloader
└── res/
    ├── drawable/                     # Adaptive launcher vector drawables
    ├── values/
    │   ├── colors.xml
    │   ├── strings.xml               # app_name = "NEO GLASS X"
    │   └── themes.xml
```

---

## 4. Audio Playback Pipeline
1. **User Triggers Playback**: `MainViewModel.playTrack(track, queue)` is called.
2. **PlaybackManager Preparation**:
   - Updates `PlayerState` to `isBuffering = true`.
   - Checks if track is local (via `localPath` or `file:` URI) or remote.
   - For local tracks, reads via `FileInputStream(file).fd` for zero-latency local playback.
   - Prepares asynchronously via `mediaPlayer.prepareAsync()`.
3. **Audio Focus & Start**:
   - Requests audio focus via modern `AudioFocusRequest` (API 26+) or legacy `AudioManager`.
   - On prepared, starts `MediaPlayer`, fires state update (`isPlaying = true`), and launches coroutine ticker for progress scrubbing (every 250ms).
4. **Queue Management & Modes**:
   - Next / Previous track transitions seamlessly.
   - Repeat modes: `OFF`, `ALL`, `ONE`.
   - Shuffle mode selects random non-current indices.
5. **State Propagation**:
   - `PlayerState` emits to Compose UI via `StateFlow` and `collectAsStateWithLifecycle()`.

---

## 5. MP3 Download & Stream Validation Pipeline
1. **Link Analysis (`MediaDownloader.analyzeUrl`)**:
   - Validates HTTP/HTTPS URL syntax.
   - Probes URL with HTTP HEAD request (`User-Agent: NEO-GLASS-X-Downloader/1.0`).
   - If HEAD is unsupported (e.g. 405 Method Not Allowed), falls back to limited GET with `Range: bytes=0-1023`.
   - Rejects non-audio HTML webpages with actionable error message.
   - Derives sanitized song title and artist from URL or `Content-Disposition`.
   - Configures pure MP3 format option.
2. **Download Execution (`MediaDownloader.startDownload`)**:
   - Streams bytes to a temporary `.tmp` file in app-specific Music directory.
   - Updates download progress fraction, speed (MB/s), and byte counters every 200ms.
   - Validates downloaded file headers: checks ID3v2 tag (`ID3`) or MPEG frame sync word (`0xFFE0`).
   - Atomically renames valid temporary file to `.mp3`.
   - Extracts genuine audio duration via `MediaMetadataRetriever`.
   - Inserts new `Track` record into Room database.
