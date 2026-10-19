package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Track
import com.example.ui.theme.AtmosphereDeep
import com.example.ui.theme.CoralAccent
import com.example.ui.theme.CyanHighlight
import com.example.ui.theme.GlassBorderLight
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassCard
import com.example.ui.theme.GlassSurfaceDark
import com.example.ui.theme.IndigoAura
import com.example.ui.theme.LavenderGlass
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletGaze
import com.example.ui.theme.auroraBackground
import com.example.ui.viewmodel.AppTab

@Composable
fun HomeScreen(
  tracks: List<Track>,
  onPlayTrack: (Track) -> Unit,
  onToggleFavorite: (Track) -> Unit,
  onNavigateTab: (AppTab) -> Unit,
  onExpandPlayer: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .auroraBackground()
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp),
      contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
      verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
      // 1. Header with Brand, Soundwave icon, and Quick Search button (avatar and bell removed per request)
      item {
        HomeHeader(onOpenSearch = { onNavigateTab(AppTab.LIBRARY) })
      }

      // 2. Featured Hero Card (Real track if available, or Welcome Downloader CTA)
      item {
        FeaturedHeroCard(
          featuredTrack = tracks.firstOrNull(),
          onPlayNow = {
            val track = tracks.firstOrNull()
            if (track != null) {
              onPlayTrack(track)
            } else {
              onNavigateTab(AppTab.DOWNLOADER)
            }
          },
          onOpenDownloader = { onNavigateTab(AppTab.DOWNLOADER) }
        )
      }

      // 3. Application Capabilities Overview
      item {
        ApplicationOverviewCards(
          onNavigateTab = onNavigateTab,
          onExpandPlayer = onExpandPlayer
        )
      }

      // 4. Recently Played Strip (Only rendered if genuine tracks exist)
      if (tracks.isNotEmpty()) {
        item {
          RecentlyPlayedSection(
            tracks = tracks.take(5),
            onPlayTrack = onPlayTrack,
            onSeeAll = { onNavigateTab(AppTab.LIBRARY) }
          )
        }
      }

      // 5. Library / Downloaded Tracks Section
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (tracks.isEmpty()) "My Tracks" else "Recent Tracks",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
          )
          if (tracks.isNotEmpty()) {
            Text(
              text = "${tracks.size} tracks",
              color = LavenderGlass,
              fontSize = 12.sp
            )
          }
        }
      }

      if (tracks.isEmpty()) {
        // Honest empty state when no tracks exist
        item {
          EmptyHomeStateCard(onGoToDownloader = { onNavigateTab(AppTab.DOWNLOADER) })
        }
      } else {
        items(tracks, key = { it.id }) { track ->
          TrackRowCard(
            track = track,
            onPlay = { onPlayTrack(track) },
            onToggleFavorite = { onToggleFavorite(track) }
          )
        }
      }
    }
  }
}

@Composable
private fun HomeHeader(onOpenSearch: () -> Unit) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Left Grid icon
    Box(
      modifier = Modifier
        .size(44.dp)
        .clip(RoundedCornerShape(14.dp))
        .background(GlassSurfaceDark)
        .border(1.dp, GlassBorderLight, RoundedCornerShape(14.dp)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Filled.GridView,
        contentDescription = "Menu Grid",
        tint = TextPrimary,
        modifier = Modifier.size(20.dp)
      )
    }

    // Brand and subtitle centered
    Column(
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Filled.GraphicEq,
          contentDescription = null,
          tint = CyanHighlight,
          modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "NeonWave",
          color = TextPrimary,
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.5.sp
        )
      }
      Text(
        text = "Feel the music, live the moment.",
        color = TextSecondary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Normal
      )
    }

    // Right Action: Quick Search shortcut (Replaces the removed bell and avatar)
    Box(
      modifier = Modifier
        .size(44.dp)
        .clip(CircleShape)
        .background(GlassSurfaceDark)
        .border(1.dp, GlassBorderLight, CircleShape)
        .clickable(onClick = onOpenSearch)
        .testTag("header_search_button"),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Filled.Search,
        contentDescription = "Search Library",
        tint = CyanHighlight,
        modifier = Modifier.size(20.dp)
      )
    }
  }
}

