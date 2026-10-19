package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.downloader.DownloadState
import com.example.data.downloader.ExtractedMediaInfo
import com.example.data.downloader.MediaDownloader
import com.example.data.downloader.MediaFormatOption
import com.example.data.model.Track
import com.example.data.repository.TrackRepository
import com.example.player.PlaybackManager
import com.example.player.PlayerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab {
  HOME, DOWNLOADER, LIBRARY
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
  val repository = TrackRepository(application)
  val playbackManager = PlaybackManager(application)
  val downloader = MediaDownloader(application, repository)

  val playerState: StateFlow<PlayerState> = playbackManager.state
  val downloadState: StateFlow<DownloadState> = downloader.state

  private val _activeTab = MutableStateFlow(AppTab.HOME)
  val activeTab: StateFlow<AppTab> = _activeTab.asStateFlow()

  private val _isPlayerExpanded = MutableStateFlow(false)
  val isPlayerExpanded: StateFlow<Boolean> = _isPlayerExpanded.asStateFlow()

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _selectedCategory = MutableStateFlow("All")
  val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

  val allTracks: StateFlow<List<Track>> = repository.getAllTracks()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val filteredTracks: StateFlow<List<Track>> = combine(
    allTracks,
    _selectedCategory,
    _searchQuery
  ) { tracks, category, query ->
    tracks.filter { track ->
      val matchesCategory = when (category) {
        "All" -> true
        "Favorites" -> track.isFavorite
        "Downloaded" -> track.isDownloaded
        else -> track.category.equals(category, ignoreCase = true)
      }
      val matchesQuery = if (query.isBlank()) true else {
        track.title.contains(query, ignoreCase = true) ||
          track.artist.contains(query, ignoreCase = true)
      }
      matchesCategory && matchesQuery
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val favoriteTracks: StateFlow<List<Track>> = repository.getFavoriteTracks()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val downloadedTracks: StateFlow<List<Track>> = repository.getDownloadedTracks()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  init {
    viewModelScope.launch {
      repository.clearDemoTracksIfAny()
    }
  }

  fun selectTab(tab: AppTab) {
    _activeTab.value = tab
  }

  fun setPlayerExpanded(expanded: Boolean) {
    _isPlayerExpanded.value = expanded
  }

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun selectCategory(category: String) {
    _selectedCategory.value = category
  }

  fun playTrack(track: Track, queue: List<Track>? = null) {
    playbackManager.playTrack(track, queue ?: allTracks.value)
  }

  fun togglePlayPause() {
    playbackManager.togglePlayPause()
  }

  fun seekTo(positionMs: Long) {
    playbackManager.seekTo(positionMs)
  }

  fun next() {
    playbackManager.next()
  }

  fun previous() {
    playbackManager.previous()
  }

  fun toggleShuffle() {
    playbackManager.toggleShuffle()
  }

  fun toggleRepeat() {
    playbackManager.toggleRepeat()
  }

  fun toggleFavorite(track: Track) {
    viewModelScope.launch {
      repository.toggleFavorite(track.id, track.isFavorite)
    }
  }

  fun analyzeUrl(url: String) {
    viewModelScope.launch {
      downloader.analyzeUrl(url)
    }
  }

  fun startDownload(info: ExtractedMediaInfo, format: MediaFormatOption) {
    viewModelScope.launch {
      downloader.startDownload(info, format)
    }
  }

  fun resetDownloader() {
    downloader.reset()
  }

  override fun onCleared() {
    super.onCleared()
    playbackManager.release()
  }
}
