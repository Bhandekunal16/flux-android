package com.flux.android.data.api

import com.flux.android.data.dto.AuthRequest
import com.flux.android.data.dto.AuthResponse
import com.flux.android.data.dto.StreamResponse
import com.flux.android.data.dto.WishlistRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FluxApi {

    @POST("api/auth")
    suspend fun authenticate(
        @Body request: AuthRequest
    ): Response<AuthResponse>

    @GET("api/music/trending")
    suspend fun getTrending(
        @Query("region") region: String
    ): Response<ResponseBody>

    @GET("api/music/search")
    suspend fun searchMusic(
        @Query("q") query: String,
        @Query("pageToken") pageToken: String? = null
    ): Response<ResponseBody>

    @GET("api/music/genre")
    suspend fun getGenreMusic(
        @Query("type") genre: String
    ): Response<ResponseBody>

    @GET("api/movies")
    suspend fun getMovies(): Response<ResponseBody>

    @GET("api/wishlist")
    suspend fun getWishlist(): Response<ResponseBody>

    @POST("api/wishlist")
    suspend fun addWishlist(
        @Body request: WishlistRequest
    ): Response<ResponseBody>

    @DELETE("api/wishlist/{id}")
    suspend fun deleteWishlist(
        @Path("id") videoId: String
    ): Response<ResponseBody>

    @GET("api/video/{id}")
    suspend fun getVideo(
        @Path("id") videoId: String
    ): Response<ResponseBody>

    @GET("api/stream")
    suspend fun getStream(
        @Query("id") videoId: String
    ): Response<StreamResponse>

    @GET("api/openapi")
    suspend fun getOpenApi(): Response<ResponseBody>
}
