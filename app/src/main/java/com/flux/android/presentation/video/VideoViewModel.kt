package com.flux.android.presentation.video

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flux.android.core.Resource
import com.flux.android.domain.model.MusicTrack
import com.flux.android.domain.repository.FluxRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VideoUiState(
    val isLoading: Boolean = true,
    val videos: List<MusicTrack> = emptyList(),
    val filteredVideos: List<MusicTrack> = emptyList(),
    val selectedCategory: String = "All Videos",
    val isGrid: Boolean = true,
    val errorMessage: String? = null,
)

class VideoViewModel(
    private val fluxRepository: FluxRepository,
) : ViewModel() {
    val categories = listOf("All Videos", "Music Videos", "Live Performances", "Official Releases")

    private val _uiState = MutableStateFlow(VideoUiState())
    val uiState: StateFlow<VideoUiState> = _uiState.asStateFlow()

    init {
        loadVideos()
    }

    fun loadVideos() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            // Default region for video is US per requirements
            when (val result = fluxRepository.getTrending("US")) {
                is Resource.Success -> {
                    val tracks = result.data.map { it.copy(isVideo = true) }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            videos = tracks,
                            filteredVideos = filterByCategory(tracks, it.selectedCategory),
                            errorMessage = null,
                        )
                    }
                }

                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }

                is Resource.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun selectCategory(category: String) {
        _uiState.update {
            it.copy(
                selectedCategory = category,
                filteredVideos = filterByCategory(it.videos, category),
            )
        }
    }

    fun setViewMode(isGrid: Boolean) {
        _uiState.update { it.copy(isGrid = isGrid) }
    }

    private fun filterByCategory(
        tracks: List<MusicTrack>,
        category: String,
    ): List<MusicTrack> =
        when (category) {
            "All Videos" -> {
                tracks
            }

            "Music Videos" -> {
                tracks
                    .filter {
                        it.title.contains("Official", ignoreCase = true) ||
                            it.title.contains("Video", ignoreCase = true)
                    }.ifEmpty { tracks }
            }

            "Live Performances" -> {
                tracks
                    .filter {
                        it.title.contains("Live", ignoreCase = true) ||
                            it.title.contains("Tour", ignoreCase = true)
                    }.ifEmpty { tracks }
            }

            else -> {
                tracks
            }
        }
}
