package com.flux.android.data.repository

import com.flux.android.core.FluxAnalytics
import com.flux.android.core.Resource
import com.flux.android.data.api.FluxApi
import com.flux.android.data.dto.MediaListResponse
import com.flux.android.data.dto.RawMediaItemDto
import com.flux.android.data.dto.WishlistItemDto
import com.flux.android.data.dto.WishlistRequest
import com.flux.android.data.dto.WishlistResponse
import com.flux.android.domain.model.GenreItem
import com.flux.android.domain.model.MovieItem
import com.flux.android.domain.model.MusicTrack
import com.flux.android.domain.model.WishlistItem
import com.flux.android.domain.repository.FluxRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

class FluxRepositoryImpl(
    private val api: FluxApi,
    private val moshi: Moshi,
    private val analytics: FluxAnalytics
) : FluxRepository {

    private val mediaListType = Types.newParameterizedType(List::class.java, RawMediaItemDto::class.java)
    private val mediaListAdapter = moshi.adapter<List<RawMediaItemDto>>(mediaListType)
    private val mediaResponseAdapter = moshi.adapter(MediaListResponse::class.java)

    private val wishlistListType = Types.newParameterizedType(List::class.java, WishlistItemDto::class.java)
    private val wishlistListAdapter = moshi.adapter<List<WishlistItemDto>>(wishlistListType)
    private val wishlistResponseAdapter = moshi.adapter(WishlistResponse::class.java)

    // In-memory cache for snappy navigation & offline resilience
    private val memoryCache = ConcurrentHashMap<String, Any>()
    private val cachedWishlist = mutableListOf<WishlistItem>()

    private val genres = listOf(
        GenreItem("pop", "Pop", "pop", "Upbeat chart-toppers and vocal hooks"),
        GenreItem("rock", "Rock", "rock", "Electric riffs, indie anthems and alternative rock"),
        GenreItem("hiphop", "Hip-Hop", "hip hop", "Heavy 808s, lyrical flows and trap beats"),
        GenreItem("rnb", "R&B", "r&b", "Soulful grooves, smooth vocals and modern rhythm"),
        GenreItem("electronic", "Electronic", "electronic", "Synthwave, house, EDM and club vibrations"),
        GenreItem("classical", "Classical", "classical", "Orchestral masterpieces, piano and strings"),
        GenreItem("jazz", "Jazz", "jazz", "Bebop, smooth brass and improvisational rhythm"),
        GenreItem("country", "Country", "country", "Acoustic storytelling, folk banjo and Americana"),
        GenreItem("blues", "Blues", "blues", "Delta guitar, heartfelt soul and timeless blues"),
        GenreItem("reggae", "Reggae", "reggae", "Roots, dub basslines and island rhythms"),
        GenreItem("metal", "Metal", "metal", "Heavy distortion, double bass and raw energy"),
        GenreItem("folk", "Folk", "folk", "Harmonies, fingerpicking acoustic guitars and tales"),
        GenreItem("indie", "Indie", "indie", "Lo-fi textures, bedroom pop and alternative sounds"),
        GenreItem("latin", "Latin", "latin", "Reggaeton, salsa, bachata and fiery percussion"),
        GenreItem("soul", "Soul", "soul", "Vintage Motown, gospel depth and neo-soul grooves"),
        GenreItem("ambient", "Ambient", "ambient", "Atmospheric soundscapes, meditation and focus drone"),
        GenreItem("bollywood", "Bollywood", "bollywood", "Indian melodies, energetic bhangra and film soundtracks"),
        GenreItem("kpop", "K-Pop", "k-pop", "Korean pop hits, dynamic choreo anthems and vibrant visuals")
    )

    override fun getAvailableGenres(): List<GenreItem> = genres

    override suspend fun getTrending(region: String): Resource<List<MusicTrack>> = withContext(Dispatchers.IO) {
        val cacheKey = "trending_$region"
        try {
            val response = api.getTrending(region)
            if (response.isSuccessful) {
                val rawBody = response.body()?.string() ?: ""
                val items = parseMediaItems(rawBody)
                val tracks = items.map { it.toMusicTrack(forceVideo = region == "US") }
                if (tracks.isNotEmpty()) {
                    memoryCache[cacheKey] = tracks
                    Resource.Success(tracks)
                } else {
                    fallbackTrending(region)
                }
            } else {
                getCachedOrFallbackTrending(region, "Trending music unavailable (code ${response.code()})")
            }
        } catch (e: IOException) {
            getCachedOrFallbackTrending(region, "Offline: showing cached trending")
        } catch (e: Exception) {
            getCachedOrFallbackTrending(region, "Error loading trending: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    override suspend fun searchMusic(query: String, pageToken: String?): Resource<Pair<List<MusicTrack>, String?>> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            return@withContext Resource.Success(Pair(emptyList(), null))
        }
        analytics.search(trimmed)
        try {
            val response = api.searchMusic(trimmed, pageToken)
            if (response.isSuccessful) {
                val rawBody = response.body()?.string() ?: ""
                val (items, nextToken) = parseMediaItemsWithToken(rawBody)
                val tracks = items.map { it.toMusicTrack() }
                Resource.Success(Pair(tracks, nextToken))
            } else {
                Resource.Error("Search failed with code ${response.code()}", code = response.code())
            }
        } catch (e: IOException) {
            Resource.Error("Network error: Search request failed.", cause = e)
        } catch (e: Exception) {
            Resource.Error("Search error: ${e.localizedMessage ?: "Unknown error"}", cause = e)
        }
    }

    override suspend fun getGenreMusic(genreType: String): Resource<List<MusicTrack>> = withContext(Dispatchers.IO) {
        analytics.genreSelected(genreType)
        val cacheKey = "genre_$genreType"
        try {
            val response = api.getGenreMusic(genreType)
            if (response.isSuccessful) {
                val rawBody = response.body()?.string() ?: ""
                val items = parseMediaItems(rawBody)
                val tracks = items.map { it.toMusicTrack() }
                if (tracks.isNotEmpty()) {
                    memoryCache[cacheKey] = tracks
                    Resource.Success(tracks)
                } else {
                    fallbackGenre(genreType)
                }
            } else {
                getCachedOrFallbackGenre(genreType, "Genre results unavailable (code ${response.code()})")
            }
        } catch (e: IOException) {
            getCachedOrFallbackGenre(genreType, "Offline: showing cached genre tracks")
        } catch (e: Exception) {
            getCachedOrFallbackGenre(genreType, "Error loading genre: ${e.localizedMessage ?: "Unknown"}")
        }
    }

    override suspend fun getMovies(): Resource<List<MovieItem>> = withContext(Dispatchers.IO) {
        val cacheKey = "movies"
        try {
            val response = api.getMovies()
            if (response.isSuccessful) {
                val rawBody = response.body()?.string() ?: ""
                val items = parseMediaItems(rawBody)
                val movies = items.map { it.toMovieItem() }
                if (movies.isNotEmpty()) {
                    memoryCache[cacheKey] = movies
                    Resource.Success(movies)
                } else {
                    fallbackMovies()
                }
            } else {
                getCachedOrFallbackMovies("Movies unavailable (code ${response.code()})")
            }
        } catch (e: IOException) {
            getCachedOrFallbackMovies("Offline: showing cached movie trailers")
        } catch (e: Exception) {
            getCachedOrFallbackMovies("Error loading movies: ${e.localizedMessage ?: "Unknown"}")
        }
    }

    override suspend fun getWishlist(): Resource<List<WishlistItem>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getWishlist()
            if (response.isSuccessful) {
                val rawBody = response.body()?.string() ?: ""
                val items = parseWishlistItems(rawBody)
                val domainItems = items.map { it.toDomain() }
                synchronized(cachedWishlist) {
                    cachedWishlist.clear()
                    cachedWishlist.addAll(domainItems)
                }
                Resource.Success(domainItems)
            } else {
                if (response.code() == 401) {
                    Resource.Error("Sign in to access and sync your wishlist across devices.", code = 401)
                } else {
                    synchronized(cachedWishlist) {
                        if (cachedWishlist.isNotEmpty()) {
                            Resource.Success(cachedWishlist.toList())
                        } else {
                            Resource.Error("Could not retrieve wishlist (code ${response.code()})", code = response.code())
                        }
                    }
                }
            }
        } catch (e: IOException) {
            synchronized(cachedWishlist) {
                if (cachedWishlist.isNotEmpty()) {
                    Resource.Success(cachedWishlist.toList())
                } else {
                    Resource.Error("Offline: Saved tracks will appear once reconnected.", cause = e)
                }
            }
        } catch (e: Exception) {
            Resource.Error("Failed to fetch wishlist: ${e.localizedMessage ?: "Unknown"}", cause = e)
        }
    }

    override suspend fun addToWishlist(track: MusicTrack): Resource<WishlistItem> = withContext(Dispatchers.IO) {
        analytics.wishlistAdd(track.id, track.title)
        val optimisticItem = WishlistItem(
            id = track.id,
            videoId = track.id,
            title = track.title,
            artist = track.artist,
            thumbnailUrl = track.thumbnailUrl,
            duration = track.duration
        )

        // Optimistic update
        synchronized(cachedWishlist) {
            if (cachedWishlist.none { it.videoId == track.id }) {
                cachedWishlist.add(0, optimisticItem)
            }
        }

        try {
            val request = WishlistRequest(
                videoId = track.id,
                title = track.title,
                thumbnailUrl = track.thumbnailUrl,
                channelTitle = track.artist,
                duration = track.duration
            )
            val response = api.addWishlist(request)
            if (response.isSuccessful) {
                Resource.Success(optimisticItem)
            } else {
                if (response.code() == 401) {
                    // Rollback optimistic state
                    synchronized(cachedWishlist) {
                        cachedWishlist.removeAll { it.videoId == track.id }
                    }
                    Resource.Error("Please sign in to save tracks to your wishlist.", code = 401)
                } else {
                    // Rollback optimistic update on network/API failure
                    synchronized(cachedWishlist) {
                        cachedWishlist.removeAll { it.videoId == track.id }
                    }
                    Resource.Error("Failed to save track (code ${response.code()})", code = response.code())
                }
            }
        } catch (e: Exception) {
            // Rollback optimistic update
            synchronized(cachedWishlist) {
                cachedWishlist.removeAll { it.videoId == track.id }
            }
            Resource.Error("Network error: Could not save track.", cause = e)
        }
    }

    override suspend fun removeFromWishlist(idOrVideoId: String): Resource<Unit> = withContext(Dispatchers.IO) {
        analytics.wishlistRemove(idOrVideoId)
        val removedBackup: WishlistItem?
        synchronized(cachedWishlist) {
            removedBackup = cachedWishlist.find { it.id == idOrVideoId || it.videoId == idOrVideoId }
            cachedWishlist.removeAll { it.id == idOrVideoId || it.videoId == idOrVideoId }
        }

        try {
            val response = api.deleteWishlist(idOrVideoId)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                // Roll back
                removedBackup?.let { backup ->
                    synchronized(cachedWishlist) {
                        cachedWishlist.add(backup)
                    }
                }
                Resource.Error("Failed to remove track (code ${response.code()})", code = response.code())
            }
        } catch (e: Exception) {
            // Roll back
            removedBackup?.let { backup ->
                synchronized(cachedWishlist) {
                    cachedWishlist.add(backup)
                }
            }
            Resource.Error("Network error: Could not remove track.", cause = e)
        }
    }

    override suspend fun getStreamUrl(videoId: String): Resource<String?> = withContext(Dispatchers.IO) {
        try {
            val response = api.getStream(videoId)
            if (response.isSuccessful) {
                val streamResponse = response.body()
                val url = streamResponse?.resolvedUrl()
                Resource.Success(url)
            } else {
                Resource.Success(null)
            }
        } catch (e: Exception) {
            Resource.Success(null)
        }
    }

    // Helper parsing methods that gracefully handle JSON arrays vs wrapped JSON objects
    private fun parseMediaItems(json: String): List<RawMediaItemDto> {
        val trimmed = json.trim()
        if (trimmed.isEmpty()) return emptyList()
        return try {
            if (trimmed.startsWith("[")) {
                mediaListAdapter.fromJson(trimmed) ?: emptyList()
            } else {
                mediaResponseAdapter.fromJson(trimmed)?.allItems() ?: emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun parseMediaItemsWithToken(json: String): Pair<List<RawMediaItemDto>, String?> {
        val trimmed = json.trim()
        if (trimmed.isEmpty()) return Pair(emptyList(), null)
        return try {
            if (trimmed.startsWith("[")) {
                Pair(mediaListAdapter.fromJson(trimmed) ?: emptyList(), null)
            } else {
                val response = mediaResponseAdapter.fromJson(trimmed)
                Pair(response?.allItems() ?: emptyList(), response?.nextPageToken)
            }
        } catch (_: Exception) {
            Pair(emptyList(), null)
        }
    }

    private fun parseWishlistItems(json: String): List<WishlistItemDto> {
        val trimmed = json.trim()
        if (trimmed.isEmpty()) return emptyList()
        return try {
            if (trimmed.startsWith("[")) {
                wishlistListAdapter.fromJson(trimmed) ?: emptyList()
            } else {
                wishlistResponseAdapter.fromJson(trimmed)?.allItems() ?: emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun getCachedOrFallbackTrending(region: String, errorMessage: String): Resource<List<MusicTrack>> {
        val cached = memoryCache["trending_$region"] as? List<MusicTrack>
        return if (!cached.isNullOrEmpty()) {
            Resource.Success(cached)
        } else {
            fallbackTrending(region)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun getCachedOrFallbackGenre(genre: String, errorMessage: String): Resource<List<MusicTrack>> {
        val cached = memoryCache["genre_$genre"] as? List<MusicTrack>
        return if (!cached.isNullOrEmpty()) {
            Resource.Success(cached)
        } else {
            fallbackGenre(genre)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun getCachedOrFallbackMovies(errorMessage: String): Resource<List<MovieItem>> {
        val cached = memoryCache["movies"] as? List<MovieItem>
        return if (!cached.isNullOrEmpty()) {
            Resource.Success(cached)
        } else {
            fallbackMovies()
        }
    }

    // Curated high-fidelity fallback items for when the live backend is unreachable / starting up
    private fun fallbackTrending(region: String): Resource<List<MusicTrack>> {
        val isVideo = region == "US"
        val tracks = if (region == "IN") {
            listOf(
                MusicTrack("k4yXQkG2s1E", "Tauba Tauba", "Karan Aujla, Badshah", "https://i.ytimg.com/vi/k4yXQkG2s1E/hqdefault.jpg", "3:28"),
                MusicTrack("kJQP7kiw5Fk", "Despacito (Indian Remix)", "Luis Fonsi, Badshah", "https://i.ytimg.com/vi/kJQP7kiw5Fk/hqdefault.jpg", "3:47"),
                MusicTrack("7zp1TbLFPXA", "Chaleya", "Arijit Singh, Anirudh", "https://i.ytimg.com/vi/7zp1TbLFPXA/hqdefault.jpg", "3:20"),
                MusicTrack("G_r36oK0YgQ", "Illuminati", "Sushin Shyam, Dabzee", "https://i.ytimg.com/vi/G_r36oK0YgQ/hqdefault.jpg", "2:46"),
                MusicTrack("V1Pl8CzNzCw", "O Mahi", "Pritam, Arijit Singh", "https://i.ytimg.com/vi/V1Pl8CzNzCw/hqdefault.jpg", "3:53"),
                MusicTrack("2g811Eo7K8U", "Husn", "Anuv Jain", "https://i.ytimg.com/vi/2g811Eo7K8U/hqdefault.jpg", "3:38"),
                MusicTrack("5GL9JoH4Sws", "Soulmate", "Badshah, Arijit Singh", "https://i.ytimg.com/vi/5GL9JoH4Sws/hqdefault.jpg", "3:34"),
                MusicTrack("JFcgOboQZ08", "Dheere Dheere", "Yo Yo Honey Singh", "https://i.ytimg.com/vi/JFcgOboQZ08/hqdefault.jpg", "3:32")
            )
        } else {
            listOf(
                MusicTrack("JGwWNGJdvx8", "Shape of You", "Ed Sheeran", "https://i.ytimg.com/vi/JGwWNGJdvx8/hqdefault.jpg", "3:53", isVideo = true),
                MusicTrack("fJ9rUzIMcZQ", "Bohemian Rhapsody", "Queen", "https://i.ytimg.com/vi/fJ9rUzIMcZQ/hqdefault.jpg", "5:55", isVideo = true),
                MusicTrack("hT_nvWreIhg", "Counting Stars", "OneRepublic", "https://i.ytimg.com/vi/hT_nvWreIhg/hqdefault.jpg", "4:17", isVideo = true),
                MusicTrack("09R8_2nJtjg", "Sugar", "Maroon 5", "https://i.ytimg.com/vi/09R8_2nJtjg/hqdefault.jpg", "3:55", isVideo = true),
                MusicTrack("OPf0YbXqDm0", "Uptown Funk", "Mark Ronson ft. Bruno Mars", "https://i.ytimg.com/vi/OPf0YbXqDm0/hqdefault.jpg", "4:30", isVideo = true),
                MusicTrack("YQHsXMglC9A", "Hello", "Adele", "https://i.ytimg.com/vi/YQHsXMglC9A/hqdefault.jpg", "4:55", isVideo = true),
                MusicTrack("CevxZvSJLk8", "Roar", "Katy Perry", "https://i.ytimg.com/vi/CevxZvSJLk8/hqdefault.jpg", "3:42", isVideo = true),
                MusicTrack("450p7goxZqg", "All of Me", "John Legend", "https://i.ytimg.com/vi/450p7goxZqg/hqdefault.jpg", "4:29", isVideo = true)
            )
        }
        memoryCache["trending_$region"] = tracks
        return Resource.Success(tracks)
    }

    private fun fallbackGenre(genre: String): Resource<List<MusicTrack>> {
        val tracks = listOf(
            MusicTrack("dQw4w9WgXcQ", "Never Gonna Give You Up", "Rick Astley", "https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg", "3:32"),
            MusicTrack("9bZkp7q19f0", "Gangnam Style", "PSY", "https://i.ytimg.com/vi/9bZkp7q19f0/hqdefault.jpg", "4:12"),
            MusicTrack("kXYiU_JCYtU", "Numb", "Linkin Park", "https://i.ytimg.com/vi/kXYiU_JCYtU/hqdefault.jpg", "3:07"),
            MusicTrack("fLexgOxsZu0", "Bruno Mars - That's What I Like", "Bruno Mars", "https://i.ytimg.com/vi/fLexgOxsZu0/hqdefault.jpg", "3:26"),
            MusicTrack("L_jWHffIx5E", "Smells Like Teen Spirit", "Nirvana", "https://i.ytimg.com/vi/L_jWHffIx5E/hqdefault.jpg", "4:38"),
            MusicTrack("kJQP7kiw5Fk", "Despacito", "Luis Fonsi ft. Daddy Yankee", "https://i.ytimg.com/vi/kJQP7kiw5Fk/hqdefault.jpg", "3:48")
        )
        return Resource.Success(tracks)
    }

    private fun fallbackMovies(): Resource<List<MovieItem>> {
        val movies = listOf(
            MovieItem("TcMBFSGVi1c", "Avengers: Endgame - Official Trailer", "https://i.ytimg.com/vi/TcMBFSGVi1c/hqdefault.jpg", "Marvel Studios", "2:26", "2019"),
            MovieItem("EXeTwQWrcwY", "The Dark Knight - Official Trailer", "https://i.ytimg.com/vi/EXeTwQWrcwY/hqdefault.jpg", "Warner Bros.", "2:30", "2008"),
            MovieItem("zSWdZVtXT7E", "Interstellar - Official Trailer", "https://i.ytimg.com/vi/zSWdZVtXT7E/hqdefault.jpg", "Paramount Pictures", "2:32", "2014"),
            MovieItem("Way9Dexny3w", "Dune: Part Two - Official Trailer", "https://i.ytimg.com/vi/Way9Dexny3w/hqdefault.jpg", "Warner Bros.", "3:01", "2024"),
            MovieItem("uYPbbksJxIg", "Oppenheimer - Official Trailer", "https://i.ytimg.com/vi/uYPbbksJxIg/hqdefault.jpg", "Universal Pictures", "3:06", "2023"),
            MovieItem("pBk4NYhWNMM", "Spider-Man: Across the Spider-Verse", "https://i.ytimg.com/vi/pBk4NYhWNMM/hqdefault.jpg", "Sony Pictures", "2:24", "2023")
        )
        return Resource.Success(movies)
    }
}
