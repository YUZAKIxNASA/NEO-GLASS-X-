# NEO GLASS X — Phased Implementation Roadmap

This roadmap defines the structured, incremental implementation plan for NEO GLASS X.

---

## Phase 1: Repository Audit & Baseline Verification [COMPLETED]
- **Goal**: Full inspection of repository files, dependencies, build configurations, and baseline test suite.
- **Verification**: Gradle build and test execution status verified; project structure accurately documented.

---

## Phase 2: Documentation & Requirements Validation [COMPLETED]
- **Goal**: Establish the documentation-first system to prevent regressions and guide development.
- **Deliverables**:
  1. `PRD.md` — Product scope and specifications.
  2. `Architecture.md` — Technical stack, architecture diagrams, component layers.
  3. `Rules.md` — Strict operational and coding rules (no mocks, real data only).
  4. `Phases.md` — Incremental execution milestones.
  5. `Design.md` — Aurora Glass design system specifications.
  6. `Memory.md` — Living project state, audit log, and continuity journal.

---

## Phase 3: Aurora Glass Design-System Foundation [COMPLETED]
- **Goal**: Establish the visual language tokens, gradients, frosted glass modifiers, and common UI atoms.
- **Status**: Implemented in `Color.kt`, `Theme.kt`, `GlassModifiers.kt`.

---

## Phase 4: Rebranding & Name Normalization to NEO GLASS X [COMPLETED]
- **Goal**: Unify application identity as **NEO GLASS X**.
- **Tasks**:
  1. Update `res/values/strings.xml` (`app_name = "NEO GLASS X"`).
  2. Update `metadata.json` (`name = "NEO GLASS X"`).
  3. Update `settings.gradle.kts` (`rootProject.name = "NEO GLASS X"`).
  4. Update Compose theme naming (`NeoGlassXTheme`, `NeoGlassXColorScheme`).
  5. Update Home, Downloader, and Library screen headings.
  6. Verify unit and Robolectric tests reflect the new brand name.

---

## Phase 5: MP4 & Video Option Deprecation [COMPLETED]
- **Goal**: Transition downloader and data models strictly to high-quality MP3 audio.
- **Tasks**:
  1. Remove video/MP4 format selections from `MediaDownloader.kt` and `DownloaderScreen.kt`.
  2. Restrict downloadable format to MP3 audio only.
  3. Remove video-related icons, state, and dead code.
  4. Clean up any video filters from library screen while retaining genre and category chips.

---

## Phase 6: URL Analysis & MP3 Download Repair [COMPLETED]
- **Goal**: Ensure robust link resolution, audio stream validation, and honest error handling.
- **Tasks**:
  1. HTTP HEAD with fallback to byte-range GET (`Range: bytes=0-1023`).
  2. Reject HTML webpages with clear, helpful feedback.
  3. Verify downloaded file headers for ID3v2 tags or MPEG sync words (`0xFFE0`).
  4. Accurately extract duration and index into Room database.

---

## Phase 7: Verification & Build Validation [COMPLETED]
- **Goal**: Verify full app compilation, unit tests, and Robolectric test execution.
