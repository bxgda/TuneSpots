package com.bogda.tunespots.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface SpotifyApiService {

    @GET("search")
    suspend fun searchTracks(
        @Header("Authorization") token: String,
        @Query("q") query: String,
        @Query("type") type: String = "track"
    ): SpotifySearchResponse

    @GET("tracks")
    suspend fun getTracks(
        @Header("Authorization") token: String,
        @Query("ids") ids: String
    ): SpotifyGetTracksResponse
}

data class SpotifySearchResponse(
    val tracks: TrackList
)

data class SpotifyGetTracksResponse(
    val tracks: List<Track>
)

data class TrackList(
    val items: List<Track>
)

data class Track(
    val id: String,
    val name: String,
    val artists: List<Artist>,
    val album: Album
) {
    val artistNames: String
        get() = artists.joinToString(separator = ", ") { it.name }
}

data class Artist(
    val name: String
)

data class Album(
    @SerializedName("images")
    val images: List<SpotifyImage>
)

data class SpotifyImage(
    val url: String
)
