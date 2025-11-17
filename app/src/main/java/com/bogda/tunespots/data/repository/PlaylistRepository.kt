package com.bogda.tunespots.data.repository

import android.net.Uri
import com.bogda.tunespots.domain.model.Playlist
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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

    fun getAllPlaylists(): Flow<List<Playlist>> = callbackFlow {
        val listener = playlistCollection.addSnapshotListener { snapshot, e ->
            if (e != null) {
                close(e)
                return@addSnapshotListener
            }
            snapshot?.let {
                trySend(it.toObjects<Playlist>())
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun getPlaylist(playlistId: String): Playlist? {
        return try {
            playlistCollection.document(playlistId).get().await().toObject(Playlist::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun uploadPlaylistImage(imageUri: Uri): String? {
        val userId = authRepository.getCurrentUserId() ?: return null
        return storageRepository.uploadPlaylistImage(imageUri, userId).getOrNull()
    }
}
