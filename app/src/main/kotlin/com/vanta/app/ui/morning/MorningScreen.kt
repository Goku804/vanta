package com.vanta.app.ui.morning

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vanta.app.data.model.MorningPlaybackMode
import com.vanta.app.data.model.Song
import com.vanta.app.ui.music.MusicViewModel

@Composable
fun MorningScreen(
    viewModel: MorningViewModel,
    musicViewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val libraryState by musicViewModel.uiState.collectAsState()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 16.dp)
    ) {
        item {
            Text("Morning Music", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 16.dp))
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.WbTwilight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                            Text("Enabled", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                            Text(state.nextPlaybackLabel, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = state.config.enabled,
                            onCheckedChange = viewModel::setEnabled,
                            colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
                        )
                    }

                    if (!state.canScheduleExact) {
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 8.dp))
                        Text(
                            "Precise alarm permission isn't granted — morning playback may start a little late. Enable \"Alarms & reminders\" in system settings for exact timing.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }

                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 16.dp))
                    TimePickerRow(
                        hour = state.config.hour,
                        minute = state.config.minute,
                        onChange = viewModel::setTime
                    )

                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 16.dp))
                    Row {
                        FilterChip(
                            selected = state.config.playbackMode == MorningPlaybackMode.SEQUENTIAL,
                            onClick = { viewModel.setPlaybackMode(MorningPlaybackMode.SEQUENTIAL) },
                            label = { Text("In order") }
                        )
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(start = 8.dp))
                        FilterChip(
                            selected = state.config.playbackMode == MorningPlaybackMode.SHUFFLE,
                            onClick = { viewModel.setPlaybackMode(MorningPlaybackMode.SHUFFLE) },
                            label = { Text("Shuffle") }
                        )
                    }
                }
            }
        }

        item {
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 24.dp))
            Text(
                "Selected songs (${state.selectedSongs.size})",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 8.dp))
        }

        if (libraryState.songs.isEmpty()) {
            item {
                Text(
                    "Your music library is empty, so there's nothing to schedule yet. Open Music to scan your device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(libraryState.songs, key = { it.id }) { song ->
                SelectableSongRow(
                    song = song,
                    isSelected = state.config.songIds.contains(song.id),
                    onToggle = { viewModel.toggleSong(song) }
                )
            }
        }
    }
}

@Composable
private fun TimePickerRow(hour: Int, minute: Int, onChange: (Int, Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            "%02d:%02d".format(hour, minute),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Column(modifier = Modifier.padding(start = 20.dp)) {
            StepperRow(label = "Hour") { delta -> onChange(((hour + delta + 24) % 24), minute) }
            StepperRow(label = "Minute") { delta -> onChange(hour, ((minute + delta * 5 + 60) % 60)) }
        }
    }
}

@Composable
private fun StepperRow(label: String, onStep: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        androidx.compose.material3.TextButton(onClick = { onStep(-1) }) { Text("−") }
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        androidx.compose.material3.TextButton(onClick = { onStep(1) }) { Text("+") }
    }
}

@Composable
private fun SelectableSongRow(song: Song, isSelected: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(
                if (isSelected) MaterialTheme.colorScheme.surfaceVariant else androidx.compose.ui.graphics.Color.Transparent,
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onToggle)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                song.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                song.artist ?: "Unknown artist",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (isSelected) {
            Icon(Icons.Filled.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
        }
    }
}
