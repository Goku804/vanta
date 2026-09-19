package com.vanta.app.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vanta.app.permissions.audioPermission
import com.vanta.app.permissions.rememberPermissionState
import com.vanta.app.permissions.videoPermission

/**
 * Real permission-status view (not a stub): shows what VANTA can currently
 * access on this device. The overlay permission and full preferences UI
 * (spec section 22) expand here as later phases add the features they gate.
 */
@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val audio = rememberPermissionState(audioPermission())
    val video = rememberPermissionState(videoPermission())

    Column(modifier = modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 20.dp))

        Text("Permissions", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 8.dp))

        PermissionRow(label = "Music library access", granted = audio.isGranted)
        PermissionRow(label = "Video library access", granted = video.isGranted)
        PermissionRow(label = "Floating widget overlay", granted = false, note = "Available in Phase 8")

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 24.dp))
        Text(
            "VANTA · 0.1.0 (Phase 1–3 build)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PermissionRow(label: String, granted: Boolean, note: String? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp))
            .padding(16.dp)
            .padding(top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            if (note != null) {
                Text(note, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Icon(
            imageVector = if (granted) Icons.Filled.Check else Icons.Filled.Close,
            contentDescription = if (granted) "Granted" else "Not granted",
            tint = if (granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
