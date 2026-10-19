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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
import com.example.ui.theme.LavenderGlass
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletGaze
import com.example.ui.theme.VisualizerBars
import com.example.ui.theme.auroraBackground

@Composable
fun LibraryScreen(
  tracks: List<Track>,
  currentPlayingTrackId: String?,
  isPlaying: Boolean,
  selectedCategory: String,
  searchQuery: String,
  onSelectCategory: (String) -> Unit,
  onSearchQueryChanged: (String) -> Unit,
  onPlayTrack: (Track) -> Unit,
  onToggleFavorite: (Track) -> Unit,
  onNavigateToDownloader: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  var isSearchOpen by remember { mutableStateOf(searchQuery.isNotBlank()) }

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
      verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
      // 1. Header with title and search toggle
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Explore & Library",
              color = TextPrimary,
              fontSize = 24.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Discover new music and offline tracks",
              color = TextSecondary,
              fontSize = 13.sp
            )
          }

          // Search button
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(GlassSurfaceDark)
              .border(1.dp, GlassBorderLight, CircleShape)
              .clickable { isSearchOpen = !isSearchOpen }
              .testTag("toggle_search_button"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Filled.Search,
              contentDescription = "Search",
              tint = if (isSearchOpen) CyanHighlight else TextPrimary,
              modifier = Modifier.size(22.dp)
            )
          }
        }
      }

      // 2. Search Bar (expandable)
      if (isSearchOpen) {
        item {
          OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            placeholder = { Text("Search songs, artists, genres...", color = TextMuted, fontSize = 13.sp) },
            leadingIcon = {
              Icon(Icons.Filled.Search, contentDescription = null, tint = CyanHighlight)
            },
            trailingIcon = {
              if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onSearchQueryChanged("") }) {
                  Icon(Icons.Filled.Clear, contentDescription = "Clear", tint = TextSecondary)
                }
              }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary,
              focusedBorderColor = CyanHighlight,
              unfocusedBorderColor = GlassBorderLight,
              focusedContainerColor = AtmosphereDeep.copy(alpha = 0.8f),
              unfocusedContainerColor = AtmosphereDeep.copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("library_search_input")
          )
        }
      }

      // 3. Category Pills (Explore reference style: icon + label)
      item {
        val categories = listOf(
          CategoryItem("All", Icons.Filled.LibraryMusic),
          CategoryItem("Downloaded", Icons.Filled.CloudDownload),
          CategoryItem("Favorites", Icons.Filled.Favorite),
          CategoryItem("Pop", Icons.Filled.MusicNote),
          CategoryItem("Lo-Fi", Icons.Filled.Headphones),
          CategoryItem("Rock", Icons.Filled.GraphicEq),
          CategoryItem("Classical", Icons.Filled.LibraryMusic)
        )

        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(categories) { cat ->
            val isSelected = selectedCategory.equals(cat.name, ignoreCase = true)
            CategoryPillCard(
              category = cat,
              isSelected = isSelected,
              onClick = { onSelectCategory(cat.name) }
            )
          }
        }
      }

      // 4. "Trending Now" Section (Track rows with animated equalizer)
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (selectedCategory == "All") "Trending Now" else selectedCategory,
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "${tracks.size} tracks",
            color = LavenderGlass,
            fontSize = 12.sp
          )
        }
      }

      // 5. Track items or empty state
      if (tracks.isEmpty()) {
        item {
          EmptyLibraryCard(
            category = selectedCategory,
            hasSearch = searchQuery.isNotBlank(),
            onNavigateToDownloader = onNavigateToDownloader
          )
        }
      } else {
        items(tracks, key = { it.id }) { track ->
          val isCurrentTrack = track.id == currentPlayingTrackId
          LibraryTrackRow(
            track = track,
            isCurrentPlaying = isCurrentTrack && isPlaying,
            onPlay = { onPlayTrack(track) },
            onToggleFavorite = { onToggleFavorite(track) }
          )
        }
      }
    }
  }
}

private data class CategoryItem(val name: String, val icon: ImageVector)

