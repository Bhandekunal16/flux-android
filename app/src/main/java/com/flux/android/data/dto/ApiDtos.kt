package com.flux.android.data.dto

import com.flux.android.domain.model.MovieItem
import com.flux.android.domain.model.MusicTrack
import com.flux.android.domain.model.WishlistItem
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AuthRequest(
    @Json(name = "email") val email: String,
)

@JsonClass(generateAdapter = true)
data class AuthResponse(
    @Json(name = "token") val token: String? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "user") val user: UserDto? = null,
)

@JsonClass(generateAdapter = true)
data class UserDto(
    @Json(name = "email") val email: String? = null,
    @Json(name = "id") val id: String? = null,
)

@JsonClass(generateAdapter = true)
data class ThumbnailInfo(
    @Json(name = "url") val url: String? = null,
    @Json(name = "width") val width: Int? = null,
    @Json(name = "height") val height: Int? = null,
)

@JsonClass(generateAdapter = true)
data class ThumbnailsDto(
    @Json(name = "default") val default: ThumbnailInfo? = null,
    @Json(name = "medium") val medium: ThumbnailInfo? = null,
    @Json(name = "high") val high: ThumbnailInfo? = null,
    @Json(name = "standard") val standard: ThumbnailInfo? = null,
    @Json(name = "maxres") val maxres: ThumbnailInfo? = null,
) {
    fun bestUrl(): String? = maxres?.url ?: high?.url ?: medium?.url ?: default?.url ?: standard?.url
}

@JsonClass(generateAdapter = true)
data class SnippetDto(
    @Json(name = "title") val title: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "channelTitle") val channelTitle: String? = null,
    @Json(name = "publishedAt") val publishedAt: String? = null,
    @Json(name = "thumbnails") val thumbnails: ThumbnailsDto? = null,
)

@JsonClass(generateAdapter = true)
data class ContentDetailsDto(
    @Json(name = "duration") val duration: String? = null,
)

@JsonClass(generateAdapter = true)
data class VideoIdDto(
    @Json(name = "videoId") val videoId: String? = null,
)

@JsonClass(generateAdapter = true)
data class RawMediaItemDto(
    // Flat properties
    @Json(name = "id") val idRaw: Any? = null,
    @Json(name = "videoId") val videoId: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "channelTitle") val channelTitle: String? = null,
    @Json(name = "artist") val artist: String? = null,
    @Json(name = "author") val author: String? = null,
    @Json(name = "thumbnail") val thumbnail: String? = null,
    @Json(name = "thumbnailUrl") val thumbnailUrl: String? = null,
    @Json(name = "duration") val duration: String? = null,
    @Json(name = "durationText") val durationText: String? = null,
    @Json(name = "streamUrl") val streamUrl: String? = null,
    @Json(name = "url") val url: String? = null,
    @Json(name = "isVideo") val isVideo: Boolean? = null,
    // YouTube API nested properties
    @Json(name = "snippet") val snippet: SnippetDto? = null,
    @Json(name = "contentDetails") val contentDetails: ContentDetailsDto? = null,
) {
    fun extractId(): String {
        videoId?.let { if (it.isNotEmpty()) return it }
        when (idRaw) {
            is String -> {
                if (idRaw.isNotEmpty()) return idRaw
            }

            is Map<*, *> -> {
                val vid = idRaw["videoId"] as? String
                if (!vid.isNullOrEmpty()) return vid
            }
        }
        return ""
    }

    fun extractTitle(): String = title ?: name ?: snippet?.title ?: "Unknown Title"

    fun extractArtist(): String = channelTitle ?: artist ?: author ?: snippet?.channelTitle ?: "Flux Artist"

    fun extractThumbnail(): String {
        thumbnailUrl?.let { if (it.isNotEmpty()) return it }
        thumbnail?.let { if (it.isNotEmpty()) return it }
        snippet?.thumbnails?.bestUrl()?.let { if (it.isNotEmpty()) return it }
        val id = extractId()
        return if (id.isNotEmpty()) "https://i.ytimg.com/vi/$id/hqdefault.jpg" else ""
    }

    fun extractDuration(): String? = duration ?: durationText ?: contentDetails?.duration?.let { formatIsoDuration(it) }

    fun toMusicTrack(forceVideo: Boolean = false): MusicTrack {
        val resolvedId = extractId()
        return MusicTrack(
            id = resolvedId,
            title = extractTitle(),
            artist = extractArtist(),
            thumbnailUrl = extractThumbnail(),
            duration = extractDuration(),
            streamUrl = streamUrl ?: url,
            isVideo = forceVideo || (isVideo ?: false),
        )
    }

    fun toMovieItem(): MovieItem =
        MovieItem(
            id = extractId(),
            title = extractTitle(),
            thumbnailUrl = extractThumbnail(),
            channelTitle = extractArtist(),
            duration = extractDuration(),
        )

    private fun formatIsoDuration(iso: String): String =
        try {
            val clean = iso.removePrefix("PT")
            val hours = if (clean.contains("H")) clean.substringBefore("H").toIntOrNull() ?: 0 else 0
            val remainderAfterH = if (clean.contains("H")) clean.substringAfter("H") else clean
            val mins = if (remainderAfterH.contains("M")) remainderAfterH.substringBefore("M").toIntOrNull() ?: 0 else 0
            val remainderAfterM = if (remainderAfterH.contains("M")) remainderAfterH.substringAfter("M") else remainderAfterH
            val secs = if (remainderAfterM.contains("S")) remainderAfterM.substringBefore("S").toIntOrNull() ?: 0 else 0
            if (hours > 0) {
                String.format("%d:%02d:%02d", hours, mins, secs)
            } else {
                String.format("%d:%02d", mins, secs)
            }
        } catch (_: Exception) {
            iso
        }
}

