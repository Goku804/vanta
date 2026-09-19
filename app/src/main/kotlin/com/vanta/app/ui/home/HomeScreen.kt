package com.vanta.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.vanta.app.ui.morning.MorningViewModel
import com.vanta.app.ui.music.MusicViewModel

private data class HomeShortcut(val label: String, val icon: ImageVector, val route: String)

private val shortcuts = listOf(
    HomeShortcut("Music", Icons.Filled.LibraryMusic, "music"),
    HomeShortcut("Videos", Icons.Filled.VideoLibrary, "videos"),
    HomeShortcut("Morning", Icons.Filled.WbTwilight, "morning"),
    HomeShortcut("Playlists", Icons.Filled.PlaylistPlay, "playlists"),
    HomeShortcut("Files", Icons.Filled.Folder, "files")
)

@Composable
fun HomeScreen(
    musicViewModel: MusicViewModel,
    morningViewModel: MorningViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val libraryState by musicViewModel.uiState.collectAsState()
    val morningState by morningViewModel.uiState.collectAsState()

    Column(modifier = modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp)) {
        Text("VANTA", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.onBackground)
        Text(
            "Your futuristic media system",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 20.dp))

        StatusStrip(
            trackCount = libraryState.songs.size,
            morningLabel = morningState.nextPlaybackLabel,
            morningEnabled = morningState.config.enabled
        )

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 24.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(shortcuts) { shortcut ->
                ShortcutTile(shortcut = shortcut, onClick = { onNavigate(shortcut.route) })
            }
        }
    }
}

@Composable
private fun StatusStrip(trackCount: Int, morningLabel: String, morningEnabled: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(18.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("$trackCount", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            Text("tracks in library", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                if (morningEnabled) "On" else "Off",
                style = MaterialTheme.typography.headlineMedium,
                color = if (morningEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(morningLabel, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ShortcutTile(shortcut: HomeShortcut, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .aspectRatio(1.3f)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(18.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(shortcut.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(shortcut.label, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}
