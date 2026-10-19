package com.example.ui.screens

import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.downloader.DownloadState
import com.example.data.downloader.ExtractedMediaInfo
import com.example.data.downloader.MediaFormatOption
import com.example.data.model.Track
import com.example.ui.theme.AtmosphereDeep
import com.example.ui.theme.CoralAccent
import com.example.ui.theme.CyanHighlight
import com.example.ui.theme.GlassBorderCyan
import com.example.ui.theme.GlassBorderLight
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassCard
import com.example.ui.theme.GlassSurfaceDark
import com.example.ui.theme.GlassSurfaceMedium
import com.example.ui.theme.IndigoAura
import com.example.ui.theme.LavenderGlass
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletGaze
import com.example.ui.theme.auroraBackground

@Composable
fun DownloaderScreen(
  downloadState: DownloadState,
  onAnalyzeUrl: (String) -> Unit,
  onStartDownload: (ExtractedMediaInfo, MediaFormatOption) -> Unit,
  onPlayTrack: (Track) -> Unit,
  onReset: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val keyboardController = LocalSoftwareKeyboardController.current
  var urlInput by remember { mutableStateOf("") }
  var selectedFormatIndex by remember { mutableStateOf(0) }

  Box(
    modifier = modifier
      .fillMaxSize()
      .auroraBackground()
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp),
      contentPadding = PaddingValues(top = 20.dp, bottom = 100.dp),
      verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
      // Header
      item {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Filled.CloudDownload,
              contentDescription = null,
              tint = CyanHighlight,
              modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Online Downloader",
              color = TextPrimary,
              fontSize = 24.sp,
              fontWeight = FontWeight.Bold
            )
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Paste any media link to analyze and extract high-speed audio or video directly to your library.",
            color = TextSecondary,
            fontSize = 13.sp,
            lineHeight = 18.sp
          )
        }
      }

      // URL Input Panel
      item {
        GlassCard(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(24.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            Text(
              text = "Media URL",
              color = TextPrimary,
              fontSize = 14.sp,
              fontWeight = FontWeight.SemiBold
            )

            // Text Field
            OutlinedTextField(
              value = urlInput,
              onValueChange = { urlInput = it },
              placeholder = {
                Text(
                  text = "https://example.com/audio.mp3...",
                  color = TextMuted,
                  fontSize = 13.sp
                )
              },
              leadingIcon = {
                Icon(
                  imageVector = Icons.Filled.Link,
                  contentDescription = null,
                  tint = CyanHighlight
                )
              },
              trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  if (urlInput.isNotEmpty()) {
                    IconButton(onClick = { urlInput = "" }) {
                      Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "Clear",
                        tint = TextSecondary
                      )
                    }
                  }
                  // Paste action
                  IconButton(
                    onClick = {
                      val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                      val clip = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()
                      if (!clip.isNullOrBlank()) {
                        urlInput = clip.trim()
                      }
                    }
                  ) {
                    Icon(
                      imageVector = Icons.Filled.ContentPaste,
                      contentDescription = "Paste",
                      tint = LavenderGlass
                    )
                  }
                }
              },
              singleLine = true,
              keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
              keyboardActions = KeyboardActions(onDone = {
                keyboardController?.hide()
                if (urlInput.isNotBlank()) onAnalyzeUrl(urlInput)
              }),
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = CyanHighlight,
                unfocusedBorderColor = GlassBorderLight,
                focusedContainerColor = AtmosphereDeep.copy(alpha = 0.8f),
                unfocusedContainerColor = AtmosphereDeep.copy(alpha = 0.6f)
              ),
              shape = RoundedCornerShape(16.dp),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("url_input_field")
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Analyze Action Button
            val isAnalyzing = downloadState is DownloadState.Analyzing
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                  brush = Brush.horizontalGradient(
                    listOf(RoyalBlue, VioletGaze)
                  )
                )
                .border(1.dp, CyanHighlight.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .clickable(
                  enabled = !isAnalyzing && urlInput.isNotBlank(),
                  interactionSource = remember { MutableInteractionSource() },
                  indication = ripple(color = Color.White),
                  onClick = {
                    keyboardController?.hide()
                    onAnalyzeUrl(urlInput)
                  }
                )
                .testTag("analyze_link_button"),
              contentAlignment = Alignment.Center
            ) {
              if (isAnalyzing) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                  )
                  Text(
                    text = "Analyzing media link...",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                  )
                }
              } else {
                Text(
                  text = "Analyze Link",
                  color = Color.White,
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }
      }

      // States rendering: Ready, Downloading, Completed, Error
      when (downloadState) {
        is DownloadState.Ready -> {
          item {
            MediaExtractedCard(
              info = downloadState.info,
              selectedFormatIndex = selectedFormatIndex,
              onSelectFormat = { selectedFormatIndex = it },
              onDownload = {
                val format = downloadState.info.availableFormats.getOrNull(selectedFormatIndex)
                  ?: downloadState.info.availableFormats.first()
                onStartDownload(downloadState.info, format)
              }
            )
          }
        }

        is DownloadState.Downloading -> {
          item {
            DownloadProgressCard(state = downloadState)
          }
        }

        is DownloadState.Completed -> {
          item {
            DownloadCompletedCard(
              track = downloadState.track,
              onPlay = { onPlayTrack(downloadState.track) },
              onReset = onReset
            )
          }
        }

        is DownloadState.Error -> {
          item {
            DownloadErrorCard(
              message = downloadState.message,
              onRetry = {
                if (urlInput.isNotBlank()) onAnalyzeUrl(urlInput) else onReset()
              }
            )
          }
        }

        else -> {}
      }
    }
  }
}


