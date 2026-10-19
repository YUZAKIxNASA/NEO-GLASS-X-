package com.example.data.model

data class Track(
  val id: String,
  val title: String,
  val artist: String,
  val album: String = "NEO GLASS X",
  val durationMs: Long = 0L,
  val streamUrl: String,
  val localPath: String? = null,
  val artworkUrl: String,
  val isFavorite: Boolean = false,
  val category: String = "Pop",
  val isDownloaded: Boolean = false,
  val format: String = "MP3",
  val isVideo: Boolean = false,
  val addedAt: Long = System.currentTimeMillis()
) {
  val formattedDuration: String
    get() {
      val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
      val minutes = totalSeconds / 60
      val seconds = totalSeconds % 60
      return "%d:%02d".format(minutes, seconds)
    }
}
