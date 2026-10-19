package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Track
import com.example.player.PlayerState
import com.example.player.RepeatMode
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
import com.example.ui.theme.VisualizerBars
import com.example.ui.theme.auroraBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
  playerState: PlayerState,
  onCollapse: () -> Unit,
  onTogglePlayPause: () -> Unit,
  onSeekTo: (Long) -> Unit,
  onNext: () -> Unit,
  onPrevious: () -> Unit,
  onToggleShuffle: () -> Unit,
  onToggleRepeat: () -> Unit,
  onToggleFavorite: (Track) -> Unit,
  onPlayQueueTrack: (Track) -> Unit,
  modifier: Modifier = Modifier
) {
  val track = playerState.currentTrack ?: return
  var isQueueSheetVisible by remember { mutableStateOf(false) }
  var isDraggingSlider by remember { mutableStateOf(false) }
  var draggedPositionFraction by remember { mutableFloatStateOf(0f) }

  Box(
    modifier = modifier
      .fillMaxSize()
      .auroraBackground()
      .testTag("full_player_screen")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 24.dp, vertical = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // 1. Top Bar: Down chevron, "Now Playing", 3-dots
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onCollapse,
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(GlassSurfaceDark)
            .border(1.dp, GlassBorderLight, CircleShape)
            .testTag("player_collapse_button")
        ) {
          Icon(
            imageVector = Icons.Filled.KeyboardArrowDown,
            contentDescription = "Collapse Player",
            tint = TextPrimary,
            modifier = Modifier.size(26.dp)
          )
        }

        Text(
          text = "Now Playing",
          color = TextPrimary,
          fontSize = 16.sp,
          fontWeight = FontWeight.SemiBold
        )

        IconButton(
          onClick = { isQueueSheetVisible = true },
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(GlassSurfaceDark)
            .border(1.dp, GlassBorderLight, CircleShape)
        ) {
          Icon(
            imageVector = Icons.Filled.MoreVert,
            contentDescription = "Options",
            tint = TextPrimary,
            modifier = Modifier.size(22.dp)
          )
        }
      }

      // 2. Artwork with flanking visualizer bars
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Left equalizer
        VisualizerBars(
          isPlaying = playerState.isPlaying,
          barCount = 4,
          maxHeight = 60.dp,
          minHeight = 8.dp,
          color = CyanHighlight,
          modifier = Modifier.padding(end = 12.dp)
        )

        // Center Artwork Card
        Box(
          modifier = Modifier
            .size(260.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(GlassSurfaceDark)
            .border(1.5.dp, LavenderGlass.copy(alpha = 0.4f), RoundedCornerShape(32.dp))
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
                modifier = Modifier.size(72.dp)
              )
            }
          }

          // Floating Favorite Heart in Artwork Corner
          Box(
            modifier = Modifier
              .align(Alignment.TopEnd)
              .padding(14.dp)
              .size(36.dp)
              .clip(CircleShape)
              .background(AtmosphereDeep.copy(alpha = 0.65f))
              .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
              .clickable { onToggleFavorite(track) }
              .testTag("player_artwork_favorite"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (track.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
              contentDescription = "Favorite",
              tint = if (track.isFavorite) CoralAccent else Color.White,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        // Right equalizer
        VisualizerBars(
          isPlaying = playerState.isPlaying,
          barCount = 4,
          maxHeight = 60.dp,
          minHeight = 8.dp,
          color = VioletGaze,
          modifier = Modifier.padding(start = 12.dp)
        )
      }

      // 3. Track Title, Artist, HD Badge
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Text(
            text = track.title,
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Spacer(modifier = Modifier.width(8.dp))
          // HD Badge
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(RoyalBlue.copy(alpha = 0.6f))
              .border(1.dp, CyanHighlight.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = if (track.format.isNotBlank()) track.format else "HD",
              color = CyanHighlight,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = track.artist,
          color = TextSecondary,
          fontSize = 15.sp,
          fontWeight = FontWeight.Normal
        )
      }

      // 4. Progress Scrubber with Timestamps
      Column(modifier = Modifier.fillMaxWidth()) {
        val totalDuration = playerState.durationMs.coerceAtLeast(1000L)
        val currentPosition = if (isDraggingSlider) {
          (draggedPositionFraction * totalDuration).toLong()
        } else {
          playerState.currentPositionMs
        }
        val sliderValue = (currentPosition.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f)

        Slider(
          value = sliderValue,
          onValueChange = { fraction ->
            isDraggingSlider = true
            draggedPositionFraction = fraction
          },
          onValueChangeFinished = {
            isDraggingSlider = false
            onSeekTo((draggedPositionFraction * totalDuration).toLong())
          },
          colors = SliderDefaults.colors(
            thumbColor = Color.White,
            activeTrackColor = CyanHighlight,
            inactiveTrackColor = Color(0x33B6A8FF)
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("player_progress_slider")
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = formatTime(currentPosition),
            color = TextSecondary,
            fontSize = 12.sp
          )
          Text(
            text = formatTime(totalDuration),
            color = TextSecondary,
            fontSize = 12.sp
          )
        }
      }

      // 5. Playback Controls Row: Shuffle, Previous, Play/Pause, Next, Repeat
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Shuffle
        IconButton(
          onClick = onToggleShuffle,
          modifier = Modifier.size(48.dp)
        ) {
          Icon(
            imageVector = Icons.Filled.Shuffle,
            contentDescription = "Shuffle",
            tint = if (playerState.isShuffle) CyanHighlight else TextMuted,
            modifier = Modifier.size(24.dp)
          )
        }

        // Previous
        IconButton(
          onClick = onPrevious,
          modifier = Modifier.size(48.dp)
        ) {
          Icon(
            imageVector = Icons.Filled.SkipPrevious,
            contentDescription = "Previous Track",
            tint = TextPrimary,
            modifier = Modifier.size(32.dp)
          )
        }

        // Large glowing central Play/Pause circle (72dp)
        Box(
          modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(
              brush = Brush.radialGradient(
                listOf(CyanHighlight, VioletGaze, RoyalBlue)
              )
            )
            .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape)
            .clickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = ripple(color = Color.White),
              onClick = onTogglePlayPause
            )
            .testTag("player_play_pause_button"),
          contentAlignment = Alignment.Center
        ) {
          if (playerState.isBuffering) {
            CircularProgressIndicator(
              modifier = Modifier.size(32.dp),
              color = Color.White,
              strokeWidth = 3.dp
            )
          } else {
            Icon(
              imageVector = if (playerState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
              contentDescription = if (playerState.isPlaying) "Pause" else "Play",
              tint = Color.White,
              modifier = Modifier.size(38.dp)
            )
          }
        }

        // Next
        IconButton(
          onClick = onNext,
          modifier = Modifier.size(48.dp)
        ) {
          Icon(
            imageVector = Icons.Filled.SkipNext,
            contentDescription = "Next Track",
            tint = TextPrimary,
            modifier = Modifier.size(32.dp)
          )
        }

        // Repeat
        IconButton(
          onClick = onToggleRepeat,
          modifier = Modifier.size(48.dp)
        ) {
          Icon(
            imageVector = if (playerState.repeatMode == RepeatMode.ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
            contentDescription = "Repeat Mode",
            tint = if (playerState.repeatMode != RepeatMode.OFF) CyanHighlight else TextMuted,
            modifier = Modifier.size(24.dp)
          )
        }
      }

      // 6. Utility Action Row: Share, Heart, Download, Queue
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(onClick = {}) {
          Icon(Icons.Filled.Share, contentDescription = "Share", tint = TextSecondary, modifier = Modifier.size(22.dp))
        }

        IconButton(onClick = { onToggleFavorite(track) }) {
          Icon(
            imageVector = if (track.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = "Favorite",
            tint = if (track.isFavorite) CoralAccent else TextSecondary,
            modifier = Modifier.size(22.dp)
          )
        }

        IconButton(onClick = {}) {
          Icon(
            imageVector = Icons.Filled.CloudDownload,
            contentDescription = "Downloaded Status",
            tint = if (track.isDownloaded) CyanHighlight else TextSecondary,
            modifier = Modifier.size(22.dp)
          )
        }

        IconButton(onClick = { isQueueSheetVisible = true }) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.QueueMusic,
            contentDescription = "Queue",
            tint = CyanHighlight,
            modifier = Modifier.size(22.dp)
          )
        }
      }

      // 7. "Up Next" Peek Card
      val nextTrack = playerState.queue.getOrNull(
        (playerState.currentIndex + 1) % playerState.queue.size.coerceAtLeast(1)
      )

      if (nextTrack != null && nextTrack.id != track.id) {
        GlassCard(
          modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable { onNext() },
          shape = RoundedCornerShape(20.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxSize()
              .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            AsyncImage(
              model = nextTrack.artworkUrl,
              contentDescription = nextTrack.title,
              contentScale = ContentScale.Crop,
              modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Up Next: ${nextTrack.title}",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                text = nextTrack.artist,
                color = TextSecondary,
                fontSize = 11.sp
              )
            }
            VisualizerBars(
              isPlaying = playerState.isPlaying,
              barCount = 3,
              maxHeight = 16.dp,
              color = CoralAccent
            )
          }
        }
      }
    }

    // Modal Queue Sheet
    if (isQueueSheetVisible) {
      ModalBottomSheet(
        onDismissRequest = { isQueueSheetVisible = false },
        sheetState = rememberModalBottomSheetState(),
        containerColor = AtmosphereDeep,
        scrimColor = Color.Black.copy(alpha = 0.65f)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
          Text(
            text = "Playing Queue (${playerState.queue.size})",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(12.dp))
          LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
          ) {
            itemsIndexed(playerState.queue) { idx, qTrack ->
              val isCurrent = idx == playerState.currentIndex
              GlassCard(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(58.dp),
                shape = RoundedCornerShape(16.dp),
                onClick = {
                  onPlayQueueTrack(qTrack)
                  isQueueSheetVisible = false
                }
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  AsyncImage(
                    model = qTrack.artworkUrl,
                    contentDescription = qTrack.title,
                    modifier = Modifier
                      .size(38.dp)
                      .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = qTrack.title,
                      color = if (isCurrent) CyanHighlight else TextPrimary,
                      fontSize = 13.sp,
                      fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                    )
                    Text(
                      text = qTrack.artist,
                      color = TextSecondary,
                      fontSize = 11.sp
                    )
                  }
                  if (isCurrent) {
                    VisualizerBars(isPlaying = playerState.isPlaying, barCount = 3)
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

private fun formatTime(millis: Long): String {
  val totalSeconds = (millis / 1000).coerceAtLeast(0)
  val minutes = totalSeconds / 60
  val seconds = totalSeconds % 60
  return "%d:%02d".format(minutes, seconds)
}
