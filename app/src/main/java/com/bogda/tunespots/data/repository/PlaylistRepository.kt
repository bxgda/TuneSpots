package com.bogda.tunespots.data.repository

import android.net.Uri
import com.bogda.tunespots.domain.model.Playlist
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storageRepository: StorageRepository,
    private val authRepository: AuthRepository
) {
    private val playlistCollection = firestore.collection("playlists")

    suspend fun addPlaylist(playlist: Playlist): Result<Unit> {
        return try {
            // If playlist has no id, Firestore will generate one.
            // If it has one, it will use it (useful for updates).
            val document = if (playlist.id.isBlank()) {
                playlistCollection.document()
            } else {
                playlistCollection.document(playlist.id)
            }
            document.set(playlist.copy(id = document.id)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllPlaylists(): Flow<List<Playlist>> = flow {
        try {
            val snapshot = playlistCollection.get().await()
            val playlists = snapshot.toObjects<Playlist>()
            emit(playlists)
        } catch (e: Exception) {
            // In a real app, you'd want to handle this error more gracefully
            emit(emptyList())
        }
    }

    suspend fun uploadPlaylistImage(imageUri: Uri): String? {
        val userId = authRepository.getCurrentUserId() ?: return null
        return storageRepository.uploadPlaylistImage(imageUri, userId).getOrNull()
    }
}
