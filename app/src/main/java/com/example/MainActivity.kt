package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.MiniPlayerBar
import com.example.ui.components.NeoGlassBottomBar
import com.example.ui.screens.DownloaderScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.theme.AtmosphereDeep
import com.example.ui.theme.NeoGlassXTheme
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
  private val viewModel: MainViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      NeoGlassXTheme {
        NeoGlassXApp(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun NeoGlassXApp(viewModel: MainViewModel) {
  val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
  val isPlayerExpanded by viewModel.isPlayerExpanded.collectAsStateWithLifecycle()
  val playerState by viewModel.playerState.collectAsStateWithLifecycle()
  val downloadState by viewModel.downloadState.collectAsStateWithLifecycle()
  val allTracks by viewModel.allTracks.collectAsStateWithLifecycle()
  val filteredTracks by viewModel.filteredTracks.collectAsStateWithLifecycle()
  val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
  val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

  // Handle system back button smoothly
  BackHandler(enabled = isPlayerExpanded || activeTab != AppTab.HOME) {
    if (isPlayerExpanded) {
      viewModel.setPlayerExpanded(false)
    } else if (activeTab != AppTab.HOME) {
      viewModel.selectTab(AppTab.HOME)
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
  ) {
    if (isPlayerExpanded && playerState.currentTrack != null) {
      // Full Screen Immersive Player
      Box(
        modifier = Modifier
          .fillMaxSize()
          .statusBarsPadding()
          .navigationBarsPadding()
      ) {
        PlayerScreen(
          playerState = playerState,
          onCollapse = { viewModel.setPlayerExpanded(false) },
          onTogglePlayPause = { viewModel.togglePlayPause() },
          onSeekTo = { viewModel.seekTo(it) },
          onNext = { viewModel.next() },
          onPrevious = { viewModel.previous() },
          onToggleShuffle = { viewModel.toggleShuffle() },
          onToggleRepeat = { viewModel.toggleRepeat() },
          onToggleFavorite = { viewModel.toggleFavorite(it) },
          onPlayQueueTrack = { viewModel.playTrack(it) }
        )
      }
    } else {
      // Main App Scaffold with Tab Navigation and Docked Mini Player
      Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AtmosphereDeep,
        bottomBar = {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .navigationBarsPadding()
          ) {
            // Persistent Mini Player above Navigation Bar
            if (playerState.currentTrack != null) {
              MiniPlayerBar(
                playerState = playerState,
                onTogglePlayPause = { viewModel.togglePlayPause() },
                onNext = { viewModel.next() },
                onClick = { viewModel.setPlayerExpanded(true) }
              )
            }

            // Floating Navigation Bar
            NeoGlassBottomBar(
              currentTab = activeTab,
              onTabSelected = { viewModel.selectTab(it) }
            )
          }
        }
      ) { innerPadding ->
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .statusBarsPadding()
        ) {
          AnimatedContent(
            targetState = activeTab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "tab_animation"
          ) { targetTab ->
            when (targetTab) {
              AppTab.HOME -> {
                HomeScreen(
                  tracks = allTracks,
                  onPlayTrack = { viewModel.playTrack(it) },
                  onToggleFavorite = { viewModel.toggleFavorite(it) },
                  onNavigateTab = { viewModel.selectTab(it) },
                  onExpandPlayer = {
                    if (playerState.currentTrack == null && allTracks.isNotEmpty()) {
                      viewModel.playTrack(allTracks.first())
                    }
                    viewModel.setPlayerExpanded(true)
                  }
                )
              }

              AppTab.DOWNLOADER -> {
                DownloaderScreen(
                  downloadState = downloadState,
                  onAnalyzeUrl = { viewModel.analyzeUrl(it) },
                  onStartDownload = { info, format -> viewModel.startDownload(info, format) },
                  onPlayTrack = {
                    viewModel.playTrack(it)
                    viewModel.setPlayerExpanded(true)
                  },
                  onReset = { viewModel.resetDownloader() }
                )
              }

              AppTab.LIBRARY -> {
                LibraryScreen(
                  tracks = filteredTracks,
                  currentPlayingTrackId = playerState.currentTrack?.id,
                  isPlaying = playerState.isPlaying,
                  selectedCategory = selectedCategory,
                  searchQuery = searchQuery,
                  onSelectCategory = { viewModel.selectCategory(it) },
                  onSearchQueryChanged = { viewModel.setSearchQuery(it) },
                  onPlayTrack = { viewModel.playTrack(it) },
                  onToggleFavorite = { viewModel.toggleFavorite(it) },
                  onNavigateToDownloader = { viewModel.selectTab(AppTab.DOWNLOADER) }
                )
              }
            }
          }
        }
      }
    }
  }
}
