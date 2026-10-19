package com.example.player

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import com.example.data.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class RepeatMode {
  OFF, ALL, ONE
}

data class PlayerState(
  val currentTrack: Track? = null,
  val isPlaying: Boolean = false,
  val isBuffering: Boolean = false,
  val currentPositionMs: Long = 0L,
  val durationMs: Long = 0L,
  val queue: List<Track> = emptyList(),
  val currentIndex: Int = -1,
  val isShuffle: Boolean = false,
  val repeatMode: RepeatMode = RepeatMode.OFF,
  val playbackSpeed: Float = 1.0f,
  val audioSessionId: Int = 0,
  val errorMessage: String? = null
)

class PlaybackManager(private val context: Context) {
  private val scope = CoroutineScope(Dispatchers.Main + Job())
  private var mediaPlayer: MediaPlayer? = null
  private var tickerJob: Job? = null

  private val _state = MutableStateFlow(PlayerState())
  val state: StateFlow<PlayerState> = _state.asStateFlow()

  private fun getOrCreatePlayer(): MediaPlayer {
    return mediaPlayer ?: MediaPlayer().apply {
      setAudioAttributes(
        AudioAttributes.Builder()
          .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
          .setUsage(AudioAttributes.USAGE_MEDIA)
          .build()
      )
      setOnPreparedListener { mp ->
        _state.value = _state.value.copy(
          isBuffering = false,
          isPlaying = true,
          durationMs = mp.duration.toLong().coerceAtLeast(1000L),
          audioSessionId = mp.audioSessionId
        )
        mp.start()
        startTicker()
      }
      setOnCompletionListener {
        handleTrackCompletion()
      }
      setOnErrorListener { _, what, extra ->
        _state.value = _state.value.copy(
          isPlaying = false,
          isBuffering = false,
          errorMessage = "Playback error ($what, $extra). Please check your internet connection."
        )
        stopTicker()
        true
      }
      mediaPlayer = this
    }
  }

  fun playTrack(track: Track, newQueue: List<Track>? = null) {
    scope.launch {
      val updatedQueue = newQueue ?: _state.value.queue.ifEmpty { listOf(track) }
      val index = updatedQueue.indexOfFirst { it.id == track.id }.let { if (it >= 0) it else 0 }

      _state.value = _state.value.copy(
        currentTrack = track,
        queue = updatedQueue,
        currentIndex = index,
        isBuffering = true,
        errorMessage = null,
        currentPositionMs = 0L,
        durationMs = track.durationMs
      )

      withContext(Dispatchers.IO) {
        try {
          val player = getOrCreatePlayer()
          player.reset()

          val uri = if (track.localPath != null) {
            Uri.parse(track.localPath)
          } else {
            Uri.parse(track.streamUrl)
          }

          player.setDataSource(context, uri)
          player.prepareAsync()
        } catch (e: Exception) {
          _state.value = _state.value.copy(
            isBuffering = false,
            isPlaying = false,
            errorMessage = "Failed to load audio: ${e.localizedMessage ?: "Unknown error"}"
          )
        }
      }
    }
  }

  fun togglePlayPause() {
    val player = mediaPlayer ?: return
    if (_state.value.isPlaying) {
      player.pause()
      _state.value = _state.value.copy(isPlaying = false)
      stopTicker()
    } else {
      if (_state.value.currentTrack != null) {
        player.start()
        _state.value = _state.value.copy(isPlaying = true)
        startTicker()
      }
    }
  }

  fun seekTo(positionMs: Long) {
    val player = mediaPlayer ?: return
    val clamped = positionMs.coerceIn(0L, _state.value.durationMs)
    player.seekTo(clamped.toInt())
    _state.value = _state.value.copy(currentPositionMs = clamped)
  }

  fun next() {
    val currentQ = _state.value.queue
    if (currentQ.isEmpty()) return

    val nextIdx = if (_state.value.isShuffle) {
      (currentQ.indices).filter { it != _state.value.currentIndex }.randomOrNull() ?: 0
    } else {
      (_state.value.currentIndex + 1) % currentQ.size
    }

    playTrack(currentQ[nextIdx], currentQ)
  }

  fun previous() {
    val currentQ = _state.value.queue
    if (currentQ.isEmpty()) return

    // If more than 3 seconds in, seek to beginning
    if (_state.value.currentPositionMs > 3000L) {
      seekTo(0L)
      return
    }

    val prevIdx = if (_state.value.currentIndex - 1 < 0) {
      currentQ.size - 1
    } else {
      _state.value.currentIndex - 1
    }

    playTrack(currentQ[prevIdx], currentQ)
  }

  fun toggleShuffle() {
    _state.value = _state.value.copy(isShuffle = !_state.value.isShuffle)
  }

  fun toggleRepeat() {
    val nextMode = when (_state.value.repeatMode) {
      RepeatMode.OFF -> RepeatMode.ALL
      RepeatMode.ALL -> RepeatMode.ONE
      RepeatMode.ONE -> RepeatMode.OFF
    }
    _state.value = _state.value.copy(repeatMode = nextMode)
  }

  fun setPlaybackSpeed(speed: Float) {
    mediaPlayer?.let { mp ->
      try {
        mp.playbackParams = mp.playbackParams.setSpeed(speed)
        _state.value = _state.value.copy(playbackSpeed = speed)
      } catch (_: Exception) {}
    }
  }

  fun addToQueue(track: Track) {
    val currentQ = _state.value.queue.toMutableList()
    if (!currentQ.any { it.id == track.id }) {
      currentQ.add(track)
      _state.value = _state.value.copy(queue = currentQ)
    }
  }

  private fun handleTrackCompletion() {
    when (_state.value.repeatMode) {
      RepeatMode.ONE -> {
        seekTo(0L)
        mediaPlayer?.start()
        _state.value = _state.value.copy(isPlaying = true)
        startTicker()
      }
      RepeatMode.ALL -> {
        next()
      }
      RepeatMode.OFF -> {
        if (_state.value.currentIndex < _state.value.queue.size - 1) {
          next()
        } else {
          _state.value = _state.value.copy(isPlaying = false, currentPositionMs = 0L)
          stopTicker()
        }
      }
    }
  }

  private fun startTicker() {
    tickerJob?.cancel()
    tickerJob = scope.launch {
      while (isActive && _state.value.isPlaying) {
        mediaPlayer?.let { mp ->
          try {
            if (mp.isPlaying) {
              val pos = mp.currentPosition.toLong()
              val dur = mp.duration.toLong().coerceAtLeast(1000L)
              _state.value = _state.value.copy(
                currentPositionMs = pos,
                durationMs = dur
              )
            }
          } catch (_: Exception) {}
        }
        delay(250)
      }
    }
  }

  private fun stopTicker() {
    tickerJob?.cancel()
    tickerJob = null
  }

  fun release() {
    stopTicker()
    mediaPlayer?.release()
    mediaPlayer = null
  }
}
