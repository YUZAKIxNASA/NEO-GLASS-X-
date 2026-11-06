package com.example.player

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

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
  private var isPrepared = false
  private var tickerJob: Job? = null

  private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
  private var focusRequest: AudioFocusRequest? = null
  private var isNoisyReceiverRegistered = false

  private val _state = MutableStateFlow(PlayerState())
  val state: StateFlow<PlayerState> = _state.asStateFlow()

  private val becomingNoisyReceiver = object : BroadcastReceiver() {
    override fun onReceive(c: Context?, intent: Intent?) {
      if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
        pause()
      }
    }
  }

  private fun registerNoisyReceiver() {
    if (!isNoisyReceiverRegistered) {
      try {
        val filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        context.registerReceiver(becomingNoisyReceiver, filter)
        isNoisyReceiverRegistered = true
      } catch (_: Exception) {}
    }
  }

  private fun unregisterNoisyReceiver() {
    if (isNoisyReceiverRegistered) {
      try {
        context.unregisterReceiver(becomingNoisyReceiver)
      } catch (_: Exception) {}
      isNoisyReceiverRegistered = false
    }
  }

  private fun getOrCreatePlayer(): MediaPlayer {
    mediaPlayer?.let { return it }

    val player = MediaPlayer().apply {
      setAudioAttributes(
        AudioAttributes.Builder()
          .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
          .setUsage(AudioAttributes.USAGE_MEDIA)
          .build()
      )
      setVolume(1.0f, 1.0f)
      setOnPreparedListener { mp ->
        isPrepared = true
        val actualDuration = mp.duration.toLong()
        _state.value = _state.value.copy(
          isBuffering = false,
          isPlaying = true,
          durationMs = if (actualDuration > 0) actualDuration else _state.value.currentTrack?.durationMs ?: 180000L,
          audioSessionId = mp.audioSessionId,
          errorMessage = null
        )
        if (requestAudioFocus()) {
          try {
            mp.start()
            registerNoisyReceiver()
            startTicker()
          } catch (e: Exception) {
            handlePlaybackError("Failed to start audio playback: ${e.localizedMessage ?: "Unknown error"}")
          }
        }
      }
      setOnCompletionListener {
        handleTrackCompletion()
      }
      setOnErrorListener { _, what, extra ->
        isPrepared = false
        val message = when (what) {
          MediaPlayer.MEDIA_ERROR_SERVER_DIED -> "Audio media server died. Please retry."
          MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK -> "Audio stream not valid for progressive playback."
          else -> "Audio playback error ($what, $extra). Please check the audio file."
        }
        handlePlaybackError(message)
        true
      }
    }
    mediaPlayer = player
    return player
  }

  private sealed class SourceValidation {
    data class LocalFile(val file: File) : SourceValidation()
    data class RemoteUri(val uri: Uri) : SourceValidation()
    data class Error(val message: String) : SourceValidation()
  }

  private fun validateAudioSource(track: Track): SourceValidation {
    // 1. Resolve local path
    val localFilePath = track.localPath ?: if (track.streamUrl.startsWith("file:")) {
      Uri.parse(track.streamUrl).path
    } else null

    if (localFilePath != null) {
      val file = File(localFilePath)
      if (!file.exists()) {
        return SourceValidation.Error("Local audio file not found: ${file.name}")
      }
      if (file.length() == 0L) {
        return SourceValidation.Error("Audio file is empty (0 bytes).")
      }
      if (!file.canRead()) {
        return SourceValidation.Error("Permission denied reading audio file: ${file.name}")
      }
      return SourceValidation.LocalFile(file)
    }

    // 2. Resolve remote URI
    val uri = try {
      Uri.parse(track.streamUrl)
    } catch (_: Exception) {
      return SourceValidation.Error("Invalid audio stream URL: ${track.streamUrl}")
    }

    val scheme = uri.scheme?.lowercase()
    if (scheme != "http" && scheme != "https") {
      return SourceValidation.Error("Unsupported stream scheme: '$scheme'. Only HTTP and HTTPS are supported.")
    }

    return SourceValidation.RemoteUri(uri)
  }

  fun playTrack(track: Track, newQueue: List<Track>? = null) {
    // 1. Build and synchronize queue immediately on caller thread
    val currentQueue = newQueue ?: _state.value.queue
    val updatedQueue = if (currentQueue.isEmpty()) {
      listOf(track)
    } else if (!currentQueue.any { it.id == track.id }) {
      listOf(track) + currentQueue
    } else {
      currentQueue
    }
    val index = updatedQueue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)

    _state.value = _state.value.copy(
      currentTrack = track,
      queue = updatedQueue,
      currentIndex = index,
      isBuffering = true,
      errorMessage = null,
      currentPositionMs = 0L,
      durationMs = track.durationMs
    )

    scope.launch {

      // 2. Validate audio source on IO dispatcher
      val source = withContext(Dispatchers.IO) {
        validateAudioSource(track)
      }

      if (source is SourceValidation.Error) {
        handlePlaybackError(source.message)
        return@launch
      }

      // 3. Configure and prepare MediaPlayer safely on Main thread
      try {
        val player = getOrCreatePlayer()
        isPrepared = false
        player.reset()
        player.setVolume(1.0f, 1.0f)

        when (source) {
          is SourceValidation.LocalFile -> {
            // Local file path directly managed by MediaPlayer's native engine
            player.setDataSource(source.file.absolutePath)
          }
          is SourceValidation.RemoteUri -> {
            player.setDataSource(context, source.uri)
          }
          else -> {}
        }

        player.prepareAsync()
      } catch (e: Exception) {
        handlePlaybackError("Failed to initialize audio source: ${e.localizedMessage ?: "Unknown file error"}")
      }
    }
  }

  fun togglePlayPause() {
    val player = mediaPlayer
    if (player == null || !isPrepared) {
      val track = _state.value.currentTrack
      if (track != null) {
        playTrack(track)
      }
      return
    }

    if (_state.value.isPlaying) {
      pause()
    } else {
      resume()
    }
  }

  fun pause() {
    val player = mediaPlayer ?: return
    if (_state.value.isPlaying) {
      try {
        player.pause()
        unregisterNoisyReceiver()
        _state.value = _state.value.copy(isPlaying = false)
        stopTicker()
      } catch (e: Exception) {
        handlePlaybackError("Pause error: ${e.localizedMessage}")
      }
    }
  }

  fun resume() {
    val player = mediaPlayer ?: return
    if (!isPrepared) {
      val track = _state.value.currentTrack
      if (track != null) playTrack(track)
      return
    }

    if (requestAudioFocus()) {
      try {
        player.start()
        registerNoisyReceiver()
        _state.value = _state.value.copy(isPlaying = true, errorMessage = null)
        startTicker()
      } catch (e: Exception) {
        handlePlaybackError("Resume error: ${e.localizedMessage}")
      }
    }
  }

  fun seekTo(positionMs: Long) {
    val clamped = positionMs.coerceIn(0L, _state.value.durationMs.coerceAtLeast(1000L))
    _state.value = _state.value.copy(currentPositionMs = clamped)
    if (isPrepared) {
      try {
        mediaPlayer?.seekTo(clamped.toInt())
      } catch (_: Exception) {}
    }
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
      if (isPrepared) {
        try {
          mp.playbackParams = mp.playbackParams.setSpeed(speed)
          _state.value = _state.value.copy(playbackSpeed = speed)
        } catch (_: Exception) {}
      }
    }
  }

  fun addToQueue(track: Track) {
    val currentQ = _state.value.queue.toMutableList()
    if (!currentQ.any { it.id == track.id }) {
      currentQ.add(track)
      _state.value = _state.value.copy(queue = currentQ)
    }
  }

  fun clearError() {
    _state.value = _state.value.copy(errorMessage = null)
  }

  private fun handlePlaybackError(message: String) {
    stopTicker()
    unregisterNoisyReceiver()
    abandonAudioFocus()
    _state.value = _state.value.copy(
      isPlaying = false,
      isBuffering = false,
      errorMessage = message
    )
    try {
      mediaPlayer?.reset()
    } catch (_: Exception) {}
    isPrepared = false
  }

  private fun handleTrackCompletion() {
    when (_state.value.repeatMode) {
      RepeatMode.ONE -> {
        seekTo(0L)
        mediaPlayer?.let { mp ->
          try {
            mp.start()
            _state.value = _state.value.copy(isPlaying = true)
            startTicker()
          } catch (_: Exception) {}
        }
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
          unregisterNoisyReceiver()
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
            if (isPrepared && mp.isPlaying) {
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
        pause()
      }
      AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
        pause()
      }
      AudioManager.AUDIOFOCUS_GAIN -> {
        mediaPlayer?.setVolume(1.0f, 1.0f)
      }
    }
  }

  fun release() {
    stopTicker()
    unregisterNoisyReceiver()
    abandonAudioFocus()
    try {
      mediaPlayer?.release()
    } catch (_: Exception) {}
    mediaPlayer = null
    isPrepared = false
    scope.cancel()
  }
}
