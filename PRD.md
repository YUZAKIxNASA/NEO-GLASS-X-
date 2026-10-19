# NEO GLASS X — Product Requirements Document (PRD)

## 1. Overview & Vision
**NEO GLASS X** is a high-fidelity, Aurora Glass-styled native Android audio player and direct MP3 downloader. It combines offline music playback, direct MP3 stream analysis and validation, an organized audio library with favorites management, and an immersive Aurora Glass now-playing experience with audio visualization and queue control.

---

## 2. Target Users & Core Personas
1. **Audiophiles & Music Lovers**: Users wanting a fluid, visually stunning local music player with album artwork display, queue management, responsive playback controls, and audio visualizer feedback.
2. **Offline Listeners & Curators**: Users who download MP3 audio tracks for offline listening during commutes, gym sessions, or travel, requiring honest stream validation, format identification, and instant library cataloging.
3. **Music Collectors**: Users requiring fast link analysis, direct MP3 stream acquisition, and organized library filtering (All, Downloaded, Favorites, Genres).

---

## 3. Product Workflows & Screen Specifications

### 3.1 Home Screen
- **Brand Header**: Compact NEO GLASS X branding with audio equalizer glyph, quick search shortcut, and sleek grid navigation toggle.
- **Hero Featured Music Card**: Large rounded translucent glass banner showcasing currently selected/playing track or quick link to Downloader.
- **Application Capabilities Overview**: Three coordinated glass cards:
  1. *Downloader*: High-speed direct MP3 link extraction and offline saving.
  2. *Player*: Seamless lossless audio playback with visualizer ambience and queue controls.
  3. *Library*: Instant access to offline downloads, favorites, and curated categories.
- **Recently Played**: Horizontal scroll of curated/recent media cards with album art and one-touch play.
- **Recent Tracks**: Vertical list of track cards with quick play, favorite toggling, and duration badges.
- **Persistent Mini-Player**: Bottom docked glass bar showing currently playing track, progress, and play/pause/skip controls.
- **Bottom Navigation**: Unified 3-tab bar (Home, Downloader, Library).

### 3.2 Online MP3 Downloader Screen
- **Media Link Input Bar**: Frosted glass URL input field with clipboard paste support and clear button.
- **Analyze Action**: Real link analysis engine resolving direct MP3 audio metadata (HEAD and range-GET stream validation, content-type verification, and stream length inspection).
- **Format Verification**: Pure MP3 audio extraction—no MP4 or video options.
- **Download Manager**: Real-time progress bar, transfer rate (MB/s), byte counters, and completion status.
- **Integrity Validation**: Verifies MP3 header and MPEG sync word before finalizing downloads.
- **Direct Library Integration**: Automatically indexes downloaded MP3s into the local database and Music directory upon completion.
- **Error Handling & State Feedback**: Clear validation for non-audio webpages (HTML), invalid URLs, network timeouts, and storage permissions.

### 3.3 Library Screen
- **Header & Search**: Polished "Explore & Library" glass header with instant real-time filtering search bar.
- **Filter Chips**: Rounded glass category pills (All, Downloaded, Favorites, Pop, Lo-Fi, Rock, Classical).
- **Track Catalog**: Well-spaced glass cards presenting:
  - Album thumbnail artwork
  - Track title & artist
  - Duration & "SAVED" badges for local downloads
  - Animated equalizer bars for active track
  - Favorite toggle (heart icon)
  - Playback trigger
- **Empty & Loading States**: Clean frosted empty state cards with call-to-action to download media when storage is empty.

### 3.4 Full Music Player (Now Playing) Screen
- **Top Navigation Bar**: Collapse / back chevron, "Now Playing" title, and queue options menu.
- **Center Artwork Card**: Rounded square album art with ambient cyan-violet background glow and favorite heart button.
- **Visualizer Accent**: Restrained audio waveform / equalizer bars flanking the artwork.
- **Track Metadata**: Large bold track title, artist name, and audio quality badge ("HD" / "MP3").
- **Interactive Scrubber**: Seek bar with current playback time and total track duration.
- **Playback Controls**: Shuffle, Previous, Large illuminated Play/Pause button (72dp radial glow), Next, Repeat mode toggle (Off, All, One).
- **Utility Actions Row**: Share, Favorite, Downloaded status, and Queue sheet toggle.
- **Up Next Drawer / Section**: Peekable card and modal bottom sheet displaying upcoming tracks with tap-to-play.

---

## 4. Verification Status
- **Architecture**: Single Activity, MVVM + Clean Architecture with Jetpack Compose.
- **Audio Engine**: Android MediaPlayer with AudioFocus management, seek, shuffle, repeat, and background ticker.
- **Persistence**: Room Database (`AppDatabase`, `TrackDao`, `TrackEntity`).
- **Network & Downloader**: OkHttpClient with stream validation, HEAD/Range probe, and MP3 sync-word verification.
- **App Name & Identity**: Verified as NEO GLASS X across `strings.xml`, `metadata.json`, and UI components.
