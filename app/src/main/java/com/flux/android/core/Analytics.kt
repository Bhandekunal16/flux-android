package com.flux.android.core

import android.util.Log

interface AnalyticsTracker {
    fun trackEvent(
        name: String,
        params: Map<String, Any> = emptyMap(),
    )
}

class FluxAnalytics(
    private val trackers: List<AnalyticsTracker> = listOf(LogcatAnalyticsTracker()),
) {
    fun appOpen() = track("app_open")

    fun search(query: String) = track("search", mapOf("query" to query))

    fun searchResultClick(
        videoId: String,
        title: String,
    ) = track("search_result_click", mapOf("video_id" to videoId, "title" to title))

    fun trackPlay(
        trackId: String,
        title: String,
    ) = track("track_play", mapOf("track_id" to trackId, "title" to title))

    fun trackPause(trackId: String) = track("track_pause", mapOf("track_id" to trackId))

    fun trackComplete(trackId: String) = track("track_complete", mapOf("track_id" to trackId))

    fun videoPlay(
        videoId: String,
        title: String,
    ) = track("video_play", mapOf("video_id" to videoId, "title" to title))

    fun wishlistAdd(
        videoId: String,
        title: String,
    ) = track("wishlist_add", mapOf("video_id" to videoId, "title" to title))

    fun wishlistRemove(videoId: String) = track("wishlist_remove", mapOf("video_id" to videoId))

    fun genreSelected(genre: String) = track("genre_selected", mapOf("genre" to genre))

    fun movieOpen(
        movieId: String,
        title: String,
    ) = track("movie_open", mapOf("movie_id" to movieId, "title" to title))

    fun login(email: String) = track("login", mapOf("email_domain" to (email.substringAfter("@", "unknown"))))

    fun logout() = track("logout")

    private fun track(
        name: String,
        params: Map<String, Any> = emptyMap(),
    ) {
        trackers.forEach { it.trackEvent(name, params) }
    }
}

class LogcatAnalyticsTracker : AnalyticsTracker {
    override fun trackEvent(
        name: String,
        params: Map<String, Any>,
    ) {
        Log.d("FluxAnalytics", "Event: $name | Params: $params")
    }
}
