package com.vanta.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.vanta.app.data.db.entity.VideoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {

    @Query("SELECT * FROM videos ORDER BY dateAddedMs DESC")
    fun observeAll(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE id = :id")
    suspend fun getById(id: Long): VideoEntity?

    @Query("SELECT * FROM videos WHERE title LIKE '%' || :query || '%' ORDER BY dateAddedMs DESC")
    fun search(query: String): Flow<List<VideoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(videos: List<VideoEntity>)

    @Query("DELETE FROM videos WHERE id NOT IN (:keepIds)")
    suspend fun pruneMissing(keepIds: List<Long>)

    @Transaction
    suspend fun replaceLibraryScan(scanned: List<VideoEntity>) {
        upsertAll(scanned)
        pruneMissing(scanned.map { it.id })
    }
}
