package com.bogda.tunespots.data.repository

import com.bogda.tunespots.domain.model.Playlist
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepository @Inject constructor(
    private val firestore: FirebaseFirestore
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
}
