package com.bogda.tunespots.presentation.ui.viewmodels.playlists

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bogda.tunespots.data.model.Track
import com.bogda.tunespots.data.repository.AuthRepository
import com.bogda.tunespots.data.repository.PlaylistRepository
import com.bogda.tunespots.data.repository.SpotifyRepository
import com.bogda.tunespots.data.repository.UserRepository
import com.bogda.tunespots.data.services.LocationService
import com.bogda.tunespots.domain.model.Playlist
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddPlaylistViewModel @Inject constructor(
    private val spotifyRepository: SpotifyRepository,
    private val playlistRepository: PlaylistRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val locationService: LocationService,
) : ViewModel() {

    // Stanja za UI
    val playlistName = mutableStateOf("")
    val playlistDescription = mutableStateOf("")
    val selectedGenre = mutableStateOf("Pop")
    val playlistImageUri = mutableStateOf<Uri?>(null)


    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Track>>(emptyList())
    val searchResults: StateFlow<List<Track>> = _searchResults.asStateFlow()

    private val _selectedTracks = MutableStateFlow<List<Track>>(emptyList())
    val selectedTracks: StateFlow<List<Track>> = _selectedTracks.asStateFlow()

    private val _isAddingPlaylist = MutableStateFlow(false)
    val isAddingPlaylist: StateFlow<Boolean> = _isAddingPlaylist.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _playlistAdded = MutableStateFlow(false)
    val playlistAdded: StateFlow<Boolean> = _playlistAdded.asStateFlow()

    private var searchJob: Job? = null

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
        searchJob?.cancel()
        if (newQuery.isNotBlank()) {
            searchJob = viewModelScope.launch {
                delay(500L)
                _isLoading.value = true
                spotifyRepository.searchTracks(newQuery)
                    .onSuccess { tracks -> _searchResults.value = tracks }
                    .onFailure { _searchResults.value = emptyList() }
                _isLoading.value = false
            }
        } else {
            _searchResults.value = emptyList()
        }
    }

    fun onTrackSelected(track: Track) {
        _selectedTracks.value = _selectedTracks.value + track
    }

    fun onTrackRemoved(track: Track) {
        _selectedTracks.value = _selectedTracks.value.filter { it.id != track.id }
    }

    fun onPlaylistAddedHandled() {
        _playlistAdded.value = false
    }

    fun addPlaylist() {
        viewModelScope.launch {
            if (playlistName.value.isBlank() || selectedTracks.value.isEmpty() || _isAddingPlaylist.value) {
                return@launch
            }

            _isAddingPlaylist.value = true
            try {
                // Korak 1: Pripremi sve podatke
                val currentUser = authRepository.getCurrentUserId()
                    ?: throw IllegalStateException("User not logged in")
                val currentLocation = locationService.getCurrentLocation().first()
                val pointsToAdd = selectedTracks.value.size.toLong()

                // Korak 2: Uploaduj sliku (ako postoji)
                // Ova operacija se izvršava i njen rezultat se čeka (await)
                val imageUrl: String? = playlistImageUri.value?.let { uri ->
                    playlistRepository.uploadPlaylistImage(uri) // Pretpostavka: ovo je suspend funkcija
                }

                // Korak 3: Kreiraj objekat plejliste
                val newPlaylist = Playlist(
                    name = playlistName.value.trim(),
                    description = playlistDescription.value.trim(),
                    genre = selectedGenre.value,
                    tracks = selectedTracks.value.map { it.id },
                    location = currentLocation,
                    ownerId = currentUser,
                    createdAt = Timestamp.now(),
                    lastUpdatedAt = Timestamp.now(),
                    coverImageUrl = imageUrl
                )

                // Korak 4: Dodaj plejlistu
                // Izvršava se i čeka završetak
                playlistRepository.addPlaylist(newPlaylist) // Pretpostavka: ovo je suspend funkcija

                // Korak 5: Dodaj poene korisniku
                // Izvršava se i čeka završetak
                userRepository.addPoints(currentUser, pointsToAdd) // Pretpostavka: ovo je suspend funkcija

                // Ako su sve prethodne operacije uspele, dolazimo do ovde
                _playlistAdded.value = true

            } catch (e: Exception) {
                // Bilo koja greška iz `try` bloka će biti uhvaćena ovde
                Log.e("AddPlaylistVM", "Failed to add playlist or points.", e)
                // Ovde možete dodati logiku za prikaz greške korisniku
            } finally {
                // Ovaj blok se izvršava uvek, bilo da je operacija uspela ili ne
                _isAddingPlaylist.value = false
            }
        }
    }
}