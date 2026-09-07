package com.flux.android.presentation.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.SmartDisplay
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flux.android.data.player.FluxPlayerManager
import com.flux.android.domain.model.MusicTrack
import com.flux.android.presentation.auth.AuthViewModel
import com.flux.android.presentation.auth.LoginDialog
import com.flux.android.presentation.components.FluxMiniPlayer
import com.flux.android.presentation.components.VideoPlayerModal
import com.flux.android.presentation.genre.GenreScreen
import com.flux.android.presentation.genre.GenreViewModel
import com.flux.android.presentation.home.HomeScreen
import com.flux.android.presentation.home.HomeViewModel
import com.flux.android.presentation.movies.MoviesScreen
import com.flux.android.presentation.movies.MoviesViewModel
import com.flux.android.presentation.player.FullPlayerScreen
import com.flux.android.presentation.search.SearchScreen
import com.flux.android.presentation.search.SearchViewModel
import com.flux.android.presentation.settings.SettingsDialog
import com.flux.android.presentation.theme.LocalFluxColors
import com.flux.android.presentation.theme.ThemeViewModel
import com.flux.android.presentation.video.VideoScreen
import com.flux.android.presentation.video.VideoViewModel
import com.flux.android.presentation.wishlist.WishlistScreen
import com.flux.android.presentation.wishlist.WishlistViewModel

enum class NavigationTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String,
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home, "nav_tab_home"),
    VIDEO("Video", Icons.Filled.SmartDisplay, Icons.Outlined.SmartDisplay, "nav_tab_video"),
    GENRES("Genres", Icons.Filled.Category, Icons.Outlined.Category, "nav_tab_genres"),
    MOVIES("Movies", Icons.Filled.Movie, Icons.Outlined.Movie, "nav_tab_movies"),
    WISHLIST("Wishlist", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder, "nav_tab_wishlist"),
}

