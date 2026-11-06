package com.example.data.metadata

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import java.io.File
import java.io.FileOutputStream

data class ExtractedAudioMetadata(
  val title: String,
  val artist: String,
  val album: String,
  val albumArtist: String? = null,
  val durationMs: Long = 0L,
  val trackNumber: Int? = null,
  val genre: String? = null,
  val year: Int? = null,
  val artworkPath: String? = null
)

class AudioMetadataExtractor(private val context: Context) {

  private val artworkDir: File by lazy {
    val dir = File(context.filesDir, "artwork")
    if (!dir.exists()) dir.mkdirs()
    dir
  }

  fun extract(file: File, fallbackTitle: String? = null, fallbackArtist: String? = null): ExtractedAudioMetadata {
    if (!file.exists() || !file.canRead() || file.length() < 64) {
      val defaultTitle = fallbackTitle ?: cleanFilename(file.nameWithoutExtension)
      return ExtractedAudioMetadata(
        title = defaultTitle,
        artist = fallbackArtist ?: "Unknown Artist",
        album = "Unknown Album",
        durationMs = 0L
      )
    }

    val retriever = MediaMetadataRetriever()
    var rawTitle: String? = null
    var rawArtist: String? = null
    var rawAlbum: String? = null
    var rawAlbumArtist: String? = null
    var rawDuration: Long? = null
    var rawGenre: String? = null
    var rawYear: Int? = null
    var rawTrackNumber: Int? = null
    var picBytes: ByteArray? = null

    try {
      retriever.setDataSource(file.absolutePath)
      rawTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)?.trim()?.takeIf { it.isNotBlank() }
      rawArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)?.trim()?.takeIf { it.isNotBlank() }
      rawAlbum = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)?.trim()?.takeIf { it.isNotBlank() }
      rawAlbumArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST)?.trim()?.takeIf { it.isNotBlank() }
      rawDuration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()?.takeIf { it > 0 }
      rawGenre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)?.trim()?.takeIf { it.isNotBlank() }
      rawYear = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)?.toIntOrNull()
        ?: retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE)?.take(4)?.toIntOrNull()
      rawTrackNumber = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)
        ?.substringBefore("/")?.trim()?.toIntOrNull()
      picBytes = retriever.embeddedPicture
    } catch (_: Exception) {
      // Corrupt or unsupported file handled gracefully
    } finally {
      try {
        retriever.release()
      } catch (_: Exception) {}
    }

    // 1. Title fallback order: Embedded -> fallbackTitle -> clean filename -> "Unknown Track"
    val finalTitle = rawTitle
      ?: fallbackTitle?.takeIf { it.isNotBlank() && it != "Direct Audio Track" }
      ?: cleanFilename(file.nameWithoutExtension).takeIf { it.isNotBlank() }
      ?: "Unknown Track"

    // 2. Artist fallback order: Embedded -> AlbumArtist -> fallbackArtist -> "Unknown Artist"
    val finalArtist = rawArtist
      ?: rawAlbumArtist
      ?: fallbackArtist?.takeIf { it.isNotBlank() && it != "Offline Artist" && it != "Direct Source" }
      ?: "Unknown Artist"

    // 3. Album fallback order: Embedded -> "Unknown Album"
    val finalAlbum = rawAlbum ?: "Unknown Album"

    // 4. Artwork caching and storage
    val artworkPath = if (picBytes != null && picBytes.isNotEmpty()) {
      val albumKey = if (finalAlbum != "Unknown Album" && finalArtist != "Unknown Artist") {
        "${finalArtist}_${finalAlbum}"
      } else {
        file.nameWithoutExtension
      }
      cacheArtworkBytes(picBytes, albumKey, file)
    } else {
      null
    }

    return ExtractedAudioMetadata(
      title = finalTitle,
      artist = finalArtist,
      album = finalAlbum,
      albumArtist = rawAlbumArtist,
      durationMs = rawDuration ?: 0L,
      trackNumber = rawTrackNumber,
      genre = rawGenre,
      year = rawYear,
      artworkPath = artworkPath
    )
  }

  fun extractArtworkOnly(file: File, albumKey: String? = null): String? {
    if (!file.exists() || !file.canRead() || file.length() < 64) return null
    val retriever = MediaMetadataRetriever()
    return try {
      retriever.setDataSource(file.absolutePath)
      val bytes = retriever.embeddedPicture
      if (bytes != null && bytes.isNotEmpty()) {
        val key = albumKey ?: file.nameWithoutExtension
        cacheArtworkBytes(bytes, key, file)
      } else null
    } catch (_: Exception) {
      null
    } finally {
      try { retriever.release() } catch (_: Exception) {}
    }
  }

  private fun cacheArtworkBytes(bytes: ByteArray, albumKey: String, sourceFile: File): String? {
    return try {
      val safeKey = albumKey.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(40)
      val hash = (sourceFile.name + sourceFile.length() + bytes.size).hashCode().toUInt()
      val outputFile = File(artworkDir, "art_${safeKey}_${hash}.jpg")

      if (outputFile.exists() && outputFile.length() > 0) {
        return outputFile.absolutePath
      }

      // Read bounds to avoid OutOfMemoryError on large cover art
      val boundsOptions = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
      }
      BitmapFactory.decodeByteArray(bytes, 0, bytes.size, boundsOptions)

      val maxDimension = maxOf(boundsOptions.outWidth, boundsOptions.outHeight)
      var sampleSize = 1
      while ((maxDimension / sampleSize) > 512) {
        sampleSize *= 2
      }

      val decodeOptions = BitmapFactory.Options().apply {
        inSampleSize = sampleSize
        inPreferredConfig = Bitmap.Config.RGB_565
      }

      val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions) ?: return null
      FileOutputStream(outputFile).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
      }
      bitmap.recycle()

      outputFile.absolutePath
    } catch (_: Exception) {
      null
    }
  }

  private fun cleanFilename(rawName: String): String {
    return rawName
      .replace(Regex("_\\d{10,}$"), "") // trailing timestamp
      .replace(Regex("\\.(mp3|MP3)$"), "")
      .replace(Regex("[_\\-]"), " ")
      .trim()
  }
}
