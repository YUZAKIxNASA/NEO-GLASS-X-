# NEONWAVE — Aurora Glass Design System Specification

## 1. Visual Source of Truth & Brand Identity
The visual identity of NEONWAVE is directly derived from the **Aurora Glass** design language exhibited in the reference visual specification (`Screenshot_20261018-235942.ChatGPT.png`). It balances deep midnight depth, radiant cyan and violet ambient light cones, frosted translucent glass panels, and crisp typography.

**Core Brand Identity**:
- **Application Name**: NEONWAVE
- **Aesthetic**: Aurora Glass (Deep Oceanic Navy + Radial Violet/Cyan Lighting + Translucent Frosted Glass + Neumorphic Glow)
- **Design Philosophy**: No flat black screens, no generic dashboard boxes, and no harsh neon borders. Every element feels suspended in an illuminated audio atmosphere.

---

## 2. Color Palette & Lighting Model

### Primary Surface & Background Tokens
| Token Name | Hex Code | Purpose & Application |
|---|---|---|
| `AtmosphereDeep` | `#07102B` | Deep navy base foundation for the entire viewport background |
| `AtmosphereMid` | `#0D1945` | Mid-depth gradient transition layer |
| `RoyalBlue` | `#244CCB` | Core blue accent and ambient radial glow emitter |
| `IndigoAura` | `#4434A6` | Intermediate violet-indigo gradient fill |
| `VioletGaze` | `#8067F5` | Primary vibrant interactive accent, active pills, and play buttons |
| `CyanHighlight` | `#43E5F5` | High-contrast neon highlight, audio visualizers, and scrubber glow |
| `LavenderGlow` | `#B6A8FF` | Glass specular reflection borders and soft ambient highlights |
| `CoralAccent` | `#FF4B72` | Favorite heart fill and active alert badge color |

### Glass Surface Tokens
- **`GlassSurfaceBase`**: `Color(0xFF131D4A).copy(alpha = 0.55f)` with 1dp border `Color(0xFFFFFFFF).copy(alpha = 0.12f)`
- **`GlassSurfaceElevated`**: `Color(0xFF1C2766).copy(alpha = 0.65f)` with 1dp border `Color(0xFFB6A8FF).copy(alpha = 0.22f)`
- **`GlassSurfaceActive`**: Brush gradient `Brush.horizontalGradient(listOf(Color(0xFF4434A6), Color(0xFF8067F5)))` with glowing shadow
- **`GlassInputSurface`**: `Color(0xFF0E163B).copy(alpha = 0.70f)` with border `Color(0xFF43E5F5).copy(alpha = 0.35f)`

### Text & Icon Contrast Hierarchy
- **`TextPrimary`**: `#F8F7FF` (100% white-violet) — titles, track names, primary actions.
- **`TextSecondary`**: `#B8C3E0` (75% lavender-slate) — subtitles, artists, duration, captions.
- **`TextMuted`**: `#7280AB` (50% deep slate) — inactive icons, placeholder text, timestamps.

---

## 3. Aurora Glass Elevation & Surface Specifications

### Glass Card Modifier Formula (Compose)
```kotlin
Modifier
    .clip(RoundedCornerShape(24.dp))
    .background(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF283675).copy(alpha = 0.40f),
                Color(0xFF0F183E).copy(alpha = 0.60f)
            )
        )
    )
    .border(
        width = 1.dp,
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFB6A8FF).copy(alpha = 0.30f),
                Color(0xFF43E5F5).copy(alpha = 0.08f)
            )
        ),
        shape = RoundedCornerShape(24.dp)
    )
```

### Corner Radius Standards
- **Hero Banners & Feature Cards**: `24.dp` – `28.dp`
- **Album Artwork & Thumbnail Cards**: `20.dp` – `24.dp`
- **Category & Filter Pills**: `20.dp` – `28.dp` (Full pill capsule)
- **Track List Rows**: `18.dp` – `20.dp`
- **Buttons (Play/Action)**: Full circular `CircleShape` or capsule pill `RoundedCornerShape(50)`

---

## 4. Screen-by-Screen Layout Compositions

### 4.1 Home Screen Layout
1. **Header**:
   - Left: Rounded square glass icon button (Menu / Grid).
   - Center: "NEONWAVE" with gradient soundwave icon and subtext "Feel the music, live the moment."
   - Right: Notification bell with badge dot, accompanied by circular user avatar badge.
2. **Hero Featured Banner**:
   - Rounded glass card (height ~180dp).
   - Left content: "Good Vibes Only", "Curated playlist for your best moments.", illuminated "Play Now" pill button.
   - Right content: Immersive artwork with cyan/violet rim lighting.
   - Indicator dots for carousel pagination.
