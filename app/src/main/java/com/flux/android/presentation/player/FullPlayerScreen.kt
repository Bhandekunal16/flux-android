package com.flux.android.presentation.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.flux.android.data.player.FluxPlayerManager
import com.flux.android.data.player.PlayerUiState
import com.flux.android.data.player.RepeatOption
import com.flux.android.domain.model.MusicTrack
import com.flux.android.presentation.components.FluxGlassCard
import com.flux.android.presentation.theme.LocalFluxColors

@Composable
fun FullPlayerScreen(
    playerState: PlayerUiState,
    playerManager: FluxPlayerManager,
    isWishlisted: Boolean,
    onToggleWishlist: (MusicTrack) -> Unit,
    onWatchVideo: (MusicTrack) -> Unit,
    onDismiss: () -> Unit,
) {
    val track = playerState.currentTrack ?: return
    val fluxColors = LocalFluxColors.current
    var showQueue by remember { mutableStateOf(false) }

    var isDraggingSlider by remember { mutableStateOf(false) }
    var sliderProgress by remember { mutableFloatStateOf(0f) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    if (fluxColors.isDark) Color(0xFF2B2930) else Color(0xFFEADDFF),
                                    if (fluxColors.isDark) Color(0xFF1C1B1F) else Color(0xFFFEF7FF),
                                ),
                        ),
                    ).padding(horizontal = 24.dp, vertical = 20.dp)
                    .testTag("full_player_screen"),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier =
                            Modifier
                                .size(44.dp)
                                .testTag("full_player_collapse"),
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = "Collapse Player",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(32.dp),
                        )
                    }

                    Text(
                        text = "NOW PLAYING",
                        style = MaterialTheme.typography.labelMedium,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold,
                        color = fluxColors.textMuted,
                    )

                    IconButton(
                        onClick = { showQueue = !showQueue },
                        modifier =
                            Modifier
                                .size(44.dp)
                                .testTag("full_player_queue_toggle"),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                            contentDescription = "Queue",
                            tint = if (showQueue) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (showQueue) {
                    // Queue Panel
                    Text(
                        text = "Up Next (${playerState.queue.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                    )
                    LazyColumn(
                        modifier =
                            Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        itemsIndexed(playerState.queue) { index, queueTrack ->
                            val isCurrent = index == playerState.currentIndex
                            FluxGlassCard(
                                onClick = { playerManager.playTrack(queueTrack, playerState.queue) },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Row(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    AsyncImage(
                                        model = queueTrack.thumbnailUrl,
                                        contentDescription = queueTrack.title,
                                        modifier =
                                            Modifier
                                                .size(44.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop,
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = queueTrack.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            text = queueTrack.artist,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = fluxColors.textMuted,
                                            maxLines = 1,
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Artwork with rounded corners and neon glow
                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            shadowElevation = 16.dp,
                            modifier =
                                Modifier
                                    .fillMaxWidth(0.92f)
                                    .aspectRatio(1f),
                        ) {
                            AsyncImage(
                                model = track.thumbnailUrl,
                                contentDescription = track.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Track Title, Artist, Wishlist & Watch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = track.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = track.artist,
                                style = MaterialTheme.typography.titleMedium,
                                color = fluxColors.textMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Watch video button
                            IconButton(
                                onClick = { onWatchVideo(track) },
                                modifier =
                                    Modifier
                                        .size(44.dp)
                                        .testTag("full_player_watch_btn"),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SmartDisplay,
                                    contentDescription = "Watch Video",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(26.dp),
                                )
                            }

                            // Wishlist button
                            IconButton(
                                onClick = { onToggleWishlist(track) },
                                modifier =
                                    Modifier
                                        .size(44.dp)
                                        .testTag("full_player_wishlist_btn"),
                            ) {
                                Icon(
                                    imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Wishlist",
                                    tint = if (isWishlisted) Color(0xFFFF2A6D) else MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.size(26.dp),
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Seek bar & Timers
                    val currentPos =
                        if (isDraggingSlider) {
                            (sliderProgress * playerState.durationMs).toLong()
                        } else {
                            playerState.currentPositionMs
                        }
                    val effectiveFraction =
                        if (playerState.durationMs > 0) {
                            (currentPos.toFloat() / playerState.durationMs.toFloat()).coerceIn(0f, 1f)
                        } else {
                            0f
                        }

                    Slider(
                        value = if (isDraggingSlider) sliderProgress else effectiveFraction,
                        onValueChange = {
                            isDraggingSlider = true
                            sliderProgress = it
                        },
                        onValueChangeFinished = {
                            isDraggingSlider = false
                            val seekMs = (sliderProgress * playerState.durationMs).toLong()
                            playerManager.seekTo(seekMs)
                        },
                        colors =
                            SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = fluxColors.surfaceHigh,
                            ),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .testTag("full_player_slider"),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = formatTime(currentPos),
                            style = MaterialTheme.typography.bodySmall,
                            color = fluxColors.textMuted,
                        )
                        Text(
                            text = formatTime(playerState.durationMs),
                            style = MaterialTheme.typography.bodySmall,
                            color = fluxColors.textMuted,
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Playback Controls (Repeat, Prev, Play/Pause, Next)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Repeat button
                        IconButton(
                            onClick = { playerManager.toggleRepeat() },
                            modifier = Modifier.size(44.dp),
                        ) {
                            Icon(
                                imageVector =
                                    when (playerState.repeatOption) {
                                        RepeatOption.ONE -> Icons.Default.RepeatOne
                                        else -> Icons.Default.Repeat
                                    },
                                contentDescription = "Repeat",
                                tint =
                                    if (playerState.repeatOption != RepeatOption.OFF) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        fluxColors.textMuted
                                    },
                                modifier = Modifier.size(24.dp),
                            )
                        }

                        // Previous button
                        IconButton(
                            onClick = { playerManager.previous() },
                            modifier = Modifier.size(54.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "Previous",
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(36.dp),
                            )
                        }

                        // Play/Pause Big Center Button
                        Box(contentAlignment = Alignment.Center) {
                            if (playerState.isBuffering) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(72.dp),
                                    strokeWidth = 3.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                            IconButton(
                                onClick = { playerManager.togglePlayPause() },
                                modifier =
                                    Modifier
                                        .size(68.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                        .testTag("full_player_play_pause"),
                            ) {
                                Icon(
                                    imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                                    tint = fluxColors.onPrimaryText,
                                    modifier = Modifier.size(38.dp),
                                )
                            }
                        }

                        // Next button
                        IconButton(
                            onClick = { playerManager.next() },
                            modifier = Modifier.size(54.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Next",
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(36.dp),
                            )
                        }

                        // Placeholder for symmetry
                        Spacer(modifier = Modifier.size(44.dp))
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
