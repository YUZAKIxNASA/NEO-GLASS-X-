package com.example.data.downloader

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import com.example.data.model.Track
import com.example.data.repository.TrackRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.URLDecoder
import java.util.UUID
import java.util.concurrent.TimeUnit

data class ExtractedMediaInfo(
  val title: String,
  val artist: String,
  val durationMs: Long,
  val artworkUrl: String,
  val sourceUrl: String,
  val estimatedSizeBytes: Long,
  val availableFormats: List<MediaFormatOption>
)

data class MediaFormatOption(
  val label: String,
  val extension: String,
  val isVideo: Boolean = false,
  val quality: String,
  val sizeDisplay: String
)

sealed class DownloadState {
  object Idle : DownloadState()
  data class Analyzing(val url: String) : DownloadState()
  data class Ready(val info: ExtractedMediaInfo) : DownloadState()
  data class Downloading(
    val title: String,
    val progress: Float, // 0.0f to 1.0f, or -1.0f if total length is unknown
    val downloadedBytes: Long,
    val totalBytes: Long,
    val speedMBs: Float
  ) : DownloadState()
  data class Completed(val track: Track) : DownloadState()
  data class Error(val message: String) : DownloadState()
}

class MediaDownloader(
  private val context: Context,
  private val repository: TrackRepository
) {
  private val client = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .followRedirects(true)
    .build()

  private val _state = MutableStateFlow<DownloadState>(DownloadState.Idle)
  val state: StateFlow<DownloadState> = _state.asStateFlow()

  private var activeAnalysisJob: Job? = null
  private var activeDownloadJob: Job? = null

  suspend fun analyzeUrl(inputUrl: String) = withContext(Dispatchers.IO) {
    val cleanUrl = inputUrl.trim()

    // 1. Validate URL structure
    if (cleanUrl.isBlank()) {
      _state.value = DownloadState.Error("Please enter a valid media link.")
      return@withContext
    }

    val httpUrl = cleanUrl.toHttpUrlOrNull()
    if (httpUrl == null || (!httpUrl.scheme.equals("http", ignoreCase = true) && !httpUrl.scheme.equals("https", ignoreCase = true))) {
      _state.value = DownloadState.Error("Invalid URL. Only HTTP and HTTPS links are supported.")
      return@withContext
    }

    _state.value = DownloadState.Analyzing(cleanUrl)

    try {
      // 2. Probe URL: Try HEAD first, fall back to Range GET
      var contentLength = -1L
      var contentType = ""
      var contentDisposition = ""
      var statusCode = 0

      val headRequest = Request.Builder()
        .url(httpUrl)
        .header("User-Agent", "NEO-GLASS-X-Downloader/1.0")
        .head()
        .build()

      var headSuccess = false
      try {
        client.newCall(headRequest).execute().use { response ->
          statusCode = response.code
          if (response.isSuccessful) {
            contentLength = response.header("Content-Length")?.toLongOrNull() ?: -1L
            contentType = response.header("Content-Type") ?: ""
            contentDisposition = response.header("Content-Disposition") ?: ""
            headSuccess = true
          }
        }
      } catch (_: Exception) {
        headSuccess = false
      }

      // If HEAD was rejected (e.g. 405 Method Not Allowed) or failed, fall back to a limited GET
      if (!headSuccess) {
        val rangeGetRequest = Request.Builder()
          .url(httpUrl)
          .header("User-Agent", "NEO-GLASS-X-Downloader/1.0")
          .header("Range", "bytes=0-1023")
          .get()
          .build()

        client.newCall(rangeGetRequest).execute().use { response ->
          statusCode = response.code
          if (!response.isSuccessful && response.code != 206) {
            _state.value = DownloadState.Error("Server returned HTTP error ${response.code}: ${response.message}")
            return@withContext
          }
          val contentRange = response.header("Content-Range")
          if (contentRange != null && contentRange.contains("/")) {
            contentLength = contentRange.substringAfterLast("/").toLongOrNull() ?: -1L
          } else {
            contentLength = response.header("Content-Length")?.toLongOrNull() ?: -1L
          }
          contentType = response.header("Content-Type") ?: ""
          contentDisposition = response.header("Content-Disposition") ?: ""
        }
      }

      // 3. Inspect Content-Type: Webpages (HTML) are NOT downloadable audio streams
      val lowerContentType = contentType.lowercase()
      if (lowerContentType.contains("text/html") || lowerContentType.contains("application/xhtml")) {
        _state.value = DownloadState.Error(
          "The provided link points to a web page (HTML), not a direct MP3 audio stream. Webpage streaming platforms require direct stream URLs."
        )
        return@withContext
      }

      // 4. Derive clean, honest title
      var derivedTitle = ""
      if (contentDisposition.isNotBlank() && contentDisposition.contains("filename=")) {
        val rawFilename = contentDisposition.substringAfter("filename=").substringBefore(";").trim('"', '\'')
        derivedTitle = rawFilename.replace(Regex("\\.(mp3|MP3)$"), "")
      }

      if (derivedTitle.isBlank()) {
        val lastSegment = httpUrl.pathSegments.lastOrNull { it.isNotBlank() } ?: "Direct Audio"
        derivedTitle = try {
          URLDecoder.decode(lastSegment, "UTF-8")
            .replace(Regex("\\.(mp3|MP3|m4a|wav|ogg|flac|aac)$"), "")
            .replace("_", " ")
            .replace("-", " ")
            .trim()
        } catch (_: Exception) {
          "Direct Audio Track"
        }
      }

      if (derivedTitle.isBlank()) {
        derivedTitle = "Direct Audio Track"
      }

      // Clean artist
      val host = httpUrl.host
      val derivedArtist = if (host.isNotBlank()) host else "Direct Source"

      // 5. Only MP3 is supported — no video, no mp4 options
      val formatOption = MediaFormatOption(
        label = "MP3 Audio",
        extension = "mp3",
        isVideo = false,
        quality = if (contentLength > 0) "Direct Audio (${formatBytes(contentLength)})" else "Direct Stream",
        sizeDisplay = formatBytes(contentLength)
      )

      val mediaInfo = ExtractedMediaInfo(
        title = derivedTitle.take(60),
        artist = derivedArtist,
        durationMs = 0L, // Real duration determined after stream inspection
        artworkUrl = "",
        sourceUrl = cleanUrl,
        estimatedSizeBytes = contentLength,
        availableFormats = listOf(formatOption)
      )

      _state.value = DownloadState.Ready(mediaInfo)
    } catch (e: Exception) {
      if (e is CancellationException) throw e
      _state.value = DownloadState.Error("Failed to analyze link: ${e.localizedMessage ?: "Network connection error"}")
    }
  }

  suspend fun startDownload(mediaInfo: ExtractedMediaInfo, format: MediaFormatOption) = withContext(Dispatchers.IO) {
    _state.value = DownloadState.Downloading(
      title = mediaInfo.title,
      progress = if (mediaInfo.estimatedSizeBytes > 0) 0f else -1f,
      downloadedBytes = 0L,
      totalBytes = mediaInfo.estimatedSizeBytes,
      speedMBs = 0f
    )

    val musicDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC) ?: context.filesDir
    if (!musicDir.exists()) musicDir.mkdirs()

    val tempFile = File(musicDir, "download_${UUID.randomUUID()}.tmp")
    val safeFileName = mediaInfo.title.replace(Regex("[^a-zA-Z0-9.-]"), "_").take(40)
    val destFile = File(musicDir, "${safeFileName}_${System.currentTimeMillis()}.mp3")

    var downloadSuccess = false

    try {
      val request = Request.Builder()
        .url(mediaInfo.sourceUrl)
        .header("User-Agent", "NEO-GLASS-X-Downloader/1.0")
        .build()

      val response = client.newCall(request).execute()
      if (!response.isSuccessful) {
        _state.value = DownloadState.Error("Download failed with HTTP ${response.code}: ${response.message}")
        return@withContext
      }

      val body = response.body
      if (body == null) {
        _state.value = DownloadState.Error("Empty audio stream received from server.")
        return@withContext
      }

      val totalLength = if (body.contentLength() > 0) body.contentLength() else mediaInfo.estimatedSizeBytes
      val inputStream = body.byteStream()
      val outputStream = FileOutputStream(tempFile)

      val buffer = ByteArray(32 * 1024)
      var bytesRead: Int
      var totalBytesRead = 0L
      var lastUpdateTime = System.currentTimeMillis()
      var lastBytes = 0L

      inputStream.use { input ->
        outputStream.use { output ->
          while (input.read(buffer).also { bytesRead = it } != -1) {
            output.write(buffer, 0, bytesRead)
            totalBytesRead += bytesRead

            val now = System.currentTimeMillis()
            if (now - lastUpdateTime >= 200) {
              val elapsedSecs = (now - lastUpdateTime) / 1000f
              val speed = if (elapsedSecs > 0) {
                ((totalBytesRead - lastBytes) / (1024f * 1024f)) / elapsedSecs
              } else 0f

              val progressFraction = if (totalLength > 0) {
                (totalBytesRead.toFloat() / totalLength.toFloat()).coerceIn(0f, 1f)
              } else -1f

              _state.value = DownloadState.Downloading(
                title = mediaInfo.title,
                progress = progressFraction,
                downloadedBytes = totalBytesRead,
                totalBytes = totalLength,
                speedMBs = speed.coerceAtLeast(0.05f)
              )

              lastUpdateTime = now
              lastBytes = totalBytesRead
            }
          }
        }
      }

      // 6. Verify downloaded content is valid MP3
      if (!isValidMp3File(tempFile)) {
        tempFile.delete()
        _state.value = DownloadState.Error(
          "The downloaded file is not valid MP3 audio or contains corrupt data. Download was discarded."
        )
        return@withContext
      }

      // 7. Atomically rename to final .mp3 file
      if (tempFile.renameTo(destFile)) {
        downloadSuccess = true
      } else {
        // Fallback copy if renameTo across filesystems fails
        tempFile.copyTo(destFile, overwrite = true)
        tempFile.delete()
        downloadSuccess = true
      }

      // 8. Extract real duration and tags from actual file
      val realDurationMs = extractMediaDuration(destFile)

      val downloadedTrack = Track(
        id = UUID.randomUUID().toString(),
        title = mediaInfo.title,
        artist = mediaInfo.artist,
        album = "NEO GLASS X Downloads",
        durationMs = if (realDurationMs > 0) realDurationMs else 180000L,
        streamUrl = Uri.fromFile(destFile).toString(),
        localPath = destFile.absolutePath,
        artworkUrl = "",
        isFavorite = false,
        category = "Downloaded",
        isDownloaded = true,
        format = "MP3",
        isVideo = false,
        addedAt = System.currentTimeMillis()
      )

      repository.insertTrack(downloadedTrack)
      _state.value = DownloadState.Completed(downloadedTrack)
    } catch (e: Exception) {
      if (e is CancellationException) throw e
      _state.value = DownloadState.Error("Download interrupted: ${e.localizedMessage ?: "Network transfer error"}")
    } finally {
      if (!downloadSuccess && tempFile.exists()) {
        tempFile.delete()
      }
    }
  }

  fun reset() {
    _state.value = DownloadState.Idle
  }

  private fun isValidMp3File(file: File): Boolean {
    if (!file.exists() || file.length() < 64) return false
    return try {
      FileInputStream(file).use { fis ->
        val header = ByteArray(10)
        val read = fis.read(header)
        if (read < 4) return false

        // Check for ID3v2: 'I', 'D', '3'
        val isId3 = header[0] == 0x49.toByte() && header[1] == 0x44.toByte() && header[2] == 0x33.toByte()
        if (isId3) return true

        // Check for MPEG sync word: 11 bits of 1s (0xFF, and high 3 bits set)
        val b0 = header[0].toInt() and 0xFF
        val b1 = header[1].toInt() and 0xFF
        if (b0 == 0xFF && (b1 and 0xE0) == 0xE0) return true

        // Inspect first 2048 bytes for MPEG frame sync
        val buffer = ByteArray(2048)
        val count = fis.read(buffer)
        if (count > 0) {
          for (i in 0 until count - 1) {
            val byteA = buffer[i].toInt() and 0xFF
            val byteB = buffer[i + 1].toInt() and 0xFF
            if (byteA == 0xFF && (byteB and 0xE0) == 0xE0) {
              return true
            }
          }
        }
        false
      }
    } catch (_: Exception) {
      false
    }
  }

  private fun extractMediaDuration(file: File): Long {
    return try {
      val retriever = MediaMetadataRetriever()
      retriever.setDataSource(file.absolutePath)
      val timeStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
      retriever.release()
      timeStr?.toLongOrNull() ?: 0L
    } catch (_: Exception) {
      0L
    }
  }

  private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "Unknown size"
    val mb = bytes / (1024.0 * 1024.0)
    return "%.1f MB".format(mb)
  }
}
