package com.vanta.app.ui.videos

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vanta.app.ui.components.PhaseComingSoon

/**
 * Video browsing/playback lands in Phase 5 of the build plan. This screen is
 * a real, navigable destination (not a fake button) so the app's structure
 * is complete now, with the actual feature wired in next.
 */
@Composable
fun VideosScreen(modifier: Modifier = Modifier) {
    PhaseComingSoon(
        icon = Icons.Filled.VideoLibrary,
        title = "Videos",
        message = "Video browsing and playback arrive in Phase 5, with its own dedicated player design.",
        modifier = modifier
    )
}
