package com.vanta.app.data.repository

import com.vanta.app.data.db.dao.MorningConfigDao
import com.vanta.app.data.db.entity.MorningConfigEntity
import com.vanta.app.data.model.MorningConfig
import com.vanta.app.data.model.MorningPlaybackMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private val DEFAULT_CONFIG = MorningConfig(
    enabled = false,
    hour = 7,
    minute = 0,
    playbackMode = MorningPlaybackMode.SEQUENTIAL,
    songIds = emptyList()
)

/**
 * Persists the morning music configuration and is the single place that
 * knows how to turn it into/from its Room representation. [MorningScheduler]
 * observes this to keep the OS alarm in sync with what's saved here.
 */
class MorningRepository(private val dao: MorningConfigDao) {

    fun observeConfig(): Flow<MorningConfig> =
        dao.observe().map { it?.toDomain() ?: DEFAULT_CONFIG }

    suspend fun getConfig(): MorningConfig = withContext(Dispatchers.IO) {
        dao.get()?.toDomain() ?: DEFAULT_CONFIG
    }

    suspend fun saveConfig(config: MorningConfig) = withContext(Dispatchers.IO) {
        dao.upsert(config.toEntity())
    }
}

private fun MorningConfigEntity.toDomain() = MorningConfig(
    enabled = enabled,
    hour = hour,
    minute = minute,
    playbackMode = runCatching { MorningPlaybackMode.valueOf(playbackMode) }
        .getOrDefault(MorningPlaybackMode.SEQUENTIAL),
    songIds = songIdsCsv.split(",").mapNotNull { it.trim().toLongOrNull() }
)

private fun MorningConfig.toEntity() = MorningConfigEntity(
    id = 0,
    enabled = enabled,
    hour = hour,
    minute = minute,
    playbackMode = playbackMode.name,
    songIdsCsv = songIds.joinToString(",")
)
