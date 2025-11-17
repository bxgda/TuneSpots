package com.bogda.tunespots.presentation.ui.viewmodels.playlists

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bogda.tunespots.data.model.Track
import com.bogda.tunespots.data.repository.PlaylistRepository
import com.bogda.tunespots.data.repository.SpotifyRepository
import com.bogda.tunespots.data.repository.UserRepository
import com.bogda.tunespots.domain.model.Playlist
import com.bogda.tunespots.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val userRepository: UserRepository,
    private val spotifyRepository: SpotifyRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _playlistState = MutableStateFlow<PlaylistState>(PlaylistState.Loading)
    val playlistState = _playlistState.asStateFlow()

    init {
        val playlistId = savedStateHandle.get<String>("playlistId")
        if (playlistId != null) {
            loadPlaylistDetails(playlistId)
        } else {
            _playlistState.value = PlaylistState.Error("Playlist ID not found.")
        }
    }

    private fun loadPlaylistDetails(playlistId: String) {
        viewModelScope.launch {
            try {
                val playlist = playlistRepository.getPlaylist(playlistId) ?: throw Exception("Playlist not found.")
                val owner = userRepository.getUser(playlist.ownerId).getOrThrow()
                val tracks = spotifyRepository.getTracks(playlist.tracks).getOrThrow()
                val contributors = playlist.contributorIds?.let { userIds ->
                    if (userIds.isNotEmpty()) {
                        userRepository.getUsers(userIds).getOrThrow()
                    } else {
                        emptyList()
                    }
                } ?: emptyList()

                _playlistState.value = PlaylistState.Loaded(playlist, owner, tracks, contributors)
            } catch (e: Exception) {
                _playlistState.value = PlaylistState.Error(e.message ?: "Failed to load playlist details.")
            }
        }
    }
}

sealed class PlaylistState {
    object Loading : PlaylistState()
    data class Loaded(
        val playlist: Playlist,
        val owner: User,
        val tracks: List<Track>,
        val contributors: List<User>
    ) : PlaylistState()
    data class Error(val message: String) : PlaylistState()
}
