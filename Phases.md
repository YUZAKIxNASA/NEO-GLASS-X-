# NEONWAVE — Phased Implementation Roadmap

This roadmap defines the structured, incremental implementation plan for NEONWAVE. No phase may commence until its prerequisites are verified and approved.

---

## Phase 1: Repository Audit & Baseline Verification [COMPLETED]
- **Goal**: Full inspection of repository files, dependencies, build configurations, and baseline test suite.
- **Prerequisites**: Access to project directory and build tools.
- **Tasks**:
  1. Audit file tree, Gradle configuration (`build.gradle.kts`, `libs.versions.toml`).
  2. Inspect existing activities, Compose themes, manifest permissions, and assets.
  3. Execute baseline local unit and Robolectric tests.
- **Exit Criteria**: Baseline build and test execution status verified; project structure accurately documented.

---

## Phase 2: Documentation & Requirements Validation [IN PROGRESS]
- **Goal**: Establish the documentation-first system to prevent regressions and guide development.
- **Prerequisites**: Phase 1 audit complete.
- **Deliverables**:
  1. `PRD.md` — Product scope, verified functionality vs pending backlog.
  2. `Architecture.md` — Technical stack, architecture diagrams, component layers.
  3. `Rules.md` — Strict operational and coding rules (no mocks, real data only).
  4. `Phases.md` — Incremental execution milestones.
  5. `Design.md` — Aurora Glass design system specifications matched to reference.
  6. `Memory.md` — Living project state, audit log, and continuity journal.
- **Exit Criteria**: All six documents created, verified, and awaiting user review.

---

## Phase 3: Aurora Glass Design-System Foundation [PENDING APPROVAL]
- **Goal**: Establish the visual language tokens, gradients, frosted glass modifiers, and common UI atoms.
- **Prerequisites**: Phase 2 approval.
- **Tasks**:
  1. Implement color tokens in `Color.kt` (#07102B deep navy, #244CCB royal blue, #4434A6 indigo, #8067F5 violet, #43E5F5 cyan highlight, #B6A8FF lavender glass).
  2. Define glass surface modifiers (translucency, subtle borders, ambient shadow, background blur fallback).
  3. Implement typography and rounded component shapes (16dp, 24dp, 28dp pill curves).
  4. Build reusable atoms: `GlassCard`, `GlowButton`, `PillCategoryChip`, `ActiveTabIndicator`.
- **Exit Criteria**: Compose preview / unit tests verifying theme rendering without compilation errors.

---

## Phase 4: Home Screen Refinement & Media Hub [PENDING]
- **Goal**: Build the primary Aurora Glass landing screen matched to Reference Image 1.
- **Prerequisites**: Phase 3 complete.
- **Tasks**:
  1. Construct compact NEONWAVE header with brand title, status icons, and user avatar.
  2. Implement featured media hero card with artwork backdrop, title, artist, and Play Now / Download actions.
  3. Implement three coordinated capability cards (Downloader, Player, Library).
  4. Implement horizontal "Recently Played" card strip with album art and play buttons.
  5. Implement "Recommended for you" vertical list with real data binding and favorite buttons.
  6. Integrate bottom navigation bar with active glowing pill indicator.
- **Exit Criteria**: Home screen matches reference visual composition and links to real application state.

---

## Phase 5: Online Downloader Screen Refinement [PENDING]
- **Goal**: Implement link extraction, format/stream selection, and real file download pipeline.
- **Prerequisites**: Phase 4 complete.
- **Tasks**:
  1. Build glass URL input card with clipboard paste and clear action.
  2. Implement link analysis pipeline (resolving title, author, duration, thumbnail, audio/video streams).
  3. Build format and quality selection cards (Audio MP3/M4A, Video 1080p/720p/480p).
  4. Implement download progress tracking (bytes, percentage, transfer speed, notification channel).
  5. Auto-index finished downloads into local storage and Room database.
- **Exit Criteria**: Downloader resolves links, performs real downloads to storage, and updates library.

---

## Phase 6: Library Screen Refinement [PENDING]
- **Goal**: Build rich, structured media library with search, filtering, and favorites.
- **Prerequisites**: Phase 5 complete.
- **Tasks**:
  1. Implement "MUSIC LIBRARY" header with real-time search input.
  2. Implement category filter pills: All, Tracks, Videos, Downloaded, Favorites.
  3. Build rich media item cards with album artwork, duration, format badge, and contextual menu.
  4. Integrate Room database entity, DAO, and repository for offline track persistence.
  5. Build elegant empty state cards when no media is downloaded yet.
- **Exit Criteria**: Library displays genuine files, supports instant filtering, search, and playback launching.

---

## Phase 7: Full Music Player & Audio Engine [PENDING]
- **Goal**: Complete the immersive Now Playing screen and background audio playback engine.
- **Prerequisites**: Phase 6 complete.
- **Tasks**:
  1. Build full-screen player sheet matched to Reference Image 2:
     - Header with dismiss chevron, "Now Playing", options menu.
     - Large rounded square album art with cyan/violet ambient glow and favorite heart.
     - Audio visualizer waveform bars flanking artwork.
     - Title, artist, HD badge, and seek bar with live elapsed/total counters.
     - Playback controls: shuffle, previous, illuminated play/pause, next, repeat.
     - Action bar: share, favorite, download, queue drawer.
     - Peekable "Up Next" queue section.
  2. Implement real `MediaPlaybackService` with notification controls and lock screen support.
  3. Implement persistent mini-player docked above bottom navigation on all screens.
- **Exit Criteria**: Continuous audio playback with real seeking, queue progression, and visualizer feedback.

---

## Phase 8: Integration & Regression Testing [PENDING]
- **Goal**: Comprehensive test coverage across all integrated workflows.
- **Prerequisites**: Phase 7 complete.
- **Tasks**:
  1. Robolectric tests for navigation, state flows, downloader parsing, and library queries.
  2. Verification of audio lifecycle (pause on call, background continuity).
  3. Memory and resource leak checks.
- **Exit Criteria**: All unit and Robolectric tests pass (`gradle :app:testDebugUnitTest`).

---

## Phase 9: Android Build & Device Verification [PENDING]
- **Goal**: Final production build verification.
- **Prerequisites**: Phase 8 complete.
- **Tasks**:
  1. Run `compile_applet` to ensure pristine compilation.
  2. Generate release/debug APK and verify package integrity.
  3. Update `metadata.json` and strings to reflect application identity.
- **Exit Criteria**: APK builds cleanly without warnings or errors.
