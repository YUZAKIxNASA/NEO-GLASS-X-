package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Track
import com.example.player.PlaybackManager
import com.example.player.RepeatMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NeoGlassXPlaybackTest {

  private lateinit var context: Context
  private lateinit var playbackManager: PlaybackManager
  private lateinit var validMp3File: File
  private lateinit var validDownloadedMp3File: File

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    playbackManager = PlaybackManager(context)

    // Create a real minimal valid MP3 file on disk for local playback tests
    val musicDir = context.getExternalFilesDir("Music") ?: context.filesDir
    musicDir.mkdirs()

    validMp3File = File(musicDir, "test_track.mp3")
    val validBytes = createDummyValidMp3Bytes()
    FileOutputStream(validMp3File).use { it.write(validBytes) }

    validDownloadedMp3File = File(musicDir, "downloaded_track.mp3")
    FileOutputStream(validDownloadedMp3File).use { it.write(validBytes) }
  }

  @After
  fun tearDown() {
    playbackManager.release()
    ShadowLooper.idleMainLooper()
    if (validMp3File.exists()) validMp3File.delete()
    if (validDownloadedMp3File.exists()) validDownloadedMp3File.delete()
  }

  private fun createDummyValidMp3Bytes(): ByteArray {
    val bytes = ByteArray(512)
    // ID3v2 header
    bytes[0] = 'I'.code.toByte()
    bytes[1] = 'D'.code.toByte()
    bytes[2] = '3'.code.toByte()
    bytes[3] = 0x03.toByte()
    bytes[4] = 0x00.toByte()
    // MPEG frame sync word
    bytes[10] = 0xFF.toByte()
    bytes[11] = 0xFB.toByte()
    return bytes
  }

  @Test
  fun testPlayValidLocalMp3() = runBlocking {
    val track = Track(
      id = "local_1",
      title = "Midnight Horizon",
      artist = "Aurora Synth",
      album = "NEO GLASS X",
      durationMs = 210000L,
      streamUrl = validMp3File.toURI().toString(),
      localPath = validMp3File.absolutePath,
      artworkUrl = "",
      isDownloaded = true
    )

    playbackManager.playTrack(track)
    ShadowLooper.idleMainLooper()

    val state = playbackManager.state.value
    assertEquals(track.id, state.currentTrack?.id)
    assertEquals("Midnight Horizon", state.currentTrack?.title)
    assertNull("No error expected on valid local file", state.errorMessage)
  }

  @Test
  fun testPauseAndResume() = runBlocking {
    val track = Track(
      id = "local_2",
      title = "Cyber Pulse",
      artist = "Neon Driver",
      album = "NEO GLASS X",
      durationMs = 185000L,
      streamUrl = validMp3File.toURI().toString(),
      localPath = validMp3File.absolutePath,
      artworkUrl = "",
      isDownloaded = true
    )

    playbackManager.playTrack(track)
    ShadowLooper.idleMainLooper()

    // Test Pause
    playbackManager.pause()
    assertFalse(playbackManager.state.value.isPlaying)

    // Test Resume
    playbackManager.resume()
    ShadowLooper.idleMainLooper()
    assertNull(playbackManager.state.value.errorMessage)
  }

  @Test
  fun testSeekWithinTrack() = runBlocking {
    val track = Track(
      id = "local_3",
      title = "Glass Reverie",
      artist = "Violet Gaze",
      album = "NEO GLASS X",
      durationMs = 240000L,
      streamUrl = validMp3File.toURI().toString(),
      localPath = validMp3File.absolutePath,
      artworkUrl = "",
      isDownloaded = true
    )

    playbackManager.playTrack(track)
    ShadowLooper.idleMainLooper()

    playbackManager.seekTo(45000L)
    val state = playbackManager.state.value
    assertEquals(45000L, state.currentPositionMs)
  }

  @Test
  fun testNextAndPreviousQueueNavigation() = runBlocking {
    val track1 = Track(
      id = "q_1",
      title = "Track One",
      artist = "Artist A",
      durationMs = 120000L,
      streamUrl = validMp3File.toURI().toString(),
      localPath = validMp3File.absolutePath,
      artworkUrl = ""
    )
    val track2 = Track(
      id = "q_2",
      title = "Track Two",
      artist = "Artist B",
      durationMs = 150000L,
      streamUrl = validMp3File.toURI().toString(),
      localPath = validMp3File.absolutePath,
      artworkUrl = ""
    )
    val queue = listOf(track1, track2)

    playbackManager.playTrack(track1, queue)
    ShadowLooper.idleMainLooper()
    assertEquals("Track One", playbackManager.state.value.currentTrack?.title)

    // Next
    playbackManager.next()
    ShadowLooper.idleMainLooper()
    assertEquals("Track Two", playbackManager.state.value.currentTrack?.title)

    // Previous
    playbackManager.previous()
    ShadowLooper.idleMainLooper()
    assertEquals("Track One", playbackManager.state.value.currentTrack?.title)
  }

  @Test
  fun testPlayDownloadedMp3() = runBlocking {
    val downloadedTrack = Track(
      id = "dl_101",
      title = "Direct Download Song",
      artist = "Web Streamer",
      album = "NEO GLASS X Downloads",
      durationMs = 195000L,
      streamUrl = validDownloadedMp3File.toURI().toString(),
      localPath = validDownloadedMp3File.absolutePath,
      artworkUrl = "",
      isDownloaded = true,
      format = "MP3"
    )

    playbackManager.playTrack(downloadedTrack)
    ShadowLooper.idleMainLooper()

    val state = playbackManager.state.value
    assertEquals("Direct Download Song", state.currentTrack?.title)
    assertTrue(state.currentTrack?.isDownloaded == true)
    assertNull(state.errorMessage)
  }

  @Test
  fun testHandleMissingFileAndUnsupportedScheme() = runBlocking {
    // 1. Missing local file
    val missingTrack = Track(
      id = "missing_1",
      title = "Missing File Track",
      artist = "Ghost Artist",
      durationMs = 100000L,
      streamUrl = "file:///non/existent/path/song.mp3",
      localPath = "/non/existent/path/song.mp3",
      artworkUrl = ""
    )

    playbackManager.playTrack(missingTrack)
    ShadowLooper.idleMainLooper()
    // Give IO coroutine a moment to validate and post error
    Thread.sleep(100)
    ShadowLooper.idleMainLooper()

    var state = playbackManager.state.value
    assertFalse(state.isPlaying)
    assertFalse(state.isBuffering)
    assertNotNull(state.errorMessage)
    assertTrue("Expected file not found error but was: ${state.errorMessage}", state.errorMessage!!.contains("not found"))

    // 2. Unsupported protocol scheme (e.g. ftp:// or raw string)
    val invalidSchemeTrack = Track(
      id = "invalid_scheme",
      title = "Bad Scheme Track",
      artist = "Unknown",
      durationMs = 100000L,
      streamUrl = "ftp://example.com/audio.mp3",
      artworkUrl = ""
    )

    playbackManager.playTrack(invalidSchemeTrack)
    ShadowLooper.idleMainLooper()
    Thread.sleep(100)
    ShadowLooper.idleMainLooper()

    state = playbackManager.state.value
    assertFalse(state.isPlaying)
    assertNotNull(state.errorMessage)
    assertTrue(state.errorMessage!!.contains("Unsupported"))
  }

  @Test
  fun testPlayerStateReflectsActivePlayback() = runBlocking {
    val track = Track(
      id = "state_test",
      title = "State Integrity Check",
      artist = "Acoustic Neo",
      durationMs = 215000L,
      streamUrl = validMp3File.toURI().toString(),
      localPath = validMp3File.absolutePath,
      artworkUrl = ""
    )

    playbackManager.playTrack(track)
    ShadowLooper.idleMainLooper()

    val state = playbackManager.state.value
    assertEquals("State Integrity Check", state.currentTrack?.title)
    assertEquals(1, state.queue.size)
    assertEquals(0, state.currentIndex)
    assertEquals(RepeatMode.OFF, state.repeatMode)
    assertFalse(state.isShuffle)

    // Toggle Repeat
    playbackManager.toggleRepeat()
    assertEquals(RepeatMode.ALL, playbackManager.state.value.repeatMode)

    // Toggle Shuffle
    playbackManager.toggleShuffle()
    assertTrue(playbackManager.state.value.isShuffle)
  }
}
