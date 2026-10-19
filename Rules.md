# NEONWAVE — Mandatory Development Rules

## 1. Absolute Integrity Rules

1. **NO DEMO APPLICATIONS**: Never create demo screens, prototype sandboxes, preview-only mockups, static sample dashboards, or duplicate screens. All code must belong to the real, functional NEONWAVE application.
2. **NO MOCK DATA REPLACING REAL APPLICATION DATA**: Display genuine tracks, real metadata, and actual storage files. Never populate empty libraries with fabricated sample songs. Always display honest empty states when data is unavailable.
3. **NO FAKE BUTTONS OR SIMULATED FUNCTIONALITY**: Every visible interactive control must perform its actual intended action:
   - Play/pause must control the real playback engine.
   - Progress sliders and timers must bind to actual elapsed playback time and track duration.
   - Download buttons must invoke real media extraction and file download.
   - Analyze Link must execute genuine link resolution.
   - Search, filters, favorites, queue, and navigation must manipulate actual collections and state.
   - Never simulate progress, success, or completion. If an operation fails, display an honest error.
4. **NO PLACEHOLDER IMPLEMENTATIONS PRESENTED AS COMPLETE**: Incomplete features must be clearly documented in `Memory.md` and surfaced honestly in the UI as under development or disabled, never faked.

---

## 2. Preservation & Stability Rules

5. **PRESERVE EXISTING FUNCTIONALITY**: Do not remove, replace, or break working functionality, existing audio sources, download pipelines, or database schemas.
6. **NO UNNECESSARY REWRITES OR DUPLICATE COMPONENTS**: Reuse existing components, state managers, and utility classes in place. Never create parallel duplicates.
7. **NO UNDOCUMENTED API CONTRACT CHANGES**: Any modification to data models, DAO interfaces, or network contracts must be documented first in `Architecture.md` and approved.
8. **ISOLATE UI WORK FROM CORE MEDIA LOGIC**: When refining presentation or styling, do not make arbitrary changes to media processing, download streams, or playback internals.
9. **PRESERVE MEDIA CONTROLS & TIMERS**: Audio playback must retain seeking, actual duration calculation, repeat modes, shuffle sequencing, and audio session binding.

---

## 3. Engineering & Verification Discipline

10. **VERIFIED EXECUTION ONLY**: Never claim a Gradle build, compilation, or test passed without actual execution and verified terminal output.
11. **INSPECT BEFORE MODIFYING**: Always read the target file (`view_file`) and inspect dependencies before making changes.
12. **MINIMAL TARGETED EDITS**: Make the smallest targeted change that solves the problem. Avoid mass refactoring across unrelated subsystems.
13. **TRANSPARENT REPORTING**: In every response, report:
    - Exactly which files were created or modified.
    - Actual test or build commands executed and their direct outputs.
    - Any unverified or partially verified behaviors.
14. **PLATFORM SYNCHRONIZATION**: Maintain strict synchronization between `res/values/strings.xml` (`app_name`), `metadata.json` (`name`), and `settings.gradle.kts` (`rootProject.name`).
