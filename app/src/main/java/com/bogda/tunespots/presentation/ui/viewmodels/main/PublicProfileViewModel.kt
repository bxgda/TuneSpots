package com.bogda.tunespots.presentation.ui.viewmodels.main

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bogda.tunespots.data.repository.PlaylistRepository
import com.bogda.tunespots.data.repository.UserRepository
import com.bogda.tunespots.domain.model.Playlist
import com.bogda.tunespots.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PublicProfileUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val playlists: List<Playlist> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class PublicProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val playlistRepository: PlaylistRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(PublicProfileUiState())
    val uiState: StateFlow<PublicProfileUiState> = _uiState.asStateFlow()

    private val userId: String = savedStateHandle.get<String>("userId")!!

    init {
        loadPublicProfile()
    }

    private fun loadPublicProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val userResult = userRepository.getUser(userId)
            val playlistsResult = playlistRepository.getPlaylists(userId)

            userResult.onSuccess { user ->
                playlistsResult.onSuccess { (authored, _) ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            user = user,
                            playlists = authored
                        )
                    }
                }.onFailure {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to load playlists"
                        )
                    }
                }
            }.onFailure {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load user data"
                    )
                }
            }
        }
    }
}
