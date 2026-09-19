package com.vanta.app.ui.files

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.vanta.app.ui.components.PhaseComingSoon

/**
 * The media-oriented file manager (spec section 9) lands in Phase 7, after
 * video support and conversion exist for it to organize.
 */
@Composable
fun FilesScreen(modifier: Modifier = Modifier) {
    PhaseComingSoon(
        icon = Icons.Filled.Folder,
        title = "Files",
        message = "The media file manager arrives in Phase 7, once video and converted-audio files exist to organize.",
        modifier = modifier
    )
}
