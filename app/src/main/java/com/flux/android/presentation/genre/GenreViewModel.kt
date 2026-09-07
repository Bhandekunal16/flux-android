package com.flux.android.presentation.genre

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flux.android.core.Resource
import com.flux.android.domain.model.GenreItem
import com.flux.android.domain.model.MusicTrack
import com.flux.android.domain.repository.FluxRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GenreUiState(
    val genres: List<GenreItem> = emptyList(),
    val selectedGenre: GenreItem? = null,
    val isLoadingTracks: Boolean = false,
    val tracks: List<MusicTrack> = emptyList(),
    val isGrid: Boolean = true,
    val errorMessage: String? = null,
)

class GenreViewModel(
    private val fluxRepository: FluxRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(GenreUiState())
    val uiState: StateFlow<GenreUiState> = _uiState.asStateFlow()

    init {
        val availableGenres = fluxRepository.getAvailableGenres()
        val firstGenre = availableGenres.firstOrNull()
        _uiState.update { it.copy(genres = availableGenres, selectedGenre = firstGenre) }
        firstGenre?.let { loadGenreTracks(it) }
    }

    fun selectGenre(genre: GenreItem) {
        if (_uiState.value.selectedGenre?.id != genre.id) {
            _uiState.update { it.copy(selectedGenre = genre) }
            loadGenreTracks(genre)
        }
    }

    fun loadGenreTracks(genreParam: GenreItem? = null) {
        val genre = genreParam ?: _uiState.value.selectedGenre ?: return
        _uiState.update { it.copy(isLoadingTracks = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = fluxRepository.getGenreMusic(genre.queryType)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoadingTracks = false, tracks = result.data, errorMessage = null) }
                }

                is Resource.Error -> {
                    _uiState.update { it.copy(isLoadingTracks = false, errorMessage = result.message) }
                }

                is Resource.Loading -> {
                    _uiState.update { it.copy(isLoadingTracks = true) }
                }
            }
        }
    }

    fun setViewMode(isGrid: Boolean) {
        _uiState.update { it.copy(isGrid = isGrid) }
    }
}
