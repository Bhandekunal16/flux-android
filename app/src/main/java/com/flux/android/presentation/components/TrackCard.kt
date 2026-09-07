package com.flux.android.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.flux.android.domain.model.MusicTrack
import com.flux.android.presentation.theme.LocalFluxColors

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
        Column(modifier = Modifier.padding(10.dp)) {
            // Thumbnail container with duration badge and wishlist button
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

                // Duration badge
                if (!track.duration.isNullOrEmpty()) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.75f),
                        shape = RoundedCornerShape(6.dp),
                        modifier =
                            Modifier
                                .align(Alignment.BottomEnd)
                                .padding(6.dp),
                    ) {
                        Text(
                            text = track.duration,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }

                // Wishlist heart button
                IconButton(
                    onClick = onWishlistToggle,
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(34.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .testTag("wishlist_btn_${track.id}"),
                ) {
                    Icon(
                        imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isWishlisted) "Remove from wishlist" else "Add to wishlist",
                        tint = if (isWishlisted) Color(0xFFFF2A6D) else Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title and Artist
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodySmall,
                color = fluxColors.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Listen & Watch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = onListen,
                    shape = RoundedCornerShape(14.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = fluxColors.surfaceHigh,
                            contentColor = MaterialTheme.colorScheme.primary,
                        ),
                    modifier =
                        Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("listen_btn_${track.id}"),
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Listen",
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Listen", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                if (onWatch != null) {
                    Button(
                        onClick = onWatch,
                        shape = RoundedCornerShape(14.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = fluxColors.surfaceHigh.copy(alpha = 0.6f),
                                contentColor = MaterialTheme.colorScheme.secondary,
                            ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, fluxColors.border),
                        modifier =
                            Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("watch_btn_${track.id}"),
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartDisplay,
                            contentDescription = "Watch",
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Watch", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
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
                            .size(38.dp)
                            .testTag("watch_list_btn_${track.id}"),
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartDisplay,
                        contentDescription = "Watch video",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            IconButton(
                onClick = onWishlistToggle,
                modifier =
                    Modifier
                        .size(38.dp)
                        .testTag("wishlist_list_btn_${track.id}"),
            ) {
                Icon(
                    imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Wishlist",
                    tint = if (isWishlisted) Color(0xFFFF2A6D) else fluxColors.textMuted,
                    modifier = Modifier.size(20.dp),
                )
            }

            IconButton(
                onClick = onListen,
                modifier =
                    Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(fluxColors.surfaceHigh)
                        .testTag("play_list_btn_${track.id}"),
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}
