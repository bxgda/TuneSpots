package com.bogda.tunespots.presentation.ui.viewmodels.main

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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.text.get

data class SearchUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val playlists: List<Pair<Playlist, User>> = emptyList(),
    val genres: List<String> = emptyList(),
    val selectedGenres: Set<String> = emptySet(),
    val radius: Float = 200f,
    val searchQuery: String = ""
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        loadPlaylists()
    }

    private fun loadPlaylists() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val playlists = playlistRepository.getAllPlaylists().first()
                val ownerIds = playlists.map { it.ownerId }.distinct()

                val users = if (ownerIds.isNotEmpty()) {
                    userRepository.getUsers(ownerIds).getOrNull() ?: emptyList()
                } else {
                    emptyList<User>()
                }

                val userMap = users.associateBy { it.id }
                val playlistsWithOwners = playlists.mapNotNull { playlist ->
                    userMap[playlist.ownerId]?.let { owner ->
                        Pair(playlist, owner)
                    }
                }

                val genres = playlists.map { it.genre }.distinct()
                val currentSelectedGenres = _uiState.value.selectedGenres

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        playlists = playlistsWithOwners,
                        genres = genres,
                        selectedGenres = if (currentSelectedGenres.isEmpty()) genres.toSet() else currentSelectedGenres,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onGenreSelected(genre: String, isSelected: Boolean) {
        _uiState.update {
            val newSelectedGenres = if (isSelected) {
                it.selectedGenres + genre
            } else {
                it.selectedGenres - genre
            }
            it.copy(selectedGenres = newSelectedGenres)
        }
    }

    fun onRadiusChange(radius: Float) {
        _uiState.update { it.copy(radius = radius) }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
}
