package com.bogda.tunespots.data.mapper

import com.bogda.tunespots.data.remote.Track as NetworkTrack
// Uvoz domenskog modela
import com.bogda.tunespots.data.model.Track as DomainTrack

// za jedan objekat
fun NetworkTrack.toDomainTrack(): DomainTrack {
    return DomainTrack(
        id = this.id,
        title = this.name,
        artist = this.artistNames,
        coverArtUrl = this.album.images.firstOrNull()?.url
    )
}

// za celu listu
fun List<NetworkTrack>.toDomainTrackList(): List<DomainTrack> {
    return this.map { it.toDomainTrack() }
}
