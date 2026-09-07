package com.flux.android.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flux.android.core.Resource
import com.flux.android.domain.model.MusicTrack
import com.flux.android.domain.repository.FluxRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val results: List<MusicTrack> = emptyList(),
    val nextPageToken: String? = null,
    val isGrid: Boolean = false,
    val errorMessage: String? = null,
    val recentSearches: List<String> = listOf("Arijit Singh", "Ed Sheeran", "Taylor Swift", "Badshah", "Dua Lipa")
)

class SearchViewModel(
    private val fluxRepository: FluxRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(newQuery: String) {
        _uiState.update { it.copy(query = newQuery, errorMessage = null) }
        searchJob?.cancel()

        if (newQuery.trim().isEmpty()) {
            _uiState.update { it.copy(results = emptyList(), isLoading = false, nextPageToken = null) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(400) // 400ms debounce
            performSearch(newQuery.trim(), null)
        }
    }

    fun performSearch(query: String, pageToken: String?) {
        val isFirstPage = pageToken == null
        _uiState.update {
            if (isFirstPage) it.copy(isLoading = true, errorMessage = null)
            else it.copy(isLoadingMore = true)
        }

        viewModelScope.launch {
            when (val result = fluxRepository.searchMusic(query, pageToken)) {
                is Resource.Success -> {
                    val (tracks, token) = result.data
                    _uiState.update { current ->
                        val combined = if (isFirstPage) tracks else current.results + tracks
                        val updatedRecent = if (isFirstPage && query.isNotEmpty() && !current.recentSearches.contains(query)) {
                            (listOf(query) + current.recentSearches).take(8)
                        } else current.recentSearches

                        current.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            results = combined,
                            nextPageToken = token,
                            recentSearches = updatedRecent,
                            errorMessage = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            errorMessage = result.message
                        )
                    }
                }
                is Resource.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        val token = state.nextPageToken
        if (!state.isLoading && !state.isLoadingMore && !token.isNullOrEmpty() && state.query.isNotEmpty()) {
            performSearch(state.query, token)
        }
    }

    fun setViewMode(isGrid: Boolean) {
        _uiState.update { it.copy(isGrid = isGrid) }
    }

    fun clearQuery() {
        searchJob?.cancel()
        _uiState.update { it.copy(query = "", results = emptyList(), isLoading = false, nextPageToken = null) }
    }
}
