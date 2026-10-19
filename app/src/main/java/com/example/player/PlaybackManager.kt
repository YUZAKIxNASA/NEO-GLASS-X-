package com.example.player

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
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
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException

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

class PlaybackManager(private val context: Context) : AudioManager.OnAudioFocusChangeListener {
  private val scope = CoroutineScope(Dispatchers.Main + Job())
  private var mediaPlayer: MediaPlayer? = null
  private var tickerJob: Job? = null

  private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
  private var focusRequest: AudioFocusRequest? = null

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
      setVolume(1.0f, 1.0f)
      setOnPreparedListener { mp ->
        val actualDuration = mp.duration.toLong()
        _state.value = _state.value.copy(
          isBuffering = false,
          isPlaying = true,
          durationMs = if (actualDuration > 0) actualDuration else _state.value.currentTrack?.durationMs ?: 180000L,
          audioSessionId = mp.audioSessionId,
          errorMessage = null
        )
        if (requestAudioFocus()) {
          mp.start()
          startTicker()
        }
      }
      setOnCompletionListener {
        handleTrackCompletion()
      }
      setOnErrorListener { _, what, extra ->
        _state.value = _state.value.copy(
          isPlaying = false,
          isBuffering = false,
          errorMessage = "Playback error occurred (code $what, extra $extra). Please check the audio file."
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
          player.setVolume(1.0f, 1.0f)

          // Handle local file vs remote URL safely
          val localFilePath = track.localPath ?: if (track.streamUrl.startsWith("file:")) {
            Uri.parse(track.streamUrl).path
          } else null

          if (localFilePath != null) {
            val file = File(localFilePath)
            if (!file.exists() || file.length() == 0L) {
              throw FileNotFoundException("Local file not found at: ${file.name}")
            }
            // Using file descriptor is the most reliable way to play local files in Android MediaPlayer
            FileInputStream(file).use { fis ->
              player.setDataSource(fis.fd)
            }
          } else {
            // Remote HTTP/HTTPS audio stream
            player.setDataSource(context, Uri.parse(track.streamUrl))
          }

          player.prepareAsync()
        } catch (e: Exception) {
          _state.value = _state.value.copy(
            isBuffering = false,
            isPlaying = false,
            errorMessage = "Failed to load audio: ${e.localizedMessage ?: "File error"}"
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
        if (requestAudioFocus()) {
          player.start()
          _state.value = _state.value.copy(isPlaying = true)
          startTicker()
        }
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
      val candidates = currentQ.indices.filter { it != _state.value.currentIndex }
      candidates.randomOrNull() ?: 0
    } else {
      (_state.value.currentIndex + 1) % currentQ.size
    }

    playTrack(currentQ[nextIdx], currentQ)
  }

  fun previous() {
    val currentQ = _state.value.queue
    if (currentQ.isEmpty()) return

    // If more than 3 seconds in, restart track
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

  private fun requestAudioFocus(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
        .setAudioAttributes(
          AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        )
        .setOnAudioFocusChangeListener(this)
        .build()
      focusRequest = request
      audioManager.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    } else {
      @Suppress("DEPRECATION")
      audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }
  }

  private fun abandonAudioFocus() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
    } else {
      @Suppress("DEPRECATION")
      audioManager.abandonAudioFocus(this)
    }
  }

  override fun onAudioFocusChange(focusChange: Int) {
    when (focusChange) {
      AudioManager.AUDIOFOCUS_LOSS -> {
        mediaPlayer?.pause()
        _state.value = _state.value.copy(isPlaying = false)
        stopTicker()
      }
      AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
        mediaPlayer?.pause()
        _state.value = _state.value.copy(isPlaying = false)
        stopTicker()
      }
      AudioManager.AUDIOFOCUS_GAIN -> {
        mediaPlayer?.setVolume(1.0f, 1.0f)
      }
    }
  }

  fun release() {
    stopTicker()
    abandonAudioFocus()
    mediaPlayer?.release()
    mediaPlayer = null
  }
}
