package com.flux.android.domain.repository

import com.flux.android.core.Resource
import com.flux.android.domain.model.GenreItem
import com.flux.android.domain.model.MovieItem
import com.flux.android.domain.model.MusicTrack
import com.flux.android.domain.model.WishlistItem

interface FluxRepository {
    suspend fun getTrending(region: String = "IN"): Resource<List<MusicTrack>>

    suspend fun searchMusic(
        query: String,
        pageToken: String? = null,
    ): Resource<Pair<List<MusicTrack>, String?>>

    suspend fun getGenreMusic(genreType: String): Resource<List<MusicTrack>>

    suspend fun getMovies(): Resource<List<MovieItem>>

    suspend fun getWishlist(): Resource<List<WishlistItem>>

    suspend fun addToWishlist(track: MusicTrack): Resource<WishlistItem>

    suspend fun removeFromWishlist(idOrVideoId: String): Resource<Unit>

    suspend fun getStreamUrl(videoId: String): Resource<String?>

    fun getAvailableGenres(): List<GenreItem>
}
