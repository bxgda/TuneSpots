package com.bogda.tunespots.presentation.ui.viewmodels.main

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bogda.tunespots.data.repository.AuthRepository
import com.bogda.tunespots.data.repository.StorageRepository
import com.bogda.tunespots.data.repository.UserRepository
import com.bogda.tunespots.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val storageRepository: StorageRepository
) : ViewModel() {

    private val _userState = MutableStateFlow<UserState>(UserState.Loading)
    val userState = _userState.asStateFlow()

    private val _showImageSourceDialog = MutableStateFlow(false)
    val showImageSourceDialog = _showImageSourceDialog.asStateFlow()

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

    fun onProfileImageClick() {
        _showImageSourceDialog.value = true
    }

    fun onImageSourceDialogDismiss() {
        _showImageSourceDialog.value = false
    }

    fun onImageSelected(uri: Uri) {
        viewModelScope.launch {
            val currentUser = (userState.value as? UserState.Loaded)?.user ?: return@launch

            // Prvo obriši staru sliku ako postoji
            currentUser.profilePictureUrl?.let {
                storageRepository.deleteImage(it)
                // Greške pri brisanju se ignorišu da se ne bi blokirao proces
            }

            // Zatim uploaduj novu sliku
            storageRepository.uploadProfileImage(uri, currentUser.id)
                .onSuccess { newImageUrl ->
                    val updatedUser = currentUser.copy(profilePictureUrl = newImageUrl)
                    userRepository.updateUser(updatedUser)
                        .onFailure {
                            _userState.value = UserState.Error("Failed to save new profile picture.")
                        }
                    // UI se automatski ažurira putem listener-a u getUser()
                }
                .onFailure {
                    _userState.value = UserState.Error("Failed to upload profile picture.")
                }
        }
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