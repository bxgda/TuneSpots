package com.bogda.tunespots.data.mapper

// Uvoz mrežnog modela
import com.bogda.tunespots.data.remote.Track as NetworkTrack
// Uvoz domenskog modela
import com.bogda.tunespots.data.model.Track as DomainTrack

// Funkcija koja mapira jedan objekat
fun NetworkTrack.toDomainTrack(): DomainTrack {
    return DomainTrack(
        id = this.id,
        title = this.name,
        artist = this.artistNames,
        // Uzimamo URL prve slike iz liste, ako lista postoji i nije prazna.
        // U suprotnom, vraćamo null.
        coverArtUrl = this.album.images.firstOrNull()?.url
    )
}

// Pomoćna funkcija za mapiranje cele liste
fun List<NetworkTrack>.toDomainTrackList(): List<DomainTrack> {
    return this.map { it.toDomainTrack() }
}
