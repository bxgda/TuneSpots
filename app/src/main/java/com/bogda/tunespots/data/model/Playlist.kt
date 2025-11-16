package com.bogda.tunespots.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

data class Playlist(
    val id: String = "",

    val name: String = "",
    val description: String? = null,
    val coverImageUrl: String? = null,

    val location: GeoPoint = GeoPoint(0.0, 0.0),
    val createdAt: Timestamp = Timestamp.now(),
    val lastUpdatedAt: Timestamp = Timestamp.now(),

    val genre: String = "",

    val tracks: List<String> = emptyList(), // spotify id-jevi pesama u plejlisti

    val ownerId: String = "",
    val contributorIds: List<String> = emptyList()
)
