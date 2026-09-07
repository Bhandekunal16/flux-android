package com.flux.android.presentation.home

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

data class HomeUiState(
    val isLoading: Boolean = true,
    val tracks: List<MusicTrack> = emptyList(),
    val isGrid: Boolean = true,
    val selectedRegion: String = "IN",
    val errorMessage: String? = null
)

class HomeViewModel(
    private val fluxRepository: FluxRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadTrending("IN")
    }

    fun loadTrending(region: String = _uiState.value.selectedRegion) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null, selectedRegion = region) }
        viewModelScope.launch {
            when (val result = fluxRepository.getTrending(region)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, tracks = result.data, errorMessage = null) }
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

    fun setViewMode(isGrid: Boolean) {
        _uiState.update { it.copy(isGrid = isGrid) }
    }

    fun setRegion(region: String) {
        if (_uiState.value.selectedRegion != region) {
            loadTrending(region)
        }
    }
}
