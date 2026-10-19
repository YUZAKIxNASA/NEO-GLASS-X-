package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.downloader.DownloadState
import com.example.data.downloader.MediaDownloader
import com.example.data.model.Track
import com.example.data.repository.TrackRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NeoGlassXDownloaderTest {

  private lateinit var context: Context
  private lateinit var repository: TrackRepository
  private lateinit var downloader: MediaDownloader
  private lateinit var server: MockWebServer

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    repository = TrackRepository(context)
    downloader = MediaDownloader(context, repository)
    server = MockWebServer()
    server.start()
  }

  @After
  fun tearDown() {
    server.shutdown()
  }

  @Test
  fun testAppBrandingIsNeoGlassX() {
    val appName = context.getString(R.string.app_name)
    assertEquals("NEO GLASS X", appName)
  }

  @Test
  fun testBlankAndMalformedUrlRejection() = runBlocking {
    downloader.analyzeUrl("")
    var state = downloader.state.value
    assertTrue(state is DownloadState.Error)
    assertTrue((state as DownloadState.Error).message.contains("valid media link"))

    downloader.analyzeUrl("   ")
    state = downloader.state.value
    assertTrue(state is DownloadState.Error)

    downloader.analyzeUrl("not_a_url")
    state = downloader.state.value
    assertTrue(state is DownloadState.Error)
    assertTrue((state as DownloadState.Error).message.contains("Only HTTP and HTTPS"))
  }

  @Test
  fun testNonHttpHttpsUrlRejection() = runBlocking {
    downloader.analyzeUrl("ftp://example.com/audio.mp3")
    var state = downloader.state.value
    assertTrue(state is DownloadState.Error)

    downloader.analyzeUrl("file:///data/audio.mp3")
    state = downloader.state.value
    assertTrue(state is DownloadState.Error)
  }

  @Test
  fun testHtmlWebpageRejection() = runBlocking {
    server.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "text/html; charset=utf-8")
        .setBody("<html><body><h1>YouTube Video Page</h1></body></html>")
    )

    val url = server.url("/watch?v=12345").toString()
    downloader.analyzeUrl(url)

    val state = downloader.state.value
    assertTrue("Expected error for HTML page but got $state", state is DownloadState.Error)
    val errorMsg = (state as DownloadState.Error).message
    assertTrue(errorMsg.contains("web page (HTML)", ignoreCase = true))
  }

  @Test
  fun testDirectMp3UrlAnalysisOnlyProducesMp3Format() = runBlocking {
    server.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "audio/mpeg")
        .setHeader("Content-Length", "5242880")
        .setHeader("Content-Disposition", "attachment; filename=\"track_audio.mp3\"")
    )

    val url = server.url("/music/track_audio.mp3").toString()
    downloader.analyzeUrl(url)

    val state = downloader.state.value
    assertTrue("Expected Ready state but got $state", state is DownloadState.Ready)
    val info = (state as DownloadState.Ready).info
    assertEquals("track_audio", info.title)
    assertEquals(1, info.availableFormats.size)
    val format = info.availableFormats.first()
    assertEquals("mp3", format.extension)
    assertEquals("MP3 Audio", format.label)
    assertFalse(format.isVideo)
  }

  @Test
  fun testHttp404Rejection() = runBlocking {
    server.enqueue(
      MockResponse()
        .setResponseCode(404)
        .setBody("Not Found")
    )
    server.enqueue(
      MockResponse()
        .setResponseCode(404)
        .setBody("Not Found")
    )

    val url = server.url("/missing.mp3").toString()
    downloader.analyzeUrl(url)

    val state = downloader.state.value
    assertTrue("Expected error on 404 but got $state", state is DownloadState.Error)
    assertTrue((state as DownloadState.Error).message.contains("404"))
  }

  @Test
  fun testCorruptNonMp3DownloadedFileRejected() = runBlocking {
    // Return HTML body under direct .mp3 path
    server.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "audio/mpeg")
        .setHeader("Content-Length", "256")
    )

    val url = server.url("/fake_song.mp3").toString()
    downloader.analyzeUrl(url)

    val state = downloader.state.value
    assertTrue(state is DownloadState.Ready)
    val readyState = state as DownloadState.Ready

    // Now enqueue corrupt bytes for the download call
    server.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "audio/mpeg")
        .setBody("This is plain text and definitely not valid MP3 audio frames or ID3 tags.")
    )

    downloader.startDownload(readyState.info, readyState.info.availableFormats.first())
    val downloadState = downloader.state.value
    assertTrue("Expected Error on corrupt file but got $downloadState", downloadState is DownloadState.Error)
    assertTrue((downloadState as DownloadState.Error).message.contains("not valid MP3"))
  }

  @Test
  fun testValidMp3DownloadAndInsertion() = runBlocking {
    server.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "audio/mpeg")
        .setHeader("Content-Length", "256")
    )

    val url = server.url("/good_song.mp3").toString()
    downloader.analyzeUrl(url)

    val state = downloader.state.value
    assertTrue(state is DownloadState.Ready)
    val readyState = state as DownloadState.Ready

    // Enqueue valid ID3v2 MP3 bytes
    val validMp3Bytes = ByteArray(512)
    validMp3Bytes[0] = 'I'.code.toByte()
    validMp3Bytes[1] = 'D'.code.toByte()
    validMp3Bytes[2] = '3'.code.toByte()
    validMp3Bytes[3] = 0x03.toByte() // version
    validMp3Bytes[4] = 0x00.toByte()
    // Rest of bytes dummy audio frames with MPEG sync (0xFF, 0xFB)
    validMp3Bytes[10] = 0xFF.toByte()
    validMp3Bytes[11] = 0xFB.toByte()

    server.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "audio/mpeg")
        .setHeader("Content-Length", validMp3Bytes.size.toString())
        .setBody(okio.Buffer().write(validMp3Bytes))
    )

    downloader.startDownload(readyState.info, readyState.info.availableFormats.first())
    val downloadState = downloader.state.value
    assertTrue("Expected Completed but got $downloadState", downloadState is DownloadState.Completed)
    val track = (downloadState as DownloadState.Completed).track
    assertEquals("good song", track.title)
    assertEquals("MP3", track.format)
    assertTrue(track.isDownloaded)
    assertNotNull(track.localPath)
    assertTrue(File(track.localPath!!).exists())

    // Check inserted into Room repository
    val allTracks = repository.getAllTracks().first()
    assertTrue(allTracks.any { it.id == track.id })
  }

  @Test
  fun testTrackModelDurationFormatting() {
    val track1 = Track(
      id = "1",
      title = "Track One",
      artist = "Artist",
      durationMs = 236000L,
      streamUrl = "https://example.com/audio.mp3",
      artworkUrl = ""
    )
    assertEquals("3:56", track1.formattedDuration)

    val track2 = Track(
      id = "2",
      title = "Track Two",
      artist = "Artist",
      durationMs = 65000L,
      streamUrl = "https://example.com/audio.mp3",
      artworkUrl = ""
    )
    assertEquals("1:05", track2.formattedDuration)
  }
}
