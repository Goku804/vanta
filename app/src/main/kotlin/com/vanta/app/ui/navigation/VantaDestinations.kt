package com.vanta.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.ui.graphics.vector.ImageVector

/** Top-level, bottom-nav-reachable destinations. Secondary screens (player,
 * playlist detail, individual video, settings, file manager) are pushed on
 * top of these and are not part of the bottom nav itself. */
sealed class VantaDestination(
    val route: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector,
    val labelRes: Int
) {
    data object Home : VantaDestination("home", Icons.Filled.Home, Icons.Outlined.Home, com.vanta.app.R.string.nav_home)
    data object Music : VantaDestination("music", Icons.Filled.LibraryMusic, Icons.Outlined.LibraryMusic, com.vanta.app.R.string.nav_music)
    data object Morning : VantaDestination("morning", Icons.Filled.WbTwilight, Icons.Outlined.WbTwilight, com.vanta.app.R.string.nav_morning)

    companion object {
        val bottomNavItems = listOf(Home, Music, Morning)
    }
}

object VantaRoutes {
    const val HOME = "home"
    const val MUSIC = "music"
    const val MORNING = "morning"
    const val PLAYLISTS = "playlists"
    const val PLAYLIST_DETAIL = "playlist/{playlistId}"
    const val VIDEOS = "videos"
    const val VIDEO_PLAYER = "video/{videoId}"
    const val FILES = "files"
    const val SETTINGS = "settings"
    const val PLAYER = "player"

    fun playlistDetail(playlistId: Long) = "playlist/$playlistId"
    fun videoPlayer(videoId: Long) = "video/$videoId"
}
