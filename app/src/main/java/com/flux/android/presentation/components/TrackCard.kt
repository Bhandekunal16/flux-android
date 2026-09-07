package com.flux.android.presentation.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.flux.android.domain.model.MusicTrack
import com.flux.android.presentation.theme.LocalFluxColors

/**
 * YouTube-style grid card matching YouTube/YouTube Music design language:
 * - 16:9 thumbnail with rounded corners and duration badge
 * - Channel avatar circle with artist initials
 * - 2-line title hierarchy preventing truncation
 * - Compact pill action buttons ("Listen" and "Watch") that fit cleanly without text clipping
 */
@Composable
fun TrackCardGrid(
    track: MusicTrack,
    isWishlisted: Boolean,
    onListen: () -> Unit,
    onWatch: (() -> Unit)? = null,
    onWishlistToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fluxColors = LocalFluxColors.current

    FluxGlassCard(
        modifier =
            modifier
                .fillMaxWidth()
                .testTag("track_card_grid_${track.id}"),
        onClick = onListen,
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // 16:9 YouTube Thumbnail container
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(fluxColors.surfaceElevated),
            ) {
                AsyncImage(
                    model = track.thumbnailUrl,
                    contentDescription = track.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )

                // YouTube-style duration timestamp pill (bottom-right)
                if (!track.duration.isNullOrEmpty()) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.82f),
                        shape = RoundedCornerShape(4.dp),
                        modifier =
                            Modifier
                                .align(Alignment.BottomEnd)
                                .padding(5.dp),
                    ) {
                        Text(
                            text = track.duration,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                        )
                    }
                }

                // Glassmorphic Wishlist heart button (top-right)
                IconButton(
                    onClick = onWishlistToggle,
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(32.dp)
                            .background(Color.Black.copy(alpha = 0.52f), CircleShape)
                            .testTag("wishlist_btn_${track.id}"),
                ) {
                    Icon(
                        imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isWishlisted) "Remove from wishlist" else "Add to wishlist",
                        tint = if (isWishlisted) Color(0xFFFF2A6D) else Color.White,
                        modifier = Modifier.size(17.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // YouTube details row: Channel Avatar + 2-line Title & Channel
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                // YouTube Channel Avatar Badge
                val initial = (track.artist.firstOrNull() ?: track.title.firstOrNull() ?: 'F').uppercaseChar().toString()
                Box(
                    modifier =
                        Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary,
                                    ),
                                ),
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = initial,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Title: 2 lines max to avoid truncation
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontSize = 12.5.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = track.artist,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = fluxColors.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // YouTube Action Pills: Listen & Watch (with compact padding so text is never truncated)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = onListen,
                    shape = RoundedCornerShape(16.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = fluxColors.surfaceHigh,
                            contentColor = MaterialTheme.colorScheme.primary,
                        ),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                    modifier =
                        Modifier
                            .weight(1f)
                            .height(34.dp)
                            .testTag("listen_btn_${track.id}"),
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Listen",
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Listen",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false,
                    )
                }

                if (onWatch != null) {
                    Button(
                        onClick = onWatch,
                        shape = RoundedCornerShape(16.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = fluxColors.surfaceHigh.copy(alpha = 0.65f),
                                contentColor = MaterialTheme.colorScheme.secondary,
                            ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, fluxColors.border),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                        modifier =
                            Modifier
                                .weight(1f)
                                .height(34.dp)
                                .testTag("watch_btn_${track.id}"),
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartDisplay,
                            contentDescription = "Watch",
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Watch",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Full YouTube Feed Video Card:
 * Modeled after YouTube's full-width feed cards with prominent 16:9 thumbnail,
 * channel avatar, 2-line title, duration badge, and comprehensive action bar.
 */
@Composable
fun YouTubeVideoCard(
    track: MusicTrack,
    isWishlisted: Boolean,
    onListen: () -> Unit,
    onWatch: (() -> Unit)? = null,
    onWishlistToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fluxColors = LocalFluxColors.current
    val context = LocalContext.current

    FluxGlassCard(
        modifier =
            modifier
                .fillMaxWidth()
                .testTag("youtube_video_card_${track.id}"),
        onClick = { onWatch?.invoke() ?: onListen() },
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Full-width 16:9 thumbnail
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(fluxColors.surfaceElevated),
            ) {
                AsyncImage(
                    model = track.thumbnailUrl,
                    contentDescription = track.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )

                // YouTube duration badge
                if (!track.duration.isNullOrEmpty()) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(4.dp),
                        modifier =
                            Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp),
                    ) {
                        Text(
                            text = track.duration,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }

                // Quick Play Video overlay button
                Surface(
                    color = Color.Black.copy(alpha = 0.45f),
                    shape = CircleShape,
                    modifier =
                        Modifier
                            .align(Alignment.Center)
                            .size(52.dp)
                            .clickable { onWatch?.invoke() ?: onListen() },
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play Video",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp),
                        )
                    }
                }

                // Wishlist heart button
                IconButton(
                    onClick = onWishlistToggle,
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(36.dp)
                            .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                            .testTag("youtube_wishlist_btn_${track.id}"),
                ) {
                    Icon(
                        imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isWishlisted) "Remove from wishlist" else "Add to wishlist",
                        tint = if (isWishlisted) Color(0xFFFF2A6D) else Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            // Video Details Row
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                // Channel Avatar Circle
                val initial = (track.artist.firstOrNull() ?: track.title.firstOrNull() ?: 'Y').uppercaseChar().toString()
                Box(
                    modifier =
                        Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary,
                                    ),
                                ),
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = initial,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        lineHeight = 19.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "${track.artist} • ${track.duration ?: "Music Video"}",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp,
                        color = fluxColors.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // YouTube Action Chips
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = onListen,
                    shape = RoundedCornerShape(20.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = fluxColors.surfaceHigh,
                            contentColor = MaterialTheme.colorScheme.primary,
                        ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(36.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Listen",
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Listen", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                if (onWatch != null) {
                    Button(
                        onClick = onWatch,
                        shape = RoundedCornerShape(20.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = fluxColors.surfaceHigh.copy(alpha = 0.6f),
                                contentColor = MaterialTheme.colorScheme.secondary,
                            ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, fluxColors.border),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(36.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartDisplay,
                            contentDescription = "Watch",
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Watch", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = {
                        val shareIntent =
                            Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Check out ${track.title} by ${track.artist}: https://www.youtube.com/watch?v=${track.id}",
                                )
                            }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Track"))
                    },
                    modifier =
                        Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(fluxColors.surfaceHigh),
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = fluxColors.textMuted,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun TrackCardList(
    track: MusicTrack,
    isWishlisted: Boolean,
    onListen: () -> Unit,
    onWatch: (() -> Unit)? = null,
    onWishlistToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fluxColors = LocalFluxColors.current

    FluxGlassCard(
        modifier =
            modifier
                .fillMaxWidth()
                .testTag("track_card_list_${track.id}"),
        onClick = onListen,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Square Thumbnail with duration
            Box(
                modifier =
                    Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(fluxColors.surfaceElevated),
            ) {
                AsyncImage(
                    model = track.thumbnailUrl,
                    contentDescription = track.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )

                if (!track.duration.isNullOrEmpty()) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.75f),
                        shape = RoundedCornerShape(4.dp),
                        modifier =
                            Modifier
                                .align(Alignment.BottomEnd)
                                .padding(2.dp),
                    ) {
                        Text(
                            text = track.duration,
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title & Channel
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = fluxColors.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Actions: Watch (optional), Wishlist, Play
            if (onWatch != null) {
                IconButton(
                    onClick = onWatch,
                    modifier =
                        Modifier
                            .size(44.dp)
                            .testTag("watch_list_btn_${track.id}"),
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartDisplay,
                        contentDescription = "Watch video",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            IconButton(
                onClick = onWishlistToggle,
                modifier =
                    Modifier
                        .size(44.dp)
                        .testTag("wishlist_list_btn_${track.id}"),
            ) {
                Icon(
                    imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Wishlist",
                    tint = if (isWishlisted) Color(0xFFFF2A6D) else fluxColors.textMuted,
                    modifier = Modifier.size(22.dp),
                )
            }

            IconButton(
                onClick = onListen,
                modifier =
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(fluxColors.surfaceHigh)
                        .testTag("play_list_btn_${track.id}"),
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}