@JsonClass(generateAdapter = true)
data class MediaListResponse(
    @Json(name = "items") val items: List<RawMediaItemDto>? = null,
    @Json(name = "results") val results: List<RawMediaItemDto>? = null,
    @Json(name = "data") val data: List<RawMediaItemDto>? = null,
    @Json(name = "nextPageToken") val nextPageToken: String? = null,
) {
    fun allItems(): List<RawMediaItemDto> = items ?: results ?: data ?: emptyList()
}

@JsonClass(generateAdapter = true)
data class StreamResponse(
    @Json(name = "url") val url: String? = null,
    @Json(name = "streamUrl") val streamUrl: String? = null,
    @Json(name = "audioUrl") val audioUrl: String? = null,
    @Json(name = "playableUrl") val playableUrl: String? = null,
) {
    fun resolvedUrl(): String? = streamUrl ?: url ?: audioUrl ?: playableUrl
}

@JsonClass(generateAdapter = true)
data class WishlistRequest(
    @Json(name = "videoId") val videoId: String,
    @Json(name = "title") val title: String,
    @Json(name = "thumbnailUrl") val thumbnailUrl: String,
    @Json(name = "channelTitle") val channelTitle: String? = null,
    @Json(name = "duration") val duration: String? = null,
)

@JsonClass(generateAdapter = true)
data class WishlistItemDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "_id") val mongoId: String? = null,
    @Json(name = "videoId") val videoId: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "artist") val artist: String? = null,
    @Json(name = "channelTitle") val channelTitle: String? = null,
    @Json(name = "thumbnailUrl") val thumbnailUrl: String? = null,
    @Json(name = "thumbnail") val thumbnail: String? = null,
    @Json(name = "duration") val duration: String? = null,
) {
    fun toDomain(): WishlistItem {
        val resolvedVideoId = videoId ?: id ?: ""
        return WishlistItem(
            id = mongoId ?: id ?: resolvedVideoId,
            videoId = resolvedVideoId,
            title = title ?: "Saved Track",
            artist = artist ?: channelTitle ?: "Flux Artist",
            thumbnailUrl =
                thumbnailUrl ?: thumbnail
                    ?: if (resolvedVideoId.isNotEmpty()) "https://i.ytimg.com/vi/$resolvedVideoId/hqdefault.jpg" else "",
            duration = duration,
        )
    }
}

@JsonClass(generateAdapter = true)
data class WishlistResponse(
    @Json(name = "items") val items: List<WishlistItemDto>? = null,
    @Json(name = "data") val data: List<WishlistItemDto>? = null,
    @Json(name = "wishlist") val wishlist: List<WishlistItemDto>? = null,
) {
    fun allItems(): List<WishlistItemDto> = items ?: data ?: wishlist ?: emptyList()
}