3. **Application Highlights (3 Coordinated Cards)**:
   - *Online Downloader*: Violet/cyan tint icon, download badge, link resolution.
   - *Real-Time Player*: Waveform icon, high-res audio playback engine.
   - *Music Library*: Folder/music icon, organized storage & playlist manager.
4. **Recently Played Strip**:
   - Header with "Recently Played" and "See all" text button.
   - Horizontal `LazyRow` of compact square cards (~120dp wide) with artwork, title, track count, and overlaid circular play button.
5. **Recommended For You**:
   - Header with "Recommended for you" and "See all".
   - Vertical stack of glass track cards with artwork, track title, artist name, favorite heart toggle, and play button.
6. **Bottom Navigation Bar**:
   - Docked floating glass bar with Home, Explore, Library, and Player tabs.
   - Active tab highlighted by an illuminated gradient violet pill with icon and text.

### 4.2 Online Downloader Screen Layout
1. **Header**: "Online Media Downloader" with subtitle "Paste any media link to analyze and extract audio or video."
2. **URL Entry Panel**: Elevated frosted glass container featuring:
   - Glass text field with search/link icon, hint "Paste YouTube, SoundCloud, or media URL..."
   - Quick "Paste" button reading system clipboard.
   - Primary "Analyze Link" glowing violet/cyan gradient button.
3. **Extraction & Stream Selector**:
   - Media summary card with resolved video thumbnail, title, channel/artist, and duration.
   - Format selector tabs (Audio MP3 vs Video MP4).
   - Stream quality chips (320kbps, 192kbps, 1080p, 720p, 480p) with estimated file sizes.
4. **Download Queue & Progress**:
   - Active download item with animated progress bar, downloaded bytes / total bytes, transfer rate (e.g. `2.4 MB/s`), and cancel/pause control.
   - Honest error feedback with retry action if link resolution fails.

### 4.3 Library Screen Layout
1. **Header & Search Bar**:
   - Large bold "MUSIC LIBRARY" title.
   - Search bar inside frosted glass capsule with leading search icon and trailing clear button.
2. **Category Filter Row**:
   - Horizontal scrolling pills: `[All]`, `[Tracks]`, `[Videos]`, `[Downloaded]`, `[Favorites]`.
   - Selected pill highlighted with glowing violet gradient; unselected pills use translucent glass surface.
3. **Content Catalog**:
   - Multi-item track rows with high-contrast typography, duration, format tag (`MP3`, `MP4`, `FLAC`), favorite heart icon, and play action.
   - Empty state: Translucent glass card with music note icon and "No downloaded media yet. Head to Downloader to add tracks!"

### 4.4 Full Music Player (Now Playing) Layout
1. **Top Bar**:
   - Downward collapse chevron on left (48dp touch target).
   - Centered "Now Playing" title.
   - Contextual overflow menu icon on right.
2. **Featured Artwork & Ambient Glow**:
   - Centered rounded square album art (~260dp) with `24.dp` corner radius.
   - Flanking equalizer visualizer columns pulsing with playback volume/frequency.
   - Top-right corner of artwork features floating favorite heart icon.
3. **Track Metadata**:
   - Large white track title (Headline Medium).
   - Artist subtitle with lavender tone.
   - "HD" / "LOSSLESS" badge in frosted glass tag.
4. **Scrubber**:
   - Illuminated progress bar with glowing circular thumb.
   - Elapsed time on left (`1:42`), total duration on right (`3:56`).
5. **Playback Controls**:
   - Symmetrical row: Shuffle toggle, Previous track, Large glowing central Play/Pause circle (`72.dp`), Next track, Repeat mode toggle (`Off`, `All`, `One`).
6. **Quick Utility Row**:
   - Share button, Favorited heart (red/pink), Download/offline status, Queue drawer toggle.
7. **"Up Next" Peek Drawer**:
   - Frosted glass drawer displaying upcoming track title, artist, 3-dots menu, and animated equalizer wave.

---

## 5. Responsive Behavior & Accessibility
- **Screen Sizing**: Designed fluidly for compact mobile screens (360dp - 480dp width) with adaptive max-width clamping (600dp) for tablets and large displays.
- **Touch Targets**: Minimum 48dp x 48dp bounding box for all interactive icons and buttons.
- **Contrast**: Text elements strictly maintain WCAG AA contrast against their underlying translucent gradient surfaces.
- **Haptic Feedback**: Subtle vibration feedback on play/pause, seek, and favorite actions.
