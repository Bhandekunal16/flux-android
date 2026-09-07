package com.flux.android.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.flux.android.presentation.components.EmptyStateView
import com.flux.android.presentation.components.ErrorStateView
import com.flux.android.presentation.components.ShimmerGrid
import com.flux.android.presentation.components.TrackCardGrid
import com.flux.android.presentation.components.TrackCardList
import com.flux.android.presentation.components.ViewModeToggle
import com.flux.android.presentation.theme.LocalFluxColors

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    wishlistedTrackIds: Set<String>,
    onTrackSelect: (MusicTrack, List<MusicTrack>) -> Unit,
    onWatchVideo: (MusicTrack) -> Unit,
    onToggleWishlist: (MusicTrack) -> Unit,
    onSetViewMode: (Boolean) -> Unit,
    onSetRegion: (String) -> Unit,
    onRetry: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fluxColors = LocalFluxColors.current

    val regions = listOf(
        Pair("IN", "India"),
        Pair("US", "Global"),
        Pair("GB", "UK"),
        Pair("KR", "Korea"),
        Pair("JP", "Japan")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen")
    ) {
        // Top App Bar: Brand Logo, Search Bar Trigger, Settings
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Logo & Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(fluxColors.accentGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Flux Logo",
                        tint = fluxColors.onPrimaryText,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "FLUX",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Editorial Discovery",
                        style = MaterialTheme.typography.labelSmall,
                        color = fluxColors.textMuted,
                        fontSize = 10.sp
                    )
                }
            }

            // Top Action buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onOpenSearch,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(fluxColors.surfaceHigh)
                        .testTag("home_search_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(fluxColors.surfaceHigh)
                        .testTag("home_settings_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Region selector chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(regions) { (code, name) ->
                val isSelected = uiState.selectedRegion == code
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else fluxColors.surfaceHigh,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else fluxColors.outlineVariant
                    ),
                    modifier = Modifier
                        .clickable { onSetRegion(code) }
                        .testTag("region_chip_$code")
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) fluxColors.onPrimaryText else MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // Section Title & Grid/List View Mode Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Trending Right Now",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            ViewModeToggle(
                isGrid = uiState.isGrid,
                onToggle = onSetViewMode
            )
        }

        // Content: Shimmer / Error / Empty / Grid / List
        when {
            uiState.isLoading -> {
                ShimmerGrid(count = 6)
            }
            !uiState.errorMessage.isNullOrEmpty() && uiState.tracks.isEmpty() -> {
                ErrorStateView(
                    message = uiState.errorMessage,
                    onRetry = onRetry
                )
            }
            uiState.tracks.isEmpty() -> {
                EmptyStateView(
                    icon = Icons.Default.MusicNote,
                    title = "No Trending Tracks Found",
                    description = "We couldn't retrieve trending tracks at this moment.",
                    actionLabel = "Refresh",
                    onAction = onRetry
                )
            }
            uiState.isGrid -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.tracks, key = { it.id }) { track ->
                        val isWishlisted = wishlistedTrackIds.contains(track.id)
                        TrackCardGrid(
                            track = track,
                            isWishlisted = isWishlisted,
                            onListen = { onTrackSelect(track, uiState.tracks) },
                            onWatch = { onWatchVideo(track) },
                            onWishlistToggle = { onToggleWishlist(track) }
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.tracks, key = { it.id }) { track ->
                        val isWishlisted = wishlistedTrackIds.contains(track.id)
                        TrackCardList(
                            track = track,
                            isWishlisted = isWishlisted,
                            onListen = { onTrackSelect(track, uiState.tracks) },
                            onWatch = { onWatchVideo(track) },
                            onWishlistToggle = { onToggleWishlist(track) }
                        )
                    }
                }
            }
        }
    }
}
