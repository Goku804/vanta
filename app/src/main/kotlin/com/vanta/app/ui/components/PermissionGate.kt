package com.vanta.app.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vanta.app.permissions.PermissionState

/**
 * Wraps a screen that needs a runtime permission. Shows a short rationale
 * with a single request button until granted, then renders [content].
 * Deliberately does not auto-request on first composition — spec section 23
 * asks that permissions be explained before they're requested.
 */
@Composable
fun PermissionGate(
    state: PermissionState,
    rationale: String,
    onGranted: () -> Unit = {},
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { state.refresh() }

    LaunchedEffect(state.isGranted) {
        if (state.isGranted) onGranted()
    }

    if (state.isGranted) {
        content()
    } else {
        Column(
            modifier = modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            Text(
                rationale,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 16.dp))
            Button(onClick = { launcher.launch(state.permission) }) {
                Text("Grant access")
            }
        }
    }
}
