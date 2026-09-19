package com.vanta.app.ui.playlists

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vanta.app.data.model.Playlist

@Composable
fun PlaylistsScreen(viewModel: PlaylistsViewModel, modifier: Modifier = Modifier) {
    val playlists by viewModel.playlists.collectAsState()
    var newName by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp)) {
        Text("Playlists", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("New playlist name") },
                singleLine = true
            )
            IconButton(onClick = {
                viewModel.createPlaylist(newName)
                newName = ""
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Create playlist", tint = MaterialTheme.colorScheme.primary)
            }
        }

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 16.dp))

        if (playlists.isEmpty()) {
            Text(
                "No playlists yet. Create one above, then add songs from the music library.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn {
                items(playlists, key = { it.id }) { playlist ->
                    PlaylistRow(playlist = playlist, onDelete = { viewModel.deletePlaylist(playlist.id) })
                }
            }
        }
    }
}

@Composable
private fun PlaylistRow(playlist: Playlist, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.PlaylistPlay, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
            Text(playlist.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text("${playlist.songCount} songs", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "Delete playlist", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
