package com.vanta.app.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/** The runtime permission that gates access to on-device audio, by API level. */
fun audioPermission(): String =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

/** The runtime permission that gates access to on-device video, by API level. */
fun videoPermission(): String =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_VIDEO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

fun isGranted(context: Context, permission: String): Boolean =
    ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

/** Observable, re-checkable grant state for [permission], for use in Compose UI. */
@Composable
fun rememberPermissionState(permission: String): PermissionState {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(isGranted(context, permission)) }
    return remember(permission) {
        PermissionState(
            permission = permission,
            isGrantedProvider = { granted },
            refresh = { granted = isGranted(context, permission) }
        )
    }
}

class PermissionState(
    val permission: String,
    private val isGrantedProvider: () -> Boolean,
    val refresh: () -> Unit
) {
    val isGranted: Boolean get() = isGrantedProvider()
}
