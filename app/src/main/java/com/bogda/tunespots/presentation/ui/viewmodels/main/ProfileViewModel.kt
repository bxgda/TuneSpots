package com.bogda.tunespots.presentation.ui.viewmodels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bogda.tunespots.data.repository.AuthRepository
import com.bogda.tunespots.data.repository.UserRepository
import com.bogda.tunespots.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _userState = MutableStateFlow<UserState>(UserState.Loading)
    val userState = _userState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        userRepository.getUser()
            .onEach { user ->
                _userState.value = UserState.Loaded(user)
            }
            .catch { e ->
                _userState.value = UserState.Error(e.message ?: "Failed to load user profile.")
            }
            .launchIn(viewModelScope)
    }

    fun logout() {
        authRepository.logoutUser()
    }

    sealed class UserState {
        object Loading : UserState()
        data class Loaded(val user: User) : UserState()
        data class Error(val message: String) : UserState()
    }
}