@Composable
fun FluxMainNavigation(
    homeViewModel: HomeViewModel,
    videoViewModel: VideoViewModel,
    genreViewModel: GenreViewModel,
    moviesViewModel: MoviesViewModel,
    wishlistViewModel: WishlistViewModel,
    searchViewModel: SearchViewModel,
    authViewModel: AuthViewModel,
    themeViewModel: ThemeViewModel,
    playerManager: FluxPlayerManager,
) {
    val fluxColors = LocalFluxColors.current

    var currentTab by remember { mutableStateOf(NavigationTab.HOME) }
    var isSearchOpen by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Video modal state
    var activeVideoTrack by remember { mutableStateOf<MusicTrack?>(null) }

    // Observers
    val playerState by playerManager.uiState.collectAsState()
    val homeUiState by homeViewModel.uiState.collectAsState()
    val videoUiState by videoViewModel.uiState.collectAsState()
    val genreUiState by genreViewModel.uiState.collectAsState()
    val moviesUiState by moviesViewModel.uiState.collectAsState()
    val wishlistUiState by wishlistViewModel.uiState.collectAsState()
    val searchUiState by searchViewModel.uiState.collectAsState()
    val authUiState by authViewModel.uiState.collectAsState()
    val userSession by authViewModel.session.collectAsState()
    val themeConfig by themeViewModel.themeConfig.collectAsState()

    val wishlistedIds =
        remember(wishlistUiState.items) {
            wishlistUiState.items.map { it.videoId }.toSet()
        }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (!isSearchOpen) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background),
                ) {
                    // Persistent Mini Player above the Navigation Bar
                    FluxMiniPlayer(
                        playerState = playerState,
                        onPlayPause = { playerManager.togglePlayPause() },
                        onNext = { playerManager.next() },
                        onOpenFullPlayer = { playerManager.setFullPlayerVisible(true) },
                    )

                    // Bottom Navigation Bar
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.onBackground,
                        tonalElevation = 0.dp,
                        modifier =
                            Modifier.border(
                                width = 1.dp,
                                color = fluxColors.border,
                                shape = androidx.compose.ui.graphics.RectangleShape,
                            ),
                    ) {
                        NavigationTab.values().forEach { tab ->
                            val isSelected = currentTab == tab && !isSearchOpen
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    isSearchOpen = false
                                    currentTab = tab
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.title,
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.title,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    )
                                },
                                colors =
                                    NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = fluxColors.surfaceHigh,
                                        unselectedIconColor = fluxColors.textMuted,
                                        unselectedTextColor = fluxColors.textMuted,
                                    ),
                                modifier = Modifier.testTag(tab.testTag),
                            )
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background),
        ) {
            if (isSearchOpen) {
                SearchScreen(
                    uiState = searchUiState,
                    wishlistedTrackIds = wishlistedIds,
                    onQueryChange = { searchViewModel.onQueryChange(it) },
                    onClearQuery = { searchViewModel.clearQuery() },
                    onLoadMore = { searchViewModel.loadMore() },
                    onSetViewMode = { searchViewModel.setViewMode(it) },
                    onTrackSelect = { track, list -> playerManager.playTrack(track, list) },
                    onWatchVideo = { activeVideoTrack = it },
                    onToggleWishlist = { track ->
                        wishlistViewModel.toggleWishlist(track, onRequireLogin = { authViewModel.showLogin() })
                    },
                    onBack = { isSearchOpen = false },
                )
            } else {
                when (currentTab) {
                    NavigationTab.HOME -> {
                        HomeScreen(
                            uiState = homeUiState,
                            wishlistedTrackIds = wishlistedIds,
                            onTrackSelect = { track, list -> playerManager.playTrack(track, list) },
                            onWatchVideo = { activeVideoTrack = it },
                            onToggleWishlist = { track ->
                                wishlistViewModel.toggleWishlist(track, onRequireLogin = { authViewModel.showLogin() })
                            },
                            onSetViewMode = { homeViewModel.setViewMode(it) },
                            onSetRegion = { homeViewModel.setRegion(it) },
                            onRetry = { homeViewModel.loadTrending() },
                            onOpenSearch = { isSearchOpen = true },
                            onOpenSettings = { showSettingsDialog = true },
                        )
                    }

                    NavigationTab.VIDEO -> {
                        VideoScreen(
                            uiState = videoUiState,
                            categories = videoViewModel.categories,
                            wishlistedTrackIds = wishlistedIds,
                            onSelectCategory = { videoViewModel.selectCategory(it) },
                            onWatchVideo = { activeVideoTrack = it },
                            onListenAudio = { track, list -> playerManager.playTrack(track, list) },
                            onToggleWishlist = { track ->
                                wishlistViewModel.toggleWishlist(track, onRequireLogin = { authViewModel.showLogin() })
                            },
                            onSetViewMode = { videoViewModel.setViewMode(it) },
                            onRetry = { videoViewModel.loadVideos() },
                        )
                    }

                    NavigationTab.GENRES -> {
                        GenreScreen(
                            uiState = genreUiState,
                            wishlistedTrackIds = wishlistedIds,
                            onSelectGenre = { genreViewModel.selectGenre(it) },
                            onTrackSelect = { track, list -> playerManager.playTrack(track, list) },
                            onWatchVideo = { activeVideoTrack = it },
                            onToggleWishlist = { track ->
                                wishlistViewModel.toggleWishlist(track, onRequireLogin = { authViewModel.showLogin() })
                            },
                            onSetViewMode = { genreViewModel.setViewMode(it) },
                            onRetry = { genreViewModel.loadGenreTracks() },
                        )
                    }

                    NavigationTab.MOVIES -> {
                        MoviesScreen(
                            uiState = moviesUiState,
                            onWatchTrailer = { movie ->
                                activeVideoTrack =
                                    MusicTrack(
                                        id = movie.id,
                                        title = movie.title,
                                        artist = movie.channelTitle ?: "Movie Trailer",
                                        thumbnailUrl = movie.thumbnailUrl,
                                        duration = movie.duration,
                                        isVideo = true,
                                    )
                            },
                            onRetry = { moviesViewModel.loadMovies() },
                        )
                    }

                    NavigationTab.WISHLIST -> {
                        WishlistScreen(
                            uiState = wishlistUiState,
                            onTrackSelect = { track, list -> playerManager.playTrack(track, list) },
                            onPlayAll = { list ->
                                if (list.isNotEmpty()) {
                                    playerManager.playTrack(list.first(), list)
                                }
                            },
                            onWatchVideo = { activeVideoTrack = it },
                            onRemoveFromWishlist = { videoId -> wishlistViewModel.removeFromWishlist(videoId) },
                            onSetViewMode = { wishlistViewModel.setViewMode(it) },
                            onOpenLogin = { authViewModel.showLogin() },
                            onRetry = { wishlistViewModel.loadWishlist() },
                            onDiscover = { currentTab = NavigationTab.HOME },
                        )
                    }
                }
            }

            // Full Player Screen Overlay
            if (playerState.isFullPlayerVisible && playerState.currentTrack != null) {
                val currentTrack = playerState.currentTrack!!
                FullPlayerScreen(
                    playerState = playerState,
                    playerManager = playerManager,
                    isWishlisted = wishlistedIds.contains(currentTrack.id),
                    onToggleWishlist = { track ->
                        wishlistViewModel.toggleWishlist(track, onRequireLogin = { authViewModel.showLogin() })
                    },
                    onWatchVideo = { track ->
                        playerManager.setFullPlayerVisible(false)
                        activeVideoTrack = track
                    },
                    onDismiss = { playerManager.setFullPlayerVisible(false) },
                )
            }

            // Video Player Modal Overlay
            activeVideoTrack?.let { videoTrack ->
                VideoPlayerModal(
                    videoId = videoTrack.id,
                    title = videoTrack.title,
                    channelTitle = videoTrack.artist,
                    onDismiss = { activeVideoTrack = null },
                )
            }

            // Login Dialog Overlay
            if (authUiState.showLoginDialog) {
                LoginDialog(
                    uiState = authUiState,
                    onLogin = { email ->
                        authViewModel.login(email) {
                            wishlistViewModel.loadWishlist()
                        }
                    },
                    onDismiss = { authViewModel.dismissLogin() },
                )
            }

            // Settings Dialog Overlay
            if (showSettingsDialog) {
                SettingsDialog(
                    themeConfig = themeConfig,
                    userSession = userSession,
                    onSetThemeMode = { themeViewModel.setThemeMode(it) },
                    onSetAccentTheme = { themeViewModel.setAccentTheme(it) },
                    onLogout = { authViewModel.logout() },
                    onOpenLogin = { authViewModel.showLogin() },
                    onDismiss = { showSettingsDialog = false },
                )
            }
        }
    }
}
