package com.example.data.repository

import android.content.Context
import android.net.Uri
import android.os.Environment
import com.example.data.db.AppDatabase
import com.example.data.db.TrackEntity
import com.example.data.metadata.AudioMetadataExtractor
import com.example.data.metadata.ExtractedAudioMetadata
import com.example.data.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File

class TrackRepository(private val context: Context) {
  private val trackDao = AppDatabase.getDatabase(context).trackDao()
  private val metadataExtractor = AudioMetadataExtractor(context)

  fun getAllTracks(): Flow<List<Track>> =
    trackDao.getAllTracks().map { list -> list.map { it.toTrack() } }

  fun getFavoriteTracks(): Flow<List<Track>> =
    trackDao.getFavoriteTracks().map { list -> list.map { it.toTrack() } }

  fun getDownloadedTracks(): Flow<List<Track>> =
    trackDao.getDownloadedTracks().map { list -> list.map { it.toTrack() } }

  fun getTracksByCategory(category: String): Flow<List<Track>> =
    trackDao.getTracksByCategory(category).map { list -> list.map { it.toTrack() } }

  suspend fun insertTrack(track: Track) = withContext(Dispatchers.IO) {
    trackDao.insertTrack(TrackEntity.fromTrack(track))
  }

  suspend fun toggleFavorite(trackId: String, currentFavorite: Boolean) = withContext(Dispatchers.IO) {
    trackDao.updateFavorite(trackId, !currentFavorite)
  }

  suspend fun deleteTrack(trackId: String) = withContext(Dispatchers.IO) {
    trackDao.deleteTrackById(trackId)
  }

  suspend fun clearDemoTracksIfAny() = withContext(Dispatchers.IO) {
    val demoIds = listOf(
      "track_1", "track_2", "track_3", "track_4",
      "track_5", "track_6", "track_7", "track_8"
    )
    for (id in demoIds) {
      trackDao.deleteTrackById(id)
    }
  }

  fun extractMetadata(file: File, fallbackTitle: String? = null, fallbackArtist: String? = null): ExtractedAudioMetadata {
    return metadataExtractor.extract(file, fallbackTitle, fallbackArtist)
  }

  suspend fun syncOfflineFiles() = withContext(Dispatchers.IO) {
    try {
      val musicDirs = listOfNotNull(
        context.getExternalFilesDir(Environment.DIRECTORY_MUSIC),
        context.filesDir
      )

      val foundFiles = mutableListOf<File>()
      for (dir in musicDirs) {
        if (dir.exists()) {
          val files = dir.listFiles { _, name -> name.endsWith(".mp3", ignoreCase = true) } ?: emptyArray()
          foundFiles.addAll(files.filter { it.length() > 64 })
        }
      }

      val existingTracks = trackDao.getAllTracksSnapshot()
      val existingByPath = existingTracks.associateBy { it.localPath }
      val existingById = existingTracks.associateBy { it.id }

      // 1. Ingest or update files found on disk
      for (file in foundFiles) {
        val existing = existingByPath[file.absolutePath] ?: existingById[file.name]
        val meta = metadataExtractor.extract(file)
        val artworkUri = meta.artworkPath?.let { Uri.fromFile(File(it)).toString() } ?: ""

        if (existing == null) {
          val track = Track(
            id = file.name,
            title = meta.title,
            artist = meta.artist,
            album = meta.album,
            durationMs = if (meta.durationMs > 0) meta.durationMs else 180000L,
            streamUrl = Uri.fromFile(file).toString(),
            localPath = file.absolutePath,
            artworkUrl = artworkUri,
            isFavorite = false,
            category = meta.genre ?: "Downloaded",
            isDownloaded = true,
            format = "MP3",
            isVideo = false,
            addedAt = file.lastModified(),
            albumArtist = meta.albumArtist,
            genre = meta.genre,
            year = meta.year,
            trackNumber = meta.trackNumber
          )
          trackDao.insertTrack(TrackEntity.fromTrack(track))
        } else {
          // If artwork or metadata can be enriched, update without destroying favorite status
          var needsUpdate = false
          var updatedArtwork = existing.artworkUrl
          if (updatedArtwork.isBlank() && artworkUri.isNotBlank()) {
            updatedArtwork = artworkUri
            needsUpdate = true
          }

          val updatedTitle = if (existing.title.isBlank() || existing.title == "Direct Audio Track") {
            needsUpdate = true
            meta.title
          } else existing.title

          val updatedArtist = if (existing.artist.isBlank() || existing.artist == "Offline Artist" || existing.artist == "Direct Source") {
            needsUpdate = true
            meta.artist
          } else existing.artist

          if (needsUpdate) {
            val updatedTrack = existing.copy(
              title = updatedTitle,
              artist = updatedArtist,
              album = if (existing.album == "Unknown Album" || existing.album == "NEO GLASS X Downloads") meta.album else existing.album,
              artworkUrl = updatedArtwork,
              durationMs = if (existing.durationMs <= 0 && meta.durationMs > 0) meta.durationMs else existing.durationMs,
              albumArtist = existing.albumArtist ?: meta.albumArtist,
              genre = existing.genre ?: meta.genre,
              year = existing.year ?: meta.year,
              trackNumber = existing.trackNumber ?: meta.trackNumber
            )
            trackDao.updateTrack(updatedTrack)
          }
        }
      }

      // 2. Safe orphaned track cleanup: remove DB records whose physical file was deleted
      val foundPaths = foundFiles.map { it.absolutePath }.toSet()
      for (existing in existingTracks) {
        if (existing.isDownloaded && existing.localPath != null && !foundPaths.contains(existing.localPath)) {
          if (!File(existing.localPath).exists()) {
            trackDao.deleteTrackById(existing.id)
          }
        }
      }
    } catch (_: Exception) {}
  }
}