@Composable
private fun CategoryPillCard(
  category: CategoryItem,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(20.dp))
      .background(
        if (isSelected) {
          Brush.verticalGradient(listOf(RoyalBlue, VioletGaze))
        } else {
          Brush.verticalGradient(listOf(Color(0x3328377C), GlassSurfaceDark))
        }
      )
      .border(
        width = 1.dp,
        color = if (isSelected) CyanHighlight.copy(alpha = 0.6f) else GlassBorderSubtle,
        shape = RoundedCornerShape(20.dp)
      )
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(color = CyanHighlight),
        onClick = onClick
      )
      .padding(horizontal = 16.dp, vertical = 12.dp)
      .testTag("category_pill_${category.name}"),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Icon(
        imageVector = category.icon,
        contentDescription = category.name,
        tint = if (isSelected) Color.White else LavenderGlass,
        modifier = Modifier.size(20.dp)
      )
      Text(
        text = category.name,
        color = if (isSelected) Color.White else TextSecondary,
        fontSize = 12.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
      )
    }
  }
}

@Composable
private fun LibraryTrackRow(
  track: Track,
  isCurrentPlaying: Boolean,
  onPlay: () -> Unit,
  onToggleFavorite: () -> Unit
) {
  GlassCard(
    modifier = Modifier
      .fillMaxWidth()
      .height(72.dp)
      .testTag("library_track_${track.id}"),
    shape = RoundedCornerShape(20.dp),
    onClick = onPlay
  ) {
    Row(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Thumbnail
      AsyncImage(
        model = track.artworkUrl,
        contentDescription = track.title,
        contentScale = ContentScale.Crop,
        modifier = Modifier
          .size(48.dp)
          .clip(RoundedCornerShape(14.dp))
          .border(1.dp, GlassBorderSubtle, RoundedCornerShape(14.dp))
      )

      Spacer(modifier = Modifier.width(14.dp))

      // Title, artist, format
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = track.title,
          color = if (isCurrentPlaying) CyanHighlight else TextPrimary,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = track.artist,
            color = TextSecondary,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            text = " • ${track.formattedDuration}",
            color = TextMuted,
            fontSize = 11.sp
          )
          if (track.isDownloaded) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
              modifier = Modifier
                .clip(CircleShape)
                .background(RoyalBlue.copy(alpha = 0.5f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(text = "SAVED", color = CyanHighlight, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      // Animated equalizer if playing
      if (isCurrentPlaying) {
        VisualizerBars(
          isPlaying = true,
          barCount = 4,
          maxHeight = 18.dp,
          modifier = Modifier.padding(horizontal = 8.dp)
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
          tint = if (track.isFavorite) CoralAccent else TextMuted,
          modifier = Modifier.size(20.dp)
        )
      }

      // Circular play button
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(
            if (isCurrentPlaying) CyanHighlight else GlassSurfaceDark
          )
          .border(1.dp, GlassBorderLight, CircleShape)
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
          tint = if (isCurrentPlaying) AtmosphereDeep else Color.White,
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}

@Composable
private fun EmptyLibraryCard(
  category: String,
  hasSearch: Boolean,
  onNavigateToDownloader: () -> Unit
) {
  GlassCard(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      Box(
        modifier = Modifier
          .size(56.dp)
          .clip(CircleShape)
          .background(GlassSurfaceDark)
          .border(1.dp, GlassBorderLight, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Filled.MusicNote,
          contentDescription = null,
          tint = CyanHighlight,
          modifier = Modifier.size(30.dp)
        )
      }
      Text(
        text = if (hasSearch) "No tracks match your search" else "No $category tracks yet",
        color = TextPrimary,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = if (hasSearch) "Try searching for a different track title or artist."
        else "Your offline library is clean and ready. Extract and download any audio or video in the Downloader tab.",
        color = TextSecondary,
        fontSize = 13.sp,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        lineHeight = 18.sp
      )

      if (!hasSearch) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
              Brush.horizontalGradient(listOf(RoyalBlue, VioletGaze))
            )
            .clickable(onClick = onNavigateToDownloader)
            .padding(horizontal = 22.dp, vertical = 10.dp)
            .testTag("empty_library_goto_downloader"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Open Downloader",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}
