package com.flux.android.presentation.genre

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flux.android.domain.model.GenreItem
import com.flux.android.domain.model.MusicTrack
import com.flux.android.presentation.components.EmptyStateView
import com.flux.android.presentation.components.ErrorStateView
import com.flux.android.presentation.components.ShimmerGrid
import com.flux.android.presentation.components.TrackCardGrid
import com.flux.android.presentation.components.TrackCardList
import com.flux.android.presentation.components.ViewModeToggle
import com.flux.android.presentation.theme.LocalFluxColors

@Composable
fun GenreScreen(
    uiState: GenreUiState,
    wishlistedTrackIds: Set<String>,
    onSelectGenre: (GenreItem) -> Unit,
    onTrackSelect: (MusicTrack, List<MusicTrack>) -> Unit,
    onWatchVideo: (MusicTrack) -> Unit,
    onToggleWishlist: (MusicTrack) -> Unit,
    onSetViewMode: (Boolean) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fluxColors = LocalFluxColors.current

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .testTag("genre_screen"),
    ) {
        // Title Bar
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = "Explore Genres",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "18 curated music categories",
                    style = MaterialTheme.typography.bodySmall,
                    color = fluxColors.textMuted,
                )
            }

            ViewModeToggle(
                isGrid = uiState.isGrid,
                onToggle = onSetViewMode,
            )
        }

        // Horizontal scrolling 18 genres list
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(uiState.genres, key = { it.id }) { genre ->
                val isSelected = uiState.selectedGenre?.id == genre.id
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else fluxColors.surfaceHigh,
                    border =
                        androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else fluxColors.outlineVariant,
                        ),
                    modifier =
                        Modifier
                            .clickable { onSelectGenre(genre) }
                            .testTag("genre_chip_${genre.id}"),
                ) {
                    Text(
                        text = genre.name,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) fluxColors.onPrimaryText else MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    )
                }
            }
        }

        // Selected Genre info badge
        uiState.selectedGenre?.let { genre ->
            if (genre.description.isNotEmpty()) {
                Text(
                    text = genre.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = fluxColors.textMuted,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Tracks Content
        when {
            uiState.isLoadingTracks -> {
                ShimmerGrid(count = 6)
            }

            !uiState.errorMessage.isNullOrEmpty() && uiState.tracks.isEmpty() -> {
                ErrorStateView(
                    message = uiState.errorMessage,
                    onRetry = onRetry,
                )
            }

            uiState.tracks.isEmpty() -> {
                EmptyStateView(
                    icon = Icons.Default.Category,
                    title = "No Tracks for this Genre",
                    description = "We couldn't find tracks for ${uiState.selectedGenre?.name ?: "this genre"}.",
                    actionLabel = "Retry",
                    onAction = onRetry,
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
                    items(uiState.tracks, key = { it.id }) { track ->
                        val isWishlisted = wishlistedTrackIds.contains(track.id)
                        TrackCardGrid(
                            track = track,
                            isWishlisted = isWishlisted,
                            onListen = { onTrackSelect(track, uiState.tracks) },
                            onWatch = { onWatchVideo(track) },
                            onWishlistToggle = { onToggleWishlist(track) },
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
                    items(uiState.tracks, key = { it.id }) { track ->
                        val isWishlisted = wishlistedTrackIds.contains(track.id)
                        TrackCardList(
                            track = track,
                            isWishlisted = isWishlisted,
                            onListen = { onTrackSelect(track, uiState.tracks) },
                            onWatch = { onWatchVideo(track) },
                            onWishlistToggle = { onToggleWishlist(track) },
                        )
                    }
                }
            }
        }
    }
}
