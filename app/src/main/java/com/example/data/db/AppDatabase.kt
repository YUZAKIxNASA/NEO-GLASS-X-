package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [TrackEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
  abstract fun trackDao(): TrackDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    val MIGRATION_2_3 = object : Migration(2, 3) {
      override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE tracks ADD COLUMN albumArtist TEXT DEFAULT NULL")
        db.execSQL("ALTER TABLE tracks ADD COLUMN genre TEXT DEFAULT NULL")
        db.execSQL("ALTER TABLE tracks ADD COLUMN year INTEGER DEFAULT NULL")
        db.execSQL("ALTER TABLE tracks ADD COLUMN trackNumber INTEGER DEFAULT NULL")
      }
    }

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "neonwave_database"
        )
          .addMigrations(MIGRATION_2_3)
          .fallbackToDestructiveMigrationOnDowngrade()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
