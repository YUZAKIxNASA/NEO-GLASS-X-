# NEO GLASS X — Project Continuity & Memory Record

## 1. Project Identity & Status
- **Project Name**: NEO GLASS X
- **Project Owner**: YUZAKI NASA
- **Platform**: Android native (Kotlin + Jetpack Compose)
- **Status**: Production Aurora Glass Clean State (Zero Demo Data, Pure MP3 Audio Downloader, Lossless Media Player)
- **Brand Consistency**: Display name "NEO GLASS X" synchronized across launcher label (`strings.xml`), platform metadata (`metadata.json`), build configuration (`settings.gradle.kts`), and UI headers.

---

## 2. Core Functional Architectural Integrity
1. **Application Identity & UI Header**:
   - Launcher and display label: "NEO GLASS X".
   - Brand headers in Home, Downloader, and Library display "NEO GLASS X".
   - Clean header with grid navigation and instant search.
2. **MP3 Downloader Repair & MP4 Deprecation**:
   - Completely removed all MP4, 1080p, 720p, and video download options.
   - Stream validation: HEAD request with fallback to byte-range GET (`Range: bytes=0-1023`).
   - Webpage (HTML) links rejected with direct diagnostic message.
   - Download validation: ID3v2 and MPEG frame sync word (`0xFFE0`) verification ensures only genuine MP3 files are accepted and indexed.
   - Direct audio indexed into Room Database and app-specific Music storage.
3. **Audio Playback Engine Repair**:
   - Resolved root cause of playback failure: `FileInputStream(file).use { fis -> player.setDataSource(fis.fd) }` was prematurely closing file descriptors before `prepareAsync()` could execute. Switched to direct file path specification (`player.setDataSource(file.absolutePath)`).
   - Moved MediaPlayer initialization to Main thread with Looper safety, while isolating file access checks on `Dispatchers.IO`.
   - Added `BecomingNoisyReceiver` for graceful pausing on headphone unplug.
   - Immediate UI synchronization of track and queue state on play requests.
   - Surfaced playback errors in `PlayerScreen` and `MiniPlayerBar` with retry affordance.
   - Synchronized offline files from disk into Room on startup via `TrackRepository.syncOfflineFiles()`.
4. **Zero Mock Data Policy**:
   - All demo tracks purged.
   - Pure real storage persistence via Room.
   - Honest empty states with CTA to open Downloader when library has no downloaded tracks.

---

## 3. Test & Verification Log
- `compile_applet`: **BUILD SUCCEEDED**
- `gradle :app:testDebugUnitTest`: **BUILD SUCCESSFUL in 44s** (33 actionable tasks executed, 0 failures, all 18 unit & Robolectric CUJ tests passing, including valid local MP3, pause/resume, seeking, queue navigation, downloaded MP3 playback, and missing file handling).
