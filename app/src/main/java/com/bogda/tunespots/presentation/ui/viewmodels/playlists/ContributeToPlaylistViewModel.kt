package com.bogda.tunespots.presentation.ui.viewmodels.playlists

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bogda.tunespots.data.model.Track
import com.bogda.tunespots.data.repository.AuthRepository
import com.bogda.tunespots.data.repository.PlaylistRepository
import com.bogda.tunespots.data.repository.SpotifyRepository
import com.bogda.tunespots.data.repository.UserRepository
import com.bogda.tunespots.domain.model.Playlist
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContributeToPlaylistViewModel @Inject constructor(
    private val spotifyRepository: SpotifyRepository,
    private val playlistRepository: PlaylistRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val playlistId: String = savedStateHandle.get<String>("playlistId")!!

    private val _playlist = MutableStateFlow<Playlist?>(null)
    val playlist = _playlist.asStateFlow()

    val playlistImageUri = mutableStateOf<Uri?>(null)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Track>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _selectedTracks = MutableStateFlow<List<Track>>(emptyList())
    val selectedTracks = _selectedTracks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isContributing = MutableStateFlow(false)
    val isContributing = _isContributing.asStateFlow()

    private val _contributionFinished = MutableStateFlow(false)
    val contributionFinished = _contributionFinished.asStateFlow()

    private var existingTrackIds = emptySet<String>()

    init {
        viewModelScope.launch {
            playlistRepository.getPlaylist(playlistId).collect { playlist ->
                playlist?.let {
                    _playlist.value = it
                    existingTrackIds = it.tracks.toSet()
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.isNotBlank()) {
            viewModelScope.launch {
                _isLoading.value = true
                val results = spotifyRepository.searchTracks(query).getOrNull()
                if (results != null) {
                    _searchResults.value = results.filter { !existingTrackIds.contains(it.id) }
                }
                _isLoading.value = false
            }
        }
    }

    fun onTrackSelected(track: Track) {
        if (!_selectedTracks.value.any { it.id == track.id }) {
            _selectedTracks.value = _selectedTracks.value + track
        }
    }



    fun onTrackRemoved(track: Track) {
        _selectedTracks.value = _selectedTracks.value.filter { it.id != track.id }
    }

    fun contribute() {
        viewModelScope.launch {
            _isContributing.value = true
            val currentUser = authRepository.getCurrentUser()
            val currentPlaylist = _playlist.value

            if (currentUser != null && currentPlaylist != null) {
                val newImageUri = playlistImageUri.value
                if (newImageUri != null && currentPlaylist.coverImageUrl.isNullOrEmpty()) {
                    val imageUrl = playlistRepository.uploadPlaylistImage(newImageUri).getOrNull()
                    if (imageUrl != null) {
                        playlistRepository.addPlaylist(
                            currentPlaylist.copy(coverImageUrl = imageUrl)
                        )
                    }
                }

                if (selectedTracks.value.isNotEmpty()) {
                    playlistRepository.addTracksToPlaylist(playlistId, _selectedTracks.value.map { it.id })
                    userRepository.addPoints(currentUser.uid, _selectedTracks.value.size.toLong())

                    if (currentPlaylist.ownerId != currentUser.uid && !currentPlaylist.contributorIds.contains(currentUser.uid)) {
                        playlistRepository.addContributor(playlistId, currentUser.uid)
                    }
                }
            }

            _isContributing.value = false
            _contributionFinished.value = true
        }
    }

    fun onContributionFinishedHandled() {
        _contributionFinished.value = false
    }
}
