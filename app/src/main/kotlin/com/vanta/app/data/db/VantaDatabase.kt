package com.vanta.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.vanta.app.data.db.dao.MorningConfigDao
import com.vanta.app.data.db.dao.PlaylistDao
import com.vanta.app.data.db.dao.SongDao
import com.vanta.app.data.db.entity.MorningConfigEntity
import com.vanta.app.data.db.entity.PlaylistEntity
import com.vanta.app.data.db.entity.PlaylistSongCrossRef
import com.vanta.app.data.db.entity.SongEntity

@Database(
    entities = [
        SongEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
        MorningConfigEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class VantaDatabase : RoomDatabase() {

    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun morningConfigDao(): MorningConfigDao

    companion object {
        @Volatile private var instance: VantaDatabase? = null

        fun get(context: Context): VantaDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    VantaDatabase::class.java,
                    "vanta.db"
                ).build().also { instance = it }
            }
    }
}