@Composable
private fun FeaturedHeroCard(
  featuredTrack: Track?,
  onPlayNow: () -> Unit,
  onOpenDownloader: () -> Unit
) {
  GlassCard(
    modifier = Modifier
      .fillMaxWidth()
      .height(180.dp)
      .testTag("featured_hero_card"),
    shape = RoundedCornerShape(28.dp),
    onClick = if (featuredTrack != null) onPlayNow else onOpenDownloader
  ) {
    Row(
      modifier = Modifier
        .fillMaxSize()
        .padding(18.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Left textual content and CTA
      Column(
        modifier = Modifier.weight(1.1f),
        verticalArrangement = Arrangement.Center
      ) {
        Text(
          text = if (featuredTrack != null) "Now Playing\nSelection" else "Welcome to\nNeonWave",
          color = TextPrimary,
          fontSize = 22.sp,
          lineHeight = 26.sp,
          fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = featuredTrack?.let { "${it.title} • ${it.artist}" }
            ?: "Your high-speed media downloader & offline audio player.",
          color = TextSecondary,
          fontSize = 12.sp,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Action button
        Row(
          modifier = Modifier
            .clip(CircleShape)
            .background(
              brush = Brush.horizontalGradient(
                listOf(RoyalBlue, VioletGaze)
              )
            )
            .border(1.dp, CyanHighlight.copy(alpha = 0.4f), CircleShape)
            .clickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = ripple(color = Color.White),
              onClick = if (featuredTrack != null) onPlayNow else onOpenDownloader
            )
            .padding(horizontal = 18.dp, vertical = 8.dp)
            .testTag("hero_action_button"),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(
            imageVector = if (featuredTrack != null) Icons.Filled.PlayArrow else Icons.Filled.CloudDownload,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(16.dp)
          )
          Text(
            text = if (featuredTrack != null) "Play Track" else "Open Downloader",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
      }

      // Right visual panel
      Box(
        modifier = Modifier
          .size(136.dp)
          .clip(RoundedCornerShape(22.dp))
          .background(
            brush = Brush.radialGradient(
              listOf(VioletGaze.copy(alpha = 0.4f), GlassSurfaceDark)
            )
          )
          .border(1.dp, LavenderGlass.copy(alpha = 0.35f), RoundedCornerShape(22.dp)),
        contentAlignment = Alignment.Center
      ) {
        if (featuredTrack != null && featuredTrack.artworkUrl.isNotBlank()) {
          AsyncImage(
            model = featuredTrack.artworkUrl,
            contentDescription = "Featured Music",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
        } else {
          // Sleek geometric soundwave icon art
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = Icons.Filled.GraphicEq,
              contentDescription = null,
              tint = CyanHighlight,
              modifier = Modifier.size(52.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "AURORA DSP",
              color = LavenderGlass,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ApplicationOverviewCards(
  onNavigateTab: (AppTab) -> Unit,
  onExpandPlayer: () -> Unit
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    OverviewMiniCard(
      title = "Downloader",
      subtitle = "Extract Links",
      icon = Icons.Filled.CloudDownload,
      accentColor = CyanHighlight,
      modifier = Modifier.weight(1f),
      onClick = { onNavigateTab(AppTab.DOWNLOADER) }
    )

    OverviewMiniCard(
      title = "Player",
      subtitle = "Lossless Audio",
      icon = Icons.Filled.GraphicEq,
      accentColor = VioletGaze,
      modifier = Modifier.weight(1f),
      onClick = onExpandPlayer
    )

    OverviewMiniCard(
      title = "Library",
      subtitle = "Offline Tracks",
      icon = Icons.Filled.LibraryMusic,
      accentColor = LavenderGlass,
      modifier = Modifier.weight(1f),
      onClick = { onNavigateTab(AppTab.LIBRARY) }
    )
  }
}

@Composable
private fun OverviewMiniCard(
  title: String,
  subtitle: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  accentColor: Color,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  GlassCard(
    modifier = modifier.height(84.dp),
    shape = RoundedCornerShape(18.dp),
    onClick = onClick
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(10.dp),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Icon(
        imageVector = icon,
        contentDescription = title,
        tint = accentColor,
        modifier = Modifier.size(20.dp)
      )
      Column {
        Text(
          text = title,
          color = TextPrimary,
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold
        )
        Text(
          text = subtitle,
          color = TextSecondary,
          fontSize = 10.sp,
          maxLines = 1
        )
      }
    }
  }
}

@Composable
private fun RecentlyPlayedSection(
  tracks: List<Track>,
  onPlayTrack: (Track) -> Unit,
  onSeeAll: () -> Unit
) {
  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Recently Played",
        color = TextPrimary,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "See all",
        color = CyanHighlight,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
          .clickable(onClick = onSeeAll)
          .padding(4.dp)
      )
    }

    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      items(tracks, key = { "recent_${it.id}" }) { track ->
        Box(
          modifier = Modifier
            .size(118.dp, 134.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(GlassSurfaceDark)
            .border(1.dp, GlassBorderLight, RoundedCornerShape(20.dp))
            .clickable { onPlayTrack(track) }
            .testTag("recent_track_${track.id}")
        ) {
          if (track.artworkUrl.isNotBlank()) {
            AsyncImage(
              model = track.artworkUrl,
              contentDescription = track.title,
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize()
            )
          } else {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .background(Brush.radialGradient(listOf(RoyalBlue, AtmosphereDeep))),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Filled.MusicNote,
                contentDescription = null,
                tint = CyanHighlight,
                modifier = Modifier.size(36.dp)
              )
            }
          }

          // Dark bottom gradient overlay
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(
                Brush.verticalGradient(
                  listOf(Color.Transparent, Color(0xDD07102B))
                )
              )
          )

          // Title & Artist
          Column(
            modifier = Modifier
              .align(Alignment.BottomStart)
              .padding(10.dp)
          ) {
            Text(
              text = track.title,
              color = TextPrimary,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = track.artist,
              color = TextSecondary,
              fontSize = 10.sp,
              maxLines = 1
            )
          }

          // Small circular glass play button
          Box(
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .padding(8.dp)
              .size(28.dp)
              .clip(CircleShape)
              .background(VioletGaze.copy(alpha = 0.85f))
              .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Filled.PlayArrow,
              contentDescription = "Play",
              tint = Color.White,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun TrackRowCard(
  track: Track,
  onPlay: () -> Unit,
  onToggleFavorite: () -> Unit
) {
  GlassCard(
    modifier = Modifier
      .fillMaxWidth()
      .height(68.dp)
      .testTag("track_row_${track.id}"),
    shape = RoundedCornerShape(18.dp),
    onClick = onPlay
  ) {
    Row(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (track.artworkUrl.isNotBlank()) {
        AsyncImage(
          model = track.artworkUrl,
          contentDescription = track.title,
          contentScale = ContentScale.Crop,
          modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, GlassBorderSubtle, RoundedCornerShape(12.dp))
        )
      } else {
        Box(
          modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(RoyalBlue.copy(alpha = 0.4f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Filled.MusicNote, contentDescription = null, tint = CyanHighlight)
        }
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = track.title,
          color = TextPrimary,
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Text(
          text = "${track.artist} • ${track.formattedDuration}",
          color = TextSecondary,
          fontSize = 12.sp,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      // Favorite toggle
      IconButton(
        onClick = onToggleFavorite,
        modifier = Modifier.size(36.dp)
      ) {
        Icon(
          imageVector = if (track.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
          contentDescription = "Favorite",
          tint = if (track.isFavorite) CoralAccent else TextSecondary,
          modifier = Modifier.size(20.dp)
        )
      }

      Spacer(modifier = Modifier.width(4.dp))

      // Circular play icon
      Box(
        modifier = Modifier
          .size(34.dp)
          .clip(CircleShape)
          .background(
            Brush.horizontalGradient(listOf(RoyalBlue, VioletGaze))
          )
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(color = Color.White),
            onClick = onPlay
          ),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Filled.PlayArrow,
          contentDescription = "Play",
          tint = Color.White,
          modifier = Modifier.size(18.dp)
        )
      }
    }
  }
}

@Composable
private fun EmptyHomeStateCard(onGoToDownloader: () -> Unit) {
  GlassCard(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Box(
        modifier = Modifier
          .size(54.dp)
          .clip(CircleShape)
          .background(GlassSurfaceDark)
          .border(1.dp, GlassBorderLight, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Filled.CloudDownload,
          contentDescription = null,
          tint = CyanHighlight,
          modifier = Modifier.size(28.dp)
        )
      }

      Text(
        text = "No Tracks Downloaded Yet",
        color = TextPrimary,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
      )

      Text(
        text = "Paste any media link in the Online Downloader to extract audio or video files directly to your NeonWave library.",
        color = TextSecondary,
        fontSize = 13.sp,
        textAlign = TextAlign.Center,
        lineHeight = 18.sp
      )

      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(14.dp))
          .background(
            Brush.horizontalGradient(listOf(RoyalBlue, VioletGaze))
          )
          .clickable(onClick = onGoToDownloader)
          .padding(horizontal = 20.dp, vertical = 10.dp)
          .testTag("empty_home_goto_downloader"),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "Go to Downloader",
          color = Color.White,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }
  }
}
