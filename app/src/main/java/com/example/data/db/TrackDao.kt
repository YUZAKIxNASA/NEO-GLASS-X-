package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
  @Query("SELECT * FROM tracks ORDER BY addedAt DESC")
  fun getAllTracks(): Flow<List<TrackEntity>>

  @Query("SELECT * FROM tracks WHERE isFavorite = 1 ORDER BY addedAt DESC")
  fun getFavoriteTracks(): Flow<List<TrackEntity>>

  @Query("SELECT * FROM tracks WHERE isDownloaded = 1 ORDER BY addedAt DESC")
  fun getDownloadedTracks(): Flow<List<TrackEntity>>

  @Query("SELECT * FROM tracks WHERE category = :category ORDER BY addedAt DESC")
  fun getTracksByCategory(category: String): Flow<List<TrackEntity>>

  @Query("SELECT * FROM tracks WHERE id = :id LIMIT 1")
  suspend fun getTrackById(id: String): TrackEntity?

  @Query("SELECT COUNT(*) FROM tracks")
  suspend fun getTrackCount(): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTrack(track: TrackEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTracks(tracks: List<TrackEntity>)

  @Update
  suspend fun updateTrack(track: TrackEntity)

  @Query("UPDATE tracks SET isFavorite = :isFavorite WHERE id = :id")
  suspend fun updateFavorite(id: String, isFavorite: Boolean)

  @Delete
  suspend fun deleteTrack(track: TrackEntity)

  @Query("DELETE FROM tracks WHERE id = :id")
  suspend fun deleteTrackById(id: String)
}
