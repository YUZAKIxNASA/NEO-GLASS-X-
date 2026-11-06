package com.example.data.repository

import android.content.Context
import com.example.data.db.AppDatabase
import com.example.data.db.TrackEntity
import com.example.data.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class TrackRepository(private val context: Context) {
  private val trackDao = AppDatabase.getDatabase(context).trackDao()

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
    // Ensure no mock demo tracks exist in the database
    val demoIds = listOf(
      "track_1", "track_2", "track_3", "track_4",
      "track_5", "track_6", "track_7", "track_8"
    )
    for (id in demoIds) {
      trackDao.deleteTrackById(id)
    }
  }

  suspend fun syncOfflineFiles() = withContext(Dispatchers.IO) {
    try {
      val musicDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_MUSIC) ?: context.filesDir
      if (musicDir.exists()) {
        val mp3Files = musicDir.listFiles { _, name -> name.endsWith(".mp3", ignoreCase = true) } ?: emptyArray()
        for (file in mp3Files) {
          if (file.length() > 64) {
            val existing = trackDao.getTrackById(file.name)
            if (existing == null) {
              val title = file.nameWithoutExtension.replace(Regex("_\\d+$"), "").replace("_", " ")
              val retriever = android.media.MediaMetadataRetriever()
              var dur = 180000L
              var artist = "Offline Artist"
              try {
                retriever.setDataSource(file.absolutePath)
                dur = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 180000L
                val extractedArtist = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_ARTIST)
                if (!extractedArtist.isNullOrBlank()) artist = extractedArtist
                retriever.release()
              } catch (_: Exception) {}

              val track = Track(
                id = file.name,
                title = title,
                artist = artist,
                album = "NEO GLASS X Downloads",
                durationMs = dur,
                streamUrl = android.net.Uri.fromFile(file).toString(),
                localPath = file.absolutePath,
                artworkUrl = "",
                isFavorite = false,
                category = "Downloaded",
                isDownloaded = true,
                format = "MP3",
                isVideo = false,
                addedAt = file.lastModified()
              )
              insertTrack(track)
            }
          }
        }
      }
    } catch (_: Exception) {}
  }
}
