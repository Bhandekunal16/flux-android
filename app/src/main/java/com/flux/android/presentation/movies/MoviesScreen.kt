package com.flux.android.presentation.movies

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.flux.android.domain.model.MovieItem
import com.flux.android.presentation.components.EmptyStateView
import com.flux.android.presentation.components.ErrorStateView
import com.flux.android.presentation.components.FluxGlassCard
import com.flux.android.presentation.components.ShimmerGrid
import com.flux.android.presentation.theme.LocalFluxColors

@Composable
fun MoviesScreen(
    uiState: MoviesUiState,
    onWatchTrailer: (MovieItem) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fluxColors = LocalFluxColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("movies_screen")
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Movie Trailers & Cinema",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Official Hollywood & blockbuster cinema previews",
                style = MaterialTheme.typography.bodySmall,
                color = fluxColors.textMuted
            )
        }

        when {
            uiState.isLoading -> {
                ShimmerGrid(count = 4)
            }
            !uiState.errorMessage.isNullOrEmpty() && uiState.movies.isEmpty() -> {
                ErrorStateView(
                    message = uiState.errorMessage,
                    onRetry = onRetry
                )
            }
            uiState.movies.isEmpty() -> {
                EmptyStateView(
                    icon = Icons.Default.Movie,
                    title = "No Movie Trailers Found",
                    description = "We couldn't retrieve movie trailers at this time.",
                    actionLabel = "Retry",
                    onAction = onRetry
                )
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.movies, key = { it.id }) { movie ->
                        MovieCard(
                            movie = movie,
                            onWatchTrailer = { onWatchTrailer(movie) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MovieCard(
    movie: MovieItem,
    onWatchTrailer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fluxColors = LocalFluxColors.current

    FluxGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("movie_card_${movie.id}"),
        onClick = onWatchTrailer
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Poster / Backdrop with play button overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(fluxColors.surfaceElevated)
            ) {
                AsyncImage(
                    model = movie.thumbnailUrl,
                    contentDescription = movie.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Play icon overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play Trailer",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Duration badge
                if (!movie.duration.isNullOrEmpty()) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.75f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                    ) {
                        Text(
                            text = movie.duration,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = movie.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Studio / Year
            val subText = listOfNotNull(movie.channelTitle, movie.year).joinToString(" • ")
            if (subText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subText,
                    style = MaterialTheme.typography.bodySmall,
                    color = fluxColors.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onWatchTrailer,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = fluxColors.surfaceHigh,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .testTag("watch_trailer_btn_${movie.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Watch Trailer", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
