package com.vanta.app.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vanta.app.data.model.Song
import com.vanta.app.di.AppContainer
import com.vanta.app.ui.VantaViewModelFactory
import com.vanta.app.ui.components.MiniPlayerBar
import com.vanta.app.ui.files.FilesScreen
import com.vanta.app.ui.home.HomeScreen
import com.vanta.app.ui.morning.MorningScreen
import com.vanta.app.ui.morning.MorningViewModel
import com.vanta.app.ui.music.MusicLibraryScreen
import com.vanta.app.ui.music.MusicViewModel
import com.vanta.app.ui.player.PlayerScreen
import com.vanta.app.ui.player.PlayerViewModel
import com.vanta.app.ui.playlists.PlaylistsScreen
import com.vanta.app.ui.playlists.PlaylistsViewModel
import com.vanta.app.ui.settings.SettingsScreen
import com.vanta.app.ui.video.VideoPlayerScreen
import com.vanta.app.ui.video.VideoPlayerViewModel
import com.vanta.app.ui.videos.VideoLibraryScreen
import com.vanta.app.ui.videos.VideoLibraryViewModel

@Composable
fun VantaNavHost(container: AppContainer) {
    val navController = rememberNavController()
    val factory = VantaViewModelFactory(container)

    // Shared across every screen so "currently playing" state (mini player,
    // song-row highlighting, morning selection) all agree with one source.
    val musicViewModel: MusicViewModel = viewModel(factory = factory)
    val playerViewModel: PlayerViewModel = viewModel(factory = factory)
    val morningViewModel: MorningViewModel = viewModel(factory = factory)
    val playlistsViewModel: PlaylistsViewModel = viewModel(factory = factory)
    val videoLibraryViewModel: VideoLibraryViewModel = viewModel(factory = factory)
    val videoPlayerViewModel: VideoPlayerViewModel = viewModel(factory = factory)

    val playbackState by playerViewModel.playbackState.collectAsState()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = VantaDestination.bottomNavItems.any { it.route == currentRoute } ||
        currentRoute in listOf(VantaRoutes.PLAYLISTS, VantaRoutes.VIDEOS, VantaRoutes.FILES, VantaRoutes.SETTINGS)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                Column {
                    AnimatedVisibility(
                        visible = playbackState.currentSong != null,
                        enter = slideInVertically(initialOffsetY = { it }),
                        exit = slideOutVertically(targetOffsetY = { it })
                    ) {
                        MiniPlayerBar(
                            state = playbackState,
                            onTogglePlayPause = playerViewModel::togglePlayPause,
                            onSkipNext = playerViewModel::skipNext,
                            onClick = { navController.navigate(VantaRoutes.PLAYER) }
                        )
                    }
                    VantaBottomBar(navController = navController, currentRoute = currentRoute)
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = VantaRoutes.HOME,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(VantaRoutes.HOME) {
                    HomeScreen(
                        musicViewModel = musicViewModel,
                        morningViewModel = morningViewModel,
                        onNavigate = { route -> navController.navigate(route) }
                    )
                }
                composable(VantaRoutes.MUSIC) {
                    MusicLibraryScreen(
                        viewModel = musicViewModel,
                        onSongClick = { song: Song ->
                            musicViewModel.playSong(song)
                            navController.navigate(VantaRoutes.PLAYER)
                        }
                    )
                }
                composable(VantaRoutes.MORNING) {
                    MorningScreen(viewModel = morningViewModel, musicViewModel = musicViewModel)
                }
                composable(VantaRoutes.PLAYLISTS) {
                    PlaylistsScreen(viewModel = playlistsViewModel)
                }
                composable(VantaRoutes.VIDEOS) {
                    VideoLibraryScreen(
                        viewModel = videoLibraryViewModel,
                        onVideoClick = { video ->
                            navController.navigate(VantaRoutes.videoPlayer(video.id))
                        }
                    )
                }
                composable(
                    route = VantaRoutes.VIDEO_PLAYER,
                    arguments = listOf(navArgument("videoId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val videoId = backStackEntry.arguments?.getLong("videoId") ?: return@composable
                    VideoPlayerScreen(
                        videoId = videoId,
                        viewModel = videoPlayerViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(VantaRoutes.FILES) {
                    FilesScreen()
                }
                composable(VantaRoutes.SETTINGS) {
                    SettingsScreen()
                }
                composable(VantaRoutes.PLAYER) {
                    PlayerScreen(
                        viewModel = playerViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

@Composable
private fun VantaBottomBar(navController: androidx.navigation.NavController, currentRoute: String?) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        VantaDestination.bottomNavItems.forEach { destination ->
            val selected = currentRoute == destination.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selected) destination.filledIcon else destination.outlinedIcon,
                        contentDescription = stringResource(destination.labelRes)
                    )
                },
                label = { Text(stringResource(destination.labelRes)) }
            )
        }
    }
}
