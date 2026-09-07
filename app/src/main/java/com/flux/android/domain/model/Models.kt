package com.flux.android.domain.model

data class MusicTrack(
    val id: String,
    val title: String,
    val artist: String,
    val thumbnailUrl: String,
    val duration: String? = null,
    val durationMs: Long? = null,
    val streamUrl: String? = null,
    val isVideo: Boolean = false,
    val isWishlisted: Boolean = false
)

data class MovieItem(
    val id: String,
    val title: String,
    val thumbnailUrl: String,
    val channelTitle: String? = null,
    val duration: String? = null,
    val year: String? = null
)

data class GenreItem(
    val id: String,
    val name: String,
    val queryType: String,
    val description: String = ""
)

data class WishlistItem(
    val id: String,
    val videoId: String,
    val title: String,
    val artist: String,
    val thumbnailUrl: String,
    val duration: String? = null
) {
    fun toMusicTrack(): MusicTrack = MusicTrack(
        id = videoId,
        title = title,
        artist = artist,
        thumbnailUrl = thumbnailUrl,
        duration = duration,
        isWishlisted = true
    )
}

data class UserSession(
    val email: String,
    val token: String,
    val expiresAt: Long = 0L
)
