package com.bogda.tunespots.data.repository

import android.net.Uri
import com.bogda.tunespots.domain.model.Playlist
import com.google.firebase.firestore.FieldValue
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

    fun getPlaylist(playlistId: String): Flow<Playlist?> = callbackFlow {
        val listenerRegistration = playlistCollection.document(playlistId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                trySend(snapshot.toObject(Playlist::class.java))
            } else {
                trySend(null)
            }
        }
        awaitClose { listenerRegistration.remove() }
    }

    suspend fun uploadPlaylistImage(imageUri: Uri): Result<String> {
        val userId = authRepository.getCurrentUserId() ?: return Result.failure(Exception("User not logged in"))
        return storageRepository.uploadPlaylistImage(imageUri, userId)
    }

    suspend fun getPlaylists(userId: String): Result<Pair<List<Playlist>, List<Playlist>>> {
        return try {
            val authoredPlaylists = firestore.collection("playlists")
                .whereEqualTo("ownerId", userId)
                .get()
                .await()
                .toObjects(Playlist::class.java)

            val contributedPlaylists = firestore.collection("playlists")
                .whereArrayContains("contributorIds", userId)
                .get()
                .await()
                .toObjects(Playlist::class.java)

            Result.success(Pair(authoredPlaylists, contributedPlaylists))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addTracksToPlaylist(playlistId: String, trackIds: List<String>) {
        playlistCollection.document(playlistId).update("tracks", FieldValue.arrayUnion(*trackIds.toTypedArray())).await()
    }

    suspend fun addContributor(playlistId: String, userId: String) {
        playlistCollection.document(playlistId).update("contributorIds", FieldValue.arrayUnion(userId)).await()
    }
}
