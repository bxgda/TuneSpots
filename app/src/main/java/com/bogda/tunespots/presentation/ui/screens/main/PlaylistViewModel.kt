package com.bogda.tunespots.presentation.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bogda.tunespots.data.repository.PlaylistRepository
import com.bogda.tunespots.data.repository.UserRepository
import com.bogda.tunespots.domain.model.Playlist
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlaylistUiState(
    val isLoading: Boolean = false,
    val authoredPlaylists: List<Pair<Playlist, String>> = emptyList(),
    val contributedPlaylists: List<Pair<Playlist, String>> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaylistUiState())
    val uiState: StateFlow<PlaylistUiState> = _uiState.asStateFlow()

    init {
        loadPlaylists()
    }

    private fun loadPlaylists() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val userId = auth.currentUser?.uid
            if (userId == null) {
                _uiState.update { it.copy(isLoading = false, error = "User not logged in") }
                return@launch
            }

            playlistRepository.getPlaylists(userId)
                .onSuccess { (authored, contributed) ->
                    val authoredWithNames = authored.map { playlist ->
                        val user = userRepository.getUser(playlist.ownerId).getOrNull()
                        Pair(playlist, user?.username ?: "Unknown")
                    }
                    val contributedWithNames = contributed.map { playlist ->
                        val user = userRepository.getUser(playlist.ownerId).getOrNull()
                        Pair(playlist, user?.username ?: "Unknown")
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            authoredPlaylists = authoredWithNames,
                            contributedPlaylists = contributedWithNames
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update { it.copy(isLoading = false, error = throwable.message) }
                }
        }
    }
}
