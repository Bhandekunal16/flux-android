package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.flux.android.FluxApplication
import com.flux.android.presentation.auth.AuthViewModel
import com.flux.android.presentation.genre.GenreViewModel
import com.flux.android.presentation.home.HomeViewModel
import com.flux.android.presentation.movies.MoviesViewModel
import com.flux.android.presentation.navigation.FluxMainNavigation
import com.flux.android.presentation.search.SearchViewModel
import com.flux.android.presentation.theme.FluxTheme
import com.flux.android.presentation.theme.ThemeViewModel
import com.flux.android.presentation.video.VideoViewModel
import com.flux.android.presentation.wishlist.WishlistViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as FluxApplication
        val container = app.appContainer

        setContent {
            val themeViewModel = remember { ThemeViewModel(container.themePreferences) }
            val themeConfig by themeViewModel.themeConfig.collectAsState()

            val homeViewModel = remember { HomeViewModel(container.fluxRepository) }
            val videoViewModel = remember { VideoViewModel(container.fluxRepository) }
            val genreViewModel = remember { GenreViewModel(container.fluxRepository) }
            val moviesViewModel = remember { MoviesViewModel(container.fluxRepository) }
            val wishlistViewModel =
                remember {
                    WishlistViewModel(container.fluxRepository, container.authRepository)
                }
            val searchViewModel = remember { SearchViewModel(container.fluxRepository) }
            val authViewModel = remember { AuthViewModel(container.authRepository) }

            FluxTheme(
                mode = themeConfig.mode,
                accent = themeConfig.accent,
            ) {
                FluxMainNavigation(
                    homeViewModel = homeViewModel,
                    videoViewModel = videoViewModel,
                    genreViewModel = genreViewModel,
                    moviesViewModel = moviesViewModel,
                    wishlistViewModel = wishlistViewModel,
                    searchViewModel = searchViewModel,
                    authViewModel = authViewModel,
                    themeViewModel = themeViewModel,
                    playerManager = container.playerManager,
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
