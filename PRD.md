# NEONWAVE — Product Requirements Document (PRD)

## 1. Overview & Vision
**NEONWAVE** is a premium, Aurora Glass-styled media player and downloader application for Android. It combines offline music and video playback, online media discovery and downloading, a high-fidelity audio/video library with playlist/favorites management, and an immersive now-playing experience inspired by modern frosted-glass neumorphic UI.

---

## 2. Target Users & Core Personas
1. **Audiophiles & Music Lovers**: Users wanting a fluid, visually stunning local music player with album artwork display, queue management, responsive playback controls, and audio effects.
2. **Offline Listeners & Curators**: Users who download audio and video for offline commutes, travel, or gym sessions, requiring robust storage tracking, format identification, and instant library cataloging.
3. **Media Collectors**: Users requiring fast link analysis, multi-format media acquisition, and organized library filtering (All, Tracks, Videos, Downloads, Favorites).

---

## 3. Product Workflows & Screen Specifications

### 3.1 Home Screen
- **Brand Header**: Compact NEONWAVE branding with ambient status indicators.
- **Hero Featured Media Card**: Large rounded translucent glass banner showcasing current/recommended track or featured playlist with quick "Play Now" and "Download" actions.
- **Workflow Highlights**: Three coordinated glass cards explaining key application capabilities:
  1. *Online Downloader*: High-speed link extraction and offline saving.
  2. *Real-Time Player*: Seamless audio playback with visualizer ambience and queue controls.
  3. *Organized Library*: Instant access to offline downloads, favorites, and recent history.
- **Recently Played / Quick Mixes**: Horizontal scroll of curated/recent media cards with album art and one-touch play.
- **Recommended / Daily Tracks**: Vertical list of track cards with quick play and favorite toggling.
- **Persistent Mini-Player**: Bottom docked glass bar showing currently playing track, progress, and play/pause/skip controls.
- **Bottom Navigation**: Unified 4-5 tab bar (Home, Online/Explore, Library, Search/Player).

### 3.2 Online Downloader Screen
- **Media Link Input Bar**: Frosted glass URL input field with clipboard paste support and clear button.
- **Analyze Action**: Real link analysis engine resolving media metadata (title, author/channel, duration, thumbnail/artwork, streams).
- **Stream/Format Selector**: Audio extraction (MP3/M4A) and Video stream selection (1080p, 720p, 480p, 360p) with estimated file sizes.
- **Download Manager**: Real-time progress bar, transfer rate, byte counters, and completion status.
- **Direct Library Integration**: Automatically indexes downloaded media into the local storage catalog upon completion.
- **Error Handling & State Feedback**: Clear validation for invalid URLs, network timeouts, and storage permissions.

### 3.3 Library Screen
- **Header & Search**: Polished "Music Library" glass header with instant real-time filtering search bar.
- **Filter Chips**: Rounded glass category pills (All, Tracks, Videos, Downloaded, Favorites).
- **Track & Video Catalog**: Well-spaced glass cards presenting:
  - Album/video thumbnail artwork
  - Track title & artist
  - Duration & file format badges (e.g. MP3, MP4, FLAC)
  - Favorite toggle (heart icon)
  - Playback trigger and contextual menu (Delete, Add to Queue, Share)
- **Empty & Loading States**: Clean frosted empty state cards with call-to-action to download media when storage is empty.

### 3.4 Full Music Player (Now Playing) Screen
- **Top Navigation Bar**: Collapse / back chevron, "Now Playing" title, and contextual options menu.
- **Center Artwork Card**: High-resolution rounded square album art with ambient cyan-violet background glow and favorite heart button.
- **Visualizer Accent**: Restrained audio waveform / equalizer bars flanking the artwork.
- **Track Metadata**: Large bold track title, artist name, and audio quality badge (e.g. "HD", "LOSSLESS").
- **Interactive Scrubber**: Seek bar with current playback time and total track duration.
- **Playback Controls**: Shuffle, Previous, Large illuminated Play/Pause button, Next, Repeat mode toggle.
- **Utility Actions Row**: Share, Favorite, Download/Save status, and Queue / Equalizer drawer toggle.
- **Up Next Drawer / Section**: Peekable queue card displaying upcoming tracks with drag reorder or tap-to-skip.

---

## 4. Current Verification Status (Baseline Audit)

| Component / Feature | Intended Specification | Actual Codebase State | Status |
|---|---|---|---|
| Android Project Base | Modern Compose + AGP | Verified: Kotlin 2.2.10, AGP 9.1.1, Compose BOM | **VERIFIED** |
| Unit Test Suite | JVM & Robolectric tests | Verified: `ExampleUnitTest.kt`, `ExampleRobolectricTest.kt` | **VERIFIED** |
| Aurora Glass Theme | Deep navy, violet/cyan gradients, blur | Template default Material3 colors (`Color.kt`, `Theme.kt`) | **PENDING PHASE 3** |
| Home Screen | Hero card, highlights, recents | Template `Greeting("Android")` in `MainActivity.kt` | **PENDING PHASE 4** |
| Online Downloader | URL analyze, stream picker, download | Not yet implemented in repository | **PENDING PHASE 5** |
| Library Screen | Filterable track catalog, search, favorites | Not yet implemented in repository | **PENDING PHASE 6** |
| Full Music Player | Artwork, visualizer, seek, queue | Not yet implemented in repository | **PENDING PHASE 7** |
| Playback Engine | ExoPlayer/Media3 audio player service | Not yet implemented in repository | **PENDING PHASE 7** |
| Storage & Database | Room entity/DAO or file storage | Room dependencies present in Gradle catalog | **PENDING PHASE 6** |

---

## 5. Non-Functional Requirements
1. **Performance**: Smooth 60/120 fps Compose rendering; zero UI stutter during playback state updates.
2. **Audio Reliability**: Uninterrupted playback during screen lock, app switching, and configuration changes.
3. **No Mock Fallbacks in Production**: Use genuine Android MediaPlayer/ExoPlayer and real storage files. Real error messages for network failures.
4. **Android Accessibility**: Minimum 48dp touch targets, semantic content descriptions for screen readers.
5. **Zero External Breakages**: Adhere to Android 14/15 storage permissions and background service lifecycles.
