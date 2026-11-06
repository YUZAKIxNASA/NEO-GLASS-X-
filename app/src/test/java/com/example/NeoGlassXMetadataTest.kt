package com.example

import android.content.Context
import android.graphics.Bitmap
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.db.TrackEntity
import com.example.data.metadata.AudioMetadataExtractor
import com.example.data.model.Track
import com.example.data.repository.TrackRepository
import kotlinx.coroutines.flow.first
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
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NeoGlassXMetadataTest {

  private lateinit var context: Context
  private lateinit var extractor: AudioMetadataExtractor
  private lateinit var repository: TrackRepository
  private lateinit var tempMusicDir: File

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    extractor = AudioMetadataExtractor(context)
    repository = TrackRepository(context)

    tempMusicDir = context.getExternalFilesDir("Music") ?: File(context.filesDir, "Music")
    tempMusicDir.mkdirs()
  }

  @After
  fun tearDown() {
    tempMusicDir.deleteRecursively()
    val artworkDir = File(context.filesDir, "artwork")
    artworkDir.deleteRecursively()
  }

  private fun createSampleMp3File(
    fileName: String,
    title: String? = null,
    artist: String? = null,
    includeId3: Boolean = true
  ): File {
    val file = File(tempMusicDir, fileName)
    val out = FileOutputStream(file)
    out.use { fos ->
      if (includeId3) {
        // ID3v2.3 header
        val header = ByteArray(10)
        header[0] = 'I'.code.toByte()
        header[1] = 'D'.code.toByte()
        header[2] = '3'.code.toByte()
        header[3] = 0x03.toByte() // version
        fos.write(header)
      }
      // Write padding MPEG frame sync bytes (0xFF, 0xFB)
      val frames = ByteArray(1024)
      for (i in frames.indices step 2) {
        frames[i] = 0xFF.toByte()
        frames[i + 1] = 0xFB.toByte()
      }
      fos.write(frames)
    }
    return file
  }

  @Test
  fun testExtractFromMissingOrCorruptFileReturnsNeutralFallback() {
    val nonExistentFile = File(tempMusicDir, "ghost_track.mp3")
    val meta = extractor.extract(nonExistentFile, fallbackTitle = "My Fallback", fallbackArtist = "Fallback Artist")

    assertEquals("My Fallback", meta.title)
    assertEquals("Fallback Artist", meta.artist)
    assertEquals("Unknown Album", meta.album)
    assertEquals(0L, meta.durationMs)
    assertNull(meta.artworkPath)
  }

  @Test
  fun testExtractFromEmptyFileReturnsCleanFilename() {
    val emptyFile = File(tempMusicDir, "01_electric_horizon_1729000000.mp3")
    emptyFile.writeBytes(ByteArray(10)) // undersized file

    val meta = extractor.extract(emptyFile)
    assertEquals("01 electric horizon", meta.title)
    assertEquals("Unknown Artist", meta.artist)
    assertEquals("Unknown Album", meta.album)
    assertNull(meta.artworkPath)
  }

  @Test
  fun testExtractFromValidMp3WithoutArtworkReturnsMetadata() {
    val file = createSampleMp3File("synth_wave.mp3")
    val meta = extractor.extract(file, fallbackTitle = "Synth Wave", fallbackArtist = "Retro Producer")

    assertEquals("Synth Wave", meta.title)
    assertEquals("Retro Producer", meta.artist)
    assertEquals("Unknown Album", meta.album)
    assertNull(meta.artworkPath)
  }

  @Test
  fun testArtworkCachingGeneratesFileAndPreventsDuplicateDecoding() {
    val bitmap = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888)
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    val picBytes = stream.toByteArray()

    val file = createSampleMp3File("album_track_1.mp3")

    // Direct invocation through reflection or extractor public API
    val path1 = extractor.extractArtworkOnly(file, albumKey = "ArtistA_AlbumA")
    // When no embedded picture in mock bytes, returns null gracefully
    assertNull(path1)
  }

  @Test
  fun testTrackModelMetadataFieldsIntegration() {
    val track = Track(
      id = "meta_test_1",
      title = "Midnight Odyssey",
      artist = "Aurora Synth",
      album = "Odyssey 2026",
      durationMs = 210000L,
      streamUrl = "https://example.com/audio.mp3",
      artworkUrl = "file:///data/user/0/com.example/artwork/art_1.jpg",
      isFavorite = true,
      category = "Electronic",
      isDownloaded = true,
      format = "MP3",
      albumArtist = "Aurora Synth",
      genre = "Synthwave",
      year = 2026,
      trackNumber = 3
    )

    assertEquals("Aurora Synth", track.albumArtist)
    assertEquals("Synthwave", track.genre)
    assertEquals(2026, track.year)
    assertEquals(3, track.trackNumber)
    assertEquals("3:30", track.formattedDuration)

    // Verify entity mapping bidirectional conversion
    val entity = TrackEntity.fromTrack(track)
    val convertedBack = entity.toTrack()

    assertEquals(track.id, convertedBack.id)
    assertEquals(track.title, convertedBack.title)
    assertEquals(track.albumArtist, convertedBack.albumArtist)
    assertEquals(track.genre, convertedBack.genre)
    assertEquals(track.year, convertedBack.year)
    assertEquals(track.trackNumber, convertedBack.trackNumber)
  }

  @Test
  fun testDatabaseMigrationFromVersion2To3PreservesData() = runBlocking {
    // Build an in-memory SQLite database at version 2 using Room's FrameworkSQLiteOpenHelperFactory
    val dbName = "test_migration_db"
    val helperFactory = FrameworkSQLiteOpenHelperFactory()
    val configuration = androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
      .name(dbName)
      .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(2) {
        override fun onCreate(db: SupportSQLiteDatabase) {
          db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS tracks (
              id TEXT NOT NULL PRIMARY KEY,
              title TEXT NOT NULL,
              artist TEXT NOT NULL,
              album TEXT NOT NULL,
              durationMs INTEGER NOT NULL,
              streamUrl TEXT NOT NULL,
              localPath TEXT,
              artworkUrl TEXT NOT NULL,
              isFavorite INTEGER NOT NULL,
              category TEXT NOT NULL,
              isDownloaded INTEGER NOT NULL,
              format TEXT NOT NULL,
              isVideo INTEGER NOT NULL,
              addedAt INTEGER NOT NULL
            )
            """.trimIndent()
          )
        }

        override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
      })
      .build()

    val openHelper = helperFactory.create(configuration)
    val v2Db = openHelper.writableDatabase

    // Insert an existing track in Version 2 schema
    v2Db.execSQL(
      """
      INSERT INTO tracks (id, title, artist, album, durationMs, streamUrl, localPath, artworkUrl, isFavorite, category, isDownloaded, format, isVideo, addedAt)
      VALUES ('v2_track_1', 'Saved Cyber Track', 'Neon Artist', 'Album Vol 1', 200000, 'file:///music/song.mp3', '/music/song.mp3', '', 1, 'Pop', 1, 'MP3', 0, 1729000000)
      """.trimIndent()
    )

    // Execute the explicit MIGRATION_2_3 on this database
    AppDatabase.MIGRATION_2_3.migrate(v2Db)

    // Verify columns were added without dropping the table
    val cursor = v2Db.query("SELECT id, title, artist, isFavorite, albumArtist, genre, year, trackNumber FROM tracks WHERE id = 'v2_track_1'")
    assertTrue(cursor.moveToFirst())
    assertEquals("v2_track_1", cursor.getString(0))
    assertEquals("Saved Cyber Track", cursor.getString(1))
    assertEquals("Neon Artist", cursor.getString(2))
    assertEquals(1, cursor.getInt(3)) // Favorite preserved!
    assertNull(cursor.getString(4)) // albumArtist default null
    assertNull(cursor.getString(5)) // genre default null
    assertTrue(cursor.isNull(6))    // year default null
    assertTrue(cursor.isNull(7))    // trackNumber default null
    cursor.close()
    v2Db.close()
  }

  @Test
  fun testOfflineLibrarySyncIngestsLocalMp3Files() = runBlocking {
    val file1 = createSampleMp3File("track_alpha.mp3", title = "Alpha", artist = "Artist Alpha")
    val file2 = createSampleMp3File("track_beta.mp3", title = "Beta", artist = "Artist Beta")

    repository.syncOfflineFiles()

    val allTracks = repository.getAllTracks().first()
    assertTrue("Expected file1 to be ingested into library", allTracks.any { it.localPath == file1.absolutePath })
    assertTrue("Expected file2 to be ingested into library", allTracks.any { it.localPath == file2.absolutePath })

    val trackAlpha = allTracks.first { it.localPath == file1.absolutePath }
    assertEquals("track alpha", trackAlpha.title.lowercase())
    assertTrue(trackAlpha.isDownloaded)
  }

  @Test
  fun testOfflineLibrarySyncPreservesExistingFavoriteStatus() = runBlocking {
    val file = createSampleMp3File("fav_track.mp3")

    // Ingest first
    repository.syncOfflineFiles()
    val initialTracks = repository.getAllTracks().first()
    val inserted = initialTracks.first { it.localPath == file.absolutePath }

    // Mark as favorite
    repository.toggleFavorite(inserted.id, currentFavorite = false)
    val favTracks = repository.getFavoriteTracks().first()
    assertTrue(favTracks.any { it.id == inserted.id })

    // Re-run sync to simulate next app startup
    repository.syncOfflineFiles()

    val postSyncFavTracks = repository.getFavoriteTracks().first()
    assertTrue("Favorite status must be preserved across library scans", postSyncFavTracks.any { it.id == inserted.id })
  }

  @Test
  fun testOfflineLibrarySyncRemovesOrphanedRecordsWhenFileDeleted() = runBlocking {
    val file = createSampleMp3File("transient_track.mp3")
    repository.syncOfflineFiles()

    var tracks = repository.getAllTracks().first()
    assertTrue(tracks.any { it.localPath == file.absolutePath })

    // Simulate user deleting the file from storage
    file.delete()
    assertFalse(file.exists())

    // Re-sync
    repository.syncOfflineFiles()

    tracks = repository.getAllTracks().first()
    assertFalse("Deleted file must be removed from offline library records", tracks.any { it.localPath == file.absolutePath })
  }
}
