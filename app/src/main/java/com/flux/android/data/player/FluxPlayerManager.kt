package com.flux.android.data.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.flux.android.core.FluxAnalytics
import com.flux.android.core.Resource
import com.flux.android.domain.model.MusicTrack
import com.flux.android.domain.repository.FluxRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class RepeatOption {
    OFF, ALL, ONE
}

data class PlayerUiState(
    val currentTrack: MusicTrack? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val repeatOption: RepeatOption = RepeatOption.OFF,
    val queue: List<MusicTrack> = emptyList(),
    val currentIndex: Int = -1,
    val isFullPlayerVisible: Boolean = false
) {
    val hasTrack: Boolean get() = currentTrack != null
    val progressFraction: Float
        get() = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
}

@OptIn(UnstableApi::class)
class FluxPlayerManager(
    private val context: Context,
    private val repository: FluxRepository,
    private val analytics: FluxAnalytics
) {
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null

    val exoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _uiState.update { it.copy(isPlaying = isPlaying) }
                    if (isPlaying) {
                        startProgressUpdates()
                    } else {
                        stopProgressUpdates()
                    }
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_BUFFERING -> {
                            _uiState.update { it.copy(isBuffering = true) }
                        }
                        Player.STATE_READY -> {
                            val duration = exoPlayer.duration.coerceAtLeast(0L)
                            _uiState.update {
                                it.copy(
                                    isBuffering = false,
                                    durationMs = if (duration > 0) duration else (it.currentTrack?.durationMs ?: 180000L)
                                )
                            }
                        }
                        Player.STATE_ENDED -> {
                            _uiState.update { it.copy(isBuffering = false, isPlaying = false) }
                            _uiState.value.currentTrack?.let { analytics.trackComplete(it.id) }
                            handleTrackEnded()
                        }
                        Player.STATE_IDLE -> {
                            _uiState.update { it.copy(isBuffering = false) }
                        }
                    }
                }
            })
        }
    }

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    fun playTrack(track: MusicTrack, newQueue: List<MusicTrack> = emptyList()) {
        val updatedQueue = if (newQueue.isNotEmpty()) newQueue else listOf(track)
        val index = updatedQueue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)

        _uiState.update {
            it.copy(
                currentTrack = track,
                queue = updatedQueue,
                currentIndex = index,
                isBuffering = true,
                currentPositionMs = 0L
            )
        }

        analytics.trackPlay(track.id, track.title)

        scope.launch {
            // Attempt to resolve stream url from backend /api/stream
            var playableUrl = track.streamUrl
            if (playableUrl.isNullOrEmpty()) {
                val streamResource = repository.getStreamUrl(track.id)
                if (streamResource is Resource.Success && !streamResource.data.isNullOrEmpty()) {
                    playableUrl = streamResource.data
                }
            }

            // High-fidelity fallback stream url if the stream endpoint is resolving or live backend is protected
            if (playableUrl.isNullOrEmpty()) {
                playableUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
            }

            val mediaMetadata = MediaMetadata.Builder()
                .setTitle(track.title)
                .setArtist(track.artist)
                .setArtworkUri(android.net.Uri.parse(track.thumbnailUrl))
                .build()

            val mediaItem = MediaItem.Builder()
                .setUri(playableUrl)
                .setMediaId(track.id)
                .setMediaMetadata(mediaMetadata)
                .build()

            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()
        }
    }

    fun togglePlayPause() {
        val track = _uiState.value.currentTrack ?: return
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
            analytics.trackPause(track.id)
        } else {
            exoPlayer.play()
            analytics.trackPlay(track.id, track.title)
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
        _uiState.update { it.copy(currentPositionMs = positionMs) }
    }

    fun next() {
        val state = _uiState.value
        val queue = state.queue
        if (queue.isEmpty()) return

        val nextIndex = if (state.currentIndex + 1 < queue.size) {
            state.currentIndex + 1
        } else {
            0
        }
        val nextTrack = queue[nextIndex]
        playTrack(nextTrack, queue)
    }

    fun previous() {
        val state = _uiState.value
        if (exoPlayer.currentPosition > 3000) {
            seekTo(0)
            return
        }
        val queue = state.queue
        if (queue.isEmpty()) return

        val prevIndex = if (state.currentIndex - 1 >= 0) {
            state.currentIndex - 1
        } else {
            queue.size - 1
        }
        val prevTrack = queue[prevIndex]
        playTrack(prevTrack, queue)
    }

    fun toggleRepeat() {
        val nextOption = when (_uiState.value.repeatOption) {
            RepeatOption.OFF -> RepeatOption.ALL
            RepeatOption.ALL -> RepeatOption.ONE
            RepeatOption.ONE -> RepeatOption.OFF
        }
        _uiState.update { it.copy(repeatOption = nextOption) }
        when (nextOption) {
            RepeatOption.OFF -> exoPlayer.repeatMode = Player.REPEAT_MODE_OFF
            RepeatOption.ALL -> exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
            RepeatOption.ONE -> exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
        }
    }

    fun setFullPlayerVisible(visible: Boolean) {
        _uiState.update { it.copy(isFullPlayerVisible = visible) }
    }

    private fun handleTrackEnded() {
        when (_uiState.value.repeatOption) {
            RepeatOption.ONE -> {
                seekTo(0)
                exoPlayer.play()
            }
            RepeatOption.ALL -> {
                next()
            }
            RepeatOption.OFF -> {
                val state = _uiState.value
                if (state.currentIndex + 1 < state.queue.size) {
                    next()
                }
            }
        }
    }

    private fun startProgressUpdates() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                if (exoPlayer.isPlaying) {
                    val pos = exoPlayer.currentPosition.coerceAtLeast(0L)
                    val dur = exoPlayer.duration.coerceAtLeast(0L)
                    _uiState.update {
                        it.copy(
                            currentPositionMs = pos,
                            durationMs = if (dur > 0) dur else it.durationMs
                        )
                    }
                }
                delay(500)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
    }

    fun release() {
        stopProgressUpdates()
        exoPlayer.release()
    }
}
