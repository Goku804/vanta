package com.vanta.app.ui.morning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vanta.app.data.model.MorningConfig
import com.vanta.app.data.model.MorningPlaybackMode
import com.vanta.app.data.model.Song
import com.vanta.app.data.repository.MorningRepository
import com.vanta.app.data.repository.MusicRepository
import com.vanta.app.morning.MorningScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class MorningUiState(
    val config: MorningConfig = MorningConfig(false, 7, 0, MorningPlaybackMode.SEQUENTIAL, emptyList()),
    val selectedSongs: List<Song> = emptyList(),
    val canScheduleExact: Boolean = true,
    val nextPlaybackLabel: String = "Not scheduled"
)

class MorningViewModel(
    private val morningRepository: MorningRepository,
    private val musicRepository: MusicRepository,
    private val scheduler: MorningScheduler
) : ViewModel() {

    private val selectedSongsFlow = morningRepository.observeConfig()
        .map { config -> musicRepository.getSongsByIds(config.songIds) }

    val uiState: StateFlow<MorningUiState> = combine(
        morningRepository.observeConfig(),
        selectedSongsFlow
    ) { config, songs ->
        MorningUiState(
            config = config,
            selectedSongs = songs,
            canScheduleExact = scheduler.canScheduleExactAlarms(),
            nextPlaybackLabel = nextPlaybackLabel(config)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MorningUiState())

    fun setEnabled(enabled: Boolean) = updateConfig { it.copy(enabled = enabled) }

    fun setTime(hour: Int, minute: Int) = updateConfig { it.copy(hour = hour, minute = minute) }

    fun setPlaybackMode(mode: MorningPlaybackMode) = updateConfig { it.copy(playbackMode = mode) }

    fun toggleSong(song: Song) = updateConfig { config ->
        val ids = config.songIds.toMutableList()
        if (ids.contains(song.id)) ids.remove(song.id) else ids.add(song.id)
        config.copy(songIds = ids)
    }

    private fun updateConfig(transform: (MorningConfig) -> MorningConfig) {
        viewModelScope.launch {
            val current = morningRepository.getConfig()
            val updated = transform(current)
            morningRepository.saveConfig(updated)
            scheduler.sync(updated)
        }
    }

    private fun nextPlaybackLabel(config: MorningConfig): String {
        if (!config.enabled || !config.hasSongs) return "Not scheduled"
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, config.hour)
            set(Calendar.MINUTE, config.minute)
            set(Calendar.SECOND, 0)
        }
        val isTomorrow = !target.after(now)
        val dayLabel = if (isTomorrow) "Tomorrow" else "Today"
        return "$dayLabel at ${"%02d:%02d".format(config.hour, config.minute)}"
    }
}
