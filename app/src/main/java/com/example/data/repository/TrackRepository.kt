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
}
