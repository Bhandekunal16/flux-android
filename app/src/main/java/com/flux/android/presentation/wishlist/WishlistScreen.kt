package com.flux.android.presentation.wishlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flux.android.domain.model.MusicTrack
import com.flux.android.domain.model.WishlistItem
import com.flux.android.presentation.components.EmptyStateView
import com.flux.android.presentation.components.ErrorStateView
import com.flux.android.presentation.components.FluxGlassCard
import com.flux.android.presentation.components.ShimmerGrid
import com.flux.android.presentation.components.TrackCardGrid
import com.flux.android.presentation.components.TrackCardList
import com.flux.android.presentation.components.ViewModeToggle
import com.flux.android.presentation.theme.LocalFluxColors

@Composable
fun WishlistScreen(
    uiState: WishlistUiState,
    onTrackSelect: (MusicTrack, List<MusicTrack>) -> Unit,
    onPlayAll: (List<MusicTrack>) -> Unit,
    onWatchVideo: (MusicTrack) -> Unit,
    onRemoveFromWishlist: (String) -> Unit,
    onSetViewMode: (Boolean) -> Unit,
    onOpenLogin: () -> Unit,
    onRetry: () -> Unit,
    onDiscover: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fluxColors = LocalFluxColors.current
    val tracks = uiState.items.map { it.toMusicTrack() }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .testTag("wishlist_screen"),
    ) {
        // Top Header
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = "My Wishlist",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "${uiState.items.size} saved favorite tracks",
                    style = MaterialTheme.typography.bodySmall,
                    color = fluxColors.textMuted,
                )
            }

            if (uiState.items.isNotEmpty()) {
                ViewModeToggle(
                    isGrid = uiState.isGrid,
                    onToggle = onSetViewMode,
                )
            }
        }

        // Unauthenticated Banner Card if needed
        if (uiState.isUnauthenticated) {
            FluxGlassCard(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Sign in",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Sync Your Wishlist",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = "Sign in to keep your library in sync across devices.",
                            style = MaterialTheme.typography.bodySmall,
                            color = fluxColors.textMuted,
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onOpenLogin,
                        shape = RoundedCornerShape(12.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = fluxColors.onPrimaryText,
                            ),
                        modifier = Modifier.testTag("wishlist_sign_in_btn"),
                    ) {
                        Text("Sign In", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = fluxColors.onPrimaryText)
                    }
                }
            }
        }

        // Action Bar: Play All
        if (tracks.isNotEmpty()) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = { onPlayAll(tracks) },
                    shape = RoundedCornerShape(14.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = fluxColors.onPrimaryText,
                        ),
                    modifier = Modifier.testTag("wishlist_play_all_btn"),
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play All",
                        tint = fluxColors.onPrimaryText,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Play All (${tracks.size})", fontWeight = FontWeight.SemiBold, color = fluxColors.onPrimaryText)
                }
            }
        }

        when {
            uiState.isLoading -> {
                ShimmerGrid(count = 4)
            }

            !uiState.errorMessage.isNullOrEmpty() && uiState.items.isEmpty() -> {
                ErrorStateView(
                    message = uiState.errorMessage,
                    onRetry = onRetry,
                )
            }

            uiState.items.isEmpty() -> {
                EmptyStateView(
                    icon = Icons.Default.Favorite,
                    title = "Your Wishlist is Empty",
                    description = "Save songs, live shows, and videos by tapping the heart icon anywhere in Flux.",
                    actionLabel = "Discover Music",
                    onAction = onDiscover,
                )
            }

            uiState.isGrid -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(tracks, key = { it.id }) { track ->
                        TrackCardGrid(
                            track = track,
                            isWishlisted = true,
                            onListen = { onTrackSelect(track, tracks) },
                            onWatch = { onWatchVideo(track) },
                            onWishlistToggle = { onRemoveFromWishlist(track.id) },
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(tracks, key = { it.id }) { track ->
                        TrackCardList(
                            track = track,
                            isWishlisted = true,
                            onListen = { onTrackSelect(track, tracks) },
                            onWatch = { onWatchVideo(track) },
                            onWishlistToggle = { onRemoveFromWishlist(track.id) },
                        )
                    }
                }
            }
        }
    }
}