@Composable
private fun MediaExtractedCard(
  info: ExtractedMediaInfo,
  selectedFormatIndex: Int,
  onSelectFormat: (Int) -> Unit,
  onDownload: () -> Unit
) {
  GlassCard(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      Text(
        text = "Media Resolved",
        color = CyanHighlight,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        AsyncImage(
          model = info.artworkUrl,
          contentDescription = info.title,
          contentScale = ContentScale.Crop,
          modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, GlassBorderLight, RoundedCornerShape(16.dp))
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = info.title,
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            text = "${info.artist} • ${(info.durationMs / 60000)}m",
            color = TextSecondary,
            fontSize = 12.sp
          )
        }
      }

      Text(
        text = "Select Extraction Format:",
        color = TextPrimary,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold
      )

      // Formats grid
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        info.availableFormats.forEachIndexed { idx, format ->
          val isSelected = idx == selectedFormatIndex
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .background(if (isSelected) RoyalBlue.copy(alpha = 0.5f) else GlassSurfaceDark)
              .border(
                1.dp,
                if (isSelected) CyanHighlight else GlassBorderSubtle,
                RoundedCornerShape(14.dp)
              )
              .clickable { onSelectFormat(idx) }
              .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = if (format.isVideo) Icons.Filled.Videocam else Icons.Filled.MusicNote,
                contentDescription = null,
                tint = if (isSelected) CyanHighlight else LavenderGlass,
                modifier = Modifier.size(18.dp)
              )
              Text(
                text = "${format.label} • ${format.quality}",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            }
            Text(
              text = format.sizeDisplay,
              color = TextSecondary,
              fontSize = 12.sp
            )
          }
        }
      }

      // Download Button
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(
            brush = Brush.horizontalGradient(
              listOf(VioletGaze, CyanHighlight)
            )
          )
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(color = Color.White),
            onClick = onDownload
          )
          .testTag("start_download_button"),
        contentAlignment = Alignment.Center
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Filled.CloudDownload,
            contentDescription = null,
            tint = AtmosphereDeep,
            modifier = Modifier.size(20.dp)
          )
          Text(
            text = "Download to Library",
            color = AtmosphereDeep,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}

@Composable
private fun DownloadProgressCard(state: DownloadState.Downloading) {
  GlassCard(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Downloading...",
          color = CyanHighlight,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "${(state.progress * 100).toInt()}%",
          color = TextPrimary,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold
        )
      }

      Text(
        text = state.title,
        color = TextPrimary,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )

      LinearProgressIndicator(
        progress = { state.progress },
        modifier = Modifier
          .fillMaxWidth()
          .height(8.dp)
          .clip(CircleShape),
        color = CyanHighlight,
        trackColor = GlassSurfaceMedium
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        val downloadedMB = state.downloadedBytes / (1024.0 * 1024.0)
        val totalMB = state.totalBytes / (1024.0 * 1024.0)
        Text(
          text = "%.1f MB / %.1f MB".format(downloadedMB, totalMB),
          color = TextSecondary,
          fontSize = 12.sp
        )
        Text(
          text = "Speed: %.1f MB/s".format(state.speedMBs),
          color = LavenderGlass,
          fontSize = 12.sp
        )
      }
    }
  }
}

@Composable
private fun DownloadCompletedCard(
  track: Track,
  onPlay: () -> Unit,
  onReset: () -> Unit
) {
  GlassCard(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      Icon(
        imageVector = Icons.Filled.CheckCircle,
        contentDescription = "Success",
        tint = CyanHighlight,
        modifier = Modifier.size(48.dp)
      )

      Text(
        text = "Download Complete!",
        color = TextPrimary,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
      )

      Text(
        text = "\"${track.title}\" has been saved and indexed in your NeonWave offline Library.",
        color = TextSecondary,
        fontSize = 13.sp,
        lineHeight = 18.sp
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Play Now CTA
        Box(
          modifier = Modifier
            .weight(1f)
            .height(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
              brush = Brush.horizontalGradient(listOf(RoyalBlue, VioletGaze))
            )
            .clickable(onClick = onPlay)
            .testTag("completed_play_button"),
          contentAlignment = Alignment.Center
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(
              imageVector = Icons.Filled.PlayArrow,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(18.dp)
            )
            Text(
              text = "Play Now",
              color = Color.White,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        // Download Another
        Box(
          modifier = Modifier
            .weight(1f)
            .height(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(GlassSurfaceDark)
            .border(1.dp, GlassBorderLight, RoundedCornerShape(14.dp))
            .clickable(onClick = onReset),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Download Another",
            color = TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
          )
        }
      }
    }
  }
}

@Composable
private fun DownloadErrorCard(
  message: String,
  onRetry: () -> Unit
) {
  GlassCard(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Icon(
        imageVector = Icons.Filled.ErrorOutline,
        contentDescription = "Error",
        tint = CoralAccent,
        modifier = Modifier.size(42.dp)
      )

      Text(
        text = "Download Notice",
        color = TextPrimary,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
      )

      Text(
        text = message,
        color = TextSecondary,
        fontSize = 13.sp,
        lineHeight = 18.sp
      )

      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(14.dp))
          .background(GlassSurfaceDark)
          .border(1.dp, GlassBorderLight, RoundedCornerShape(14.dp))
          .clickable(onClick = onRetry)
          .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "Try Again",
          color = CyanHighlight,
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold
        )
      }
    }
  }
}
