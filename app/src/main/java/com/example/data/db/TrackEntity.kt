package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Track

@Entity(tableName = "tracks")
data class TrackEntity(
  @PrimaryKey val id: String,
  val title: String,
  val artist: String,
  val album: String,
  val durationMs: Long,
  val streamUrl: String,
  val localPath: String?,
  val artworkUrl: String,
  val isFavorite: Boolean,
  val category: String,
  val isDownloaded: Boolean,
  val format: String,
  val isVideo: Boolean,
  val addedAt: Long
) {
  fun toTrack(): Track = Track(
    id = id,
    title = title,
    artist = artist,
    album = album,
    durationMs = durationMs,
    streamUrl = streamUrl,
    localPath = localPath,
    artworkUrl = artworkUrl,
    isFavorite = isFavorite,
    category = category,
    isDownloaded = isDownloaded,
    format = format,
    isVideo = isVideo,
    addedAt = addedAt
  )

  companion object {
    fun fromTrack(track: Track): TrackEntity = TrackEntity(
      id = track.id,
      title = track.title,
      artist = track.artist,
      album = track.album,
      durationMs = track.durationMs,
      streamUrl = track.streamUrl,
      localPath = track.localPath,
      artworkUrl = track.artworkUrl,
      isFavorite = track.isFavorite,
      category = track.category,
      isDownloaded = track.isDownloaded,
      format = track.format,
      isVideo = track.isVideo,
      addedAt = track.addedAt
    )
  }
}
