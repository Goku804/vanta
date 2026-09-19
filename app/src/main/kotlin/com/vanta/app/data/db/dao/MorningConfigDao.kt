package com.vanta.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vanta.app.data.db.entity.MorningConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MorningConfigDao {

    @Query("SELECT * FROM morning_config WHERE id = 0")
    fun observe(): Flow<MorningConfigEntity?>

    @Query("SELECT * FROM morning_config WHERE id = 0")
    suspend fun get(): MorningConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(config: MorningConfigEntity)
}
