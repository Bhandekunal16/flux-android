package com.flux.android.presentation.wishlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flux.android.core.Resource
import com.flux.android.domain.model.MusicTrack
import com.flux.android.domain.model.WishlistItem
import com.flux.android.domain.repository.AuthRepository
import com.flux.android.domain.repository.FluxRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WishlistUiState(
    val isLoading: Boolean = false,
    val items: List<WishlistItem> = emptyList(),
    val isGrid: Boolean = false,
    val isUnauthenticated: Boolean = false,
    val errorMessage: String? = null,
    val actionFeedback: String? = null,
)

class WishlistViewModel(
    private val fluxRepository: FluxRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(WishlistUiState())
    val uiState: StateFlow<WishlistUiState> = _uiState.asStateFlow()

    init {
        loadWishlist()
    }

    fun loadWishlist() {
        val isLoggedIn = authRepository.isLoggedIn()
        _uiState.update { it.copy(isLoading = true, isUnauthenticated = !isLoggedIn, errorMessage = null) }

        viewModelScope.launch {
            when (val result = fluxRepository.getWishlist()) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            items = result.data,
                            isUnauthenticated = false,
                            errorMessage = null,
                        )
                    }
                }

                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isUnauthenticated = result.code == 401 || !isLoggedIn,
                            errorMessage = if (result.code == 401) null else result.message,
                        )
                    }
                }

                is Resource.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun toggleWishlist(
        track: MusicTrack,
        onRequireLogin: () -> Unit = {},
    ) {
        val isWishlisted = _uiState.value.items.any { it.videoId == track.id }
        if (!authRepository.isLoggedIn()) {
            onRequireLogin()
            return
        }

        if (isWishlisted) {
            val item = _uiState.value.items.find { it.videoId == track.id } ?: return
            removeFromWishlist(item.id.ifEmpty { item.videoId })
        } else {
            addToWishlist(track)
        }
    }

    fun addToWishlist(track: MusicTrack) {
        viewModelScope.launch {
            when (val result = fluxRepository.addToWishlist(track)) {
                is Resource.Success -> {
                    _uiState.update {
                        val updated =
                            if (it.items.none { i -> i.videoId == track.id }) {
                                listOf(result.data) + it.items
                            } else {
                                it.items
                            }
                        it.copy(items = updated, actionFeedback = "Saved to Wishlist")
                    }
                }

                is Resource.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }

                is Resource.Loading -> {}
            }
        }
    }

    fun removeFromWishlist(idOrVideoId: String) {
        viewModelScope.launch {
            // Optimistically filter
            _uiState.update {
                it.copy(items = it.items.filterNot { item -> item.id == idOrVideoId || item.videoId == idOrVideoId })
            }
            when (val result = fluxRepository.removeFromWishlist(idOrVideoId)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(actionFeedback = "Removed from Wishlist") }
                }

                is Resource.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                    loadWishlist()
                }

                is Resource.Loading -> {}
            }
        }
    }

    fun setViewMode(isGrid: Boolean) {
        _uiState.update { it.copy(isGrid = isGrid) }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(actionFeedback = null) }
    }
}
