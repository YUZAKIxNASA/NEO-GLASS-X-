package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.player.PlayerState
import com.example.ui.theme.CyanHighlight
import com.example.ui.theme.GlassBorderLight
import com.example.ui.theme.GlassSurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletGaze

@Composable
fun MiniPlayerBar(
  playerState: PlayerState,
  onTogglePlayPause: () -> Unit,
  onNext: () -> Unit,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val track = playerState.currentTrack ?: return

  Box(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp, vertical = 4.dp)
      .clip(RoundedCornerShape(20.dp))
      .background(
        brush = Brush.verticalGradient(
          listOf(
            Color(0xEE1E2B63),
            GlassSurfaceDark.copy(alpha = 0.95f)
          )
        )
      )
      .border(
        width = 1.dp,
        brush = Brush.verticalGradient(
          listOf(
            GlassBorderLight,
            CyanHighlight.copy(alpha = 0.25f)
          )
        ),
        shape = RoundedCornerShape(20.dp)
      )
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(color = VioletGaze),
        onClick = onClick
      )
      .testTag("mini_player_bar")
  ) {
    Column {
      // Progress indicator bar at top of mini player
      val progress = if (playerState.durationMs > 0) {
        (playerState.currentPositionMs.toFloat() / playerState.durationMs.toFloat()).coerceIn(0f, 1f)
      } else 0f

      LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier
          .fillMaxWidth()
          .height(2.5.dp),
        color = CyanHighlight,
        trackColor = Color(0x33B6A8FF),
      )

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Thumbnail
        AsyncImage(
          model = track.artworkUrl,
          contentDescription = "Track Artwork",
          contentScale = ContentScale.Crop,
          modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Color(0x33B6A8FF), RoundedCornerShape(12.dp))
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Title and artist
        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.Center
        ) {
          Text(
            text = track.title,
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
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
            if (track.format.isNotBlank()) {
              Text(
                text = " • ${track.format}",
                color = TextMuted,
                fontSize = 10.sp
              )
            }
          }
        }

        // Play/Pause Action
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(
              brush = Brush.horizontalGradient(
                listOf(VioletGaze, CyanHighlight)
              )
            )
            .clickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = ripple(color = Color.White),
              onClick = onTogglePlayPause
            )
            .testTag("mini_player_play_pause"),
          contentAlignment = Alignment.Center
        ) {
          if (playerState.isBuffering) {
            CircularProgressIndicator(
              modifier = Modifier.size(20.dp),
              color = Color.White,
              strokeWidth = 2.dp
            )
          } else {
            Icon(
              imageVector = if (playerState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
              contentDescription = if (playerState.isPlaying) "Pause" else "Play",
              tint = Color.White,
              modifier = Modifier.size(22.dp)
            )
          }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Next Action
        IconButton(
          onClick = onNext,
          modifier = Modifier
            .size(40.dp)
            .testTag("mini_player_next")
        ) {
          Icon(
            imageVector = Icons.Filled.SkipNext,
            contentDescription = "Next Track",
            tint = TextPrimary,
            modifier = Modifier.size(24.dp)
          )
        }
      }
    }
  }
}
