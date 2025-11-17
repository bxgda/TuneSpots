package com.bogda.tunespots.presentation.ui.viewmodels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bogda.tunespots.data.repository.UserRepository
import com.bogda.tunespots.presentation.ui.components.DJ
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LeaderboardUiState(
    val djs: List<DJ> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    init {
        getDjs()
    }

    private fun getDjs() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            userRepository.getTopDjs().onSuccess { users ->
                val djs = users.map { user ->
                    DJ(
                        id = user.id,
                        username = user.username,
                        points = user.points.toInt(),
                        imageUrl = user.profilePictureUrl ?: ""
                    )
                }
                _uiState.value = _uiState.value.copy(djs = djs, isLoading = false)
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    error = exception.message ?: "An unexpected error occurred.",
                    isLoading = false
                )
            }
        }
    }
}
