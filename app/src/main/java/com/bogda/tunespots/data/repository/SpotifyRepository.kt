package com.bogda.tunespots.data.repository

import android.util.Base64
import android.util.Log
import com.bogda.tunespots.BuildConfig
import com.bogda.tunespots.data.mapper.toDomainTrack
import com.bogda.tunespots.data.mapper.toDomainTrackList
import com.bogda.tunespots.data.model.Track as DomainTrack
import com.bogda.tunespots.data.remote.SpotifyApiService
import com.bogda.tunespots.data.remote.SpotifyAuthService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpotifyRepository @Inject constructor(
    private val spotifyApiService: SpotifyApiService,
    private val spotifyAuthService: SpotifyAuthService
) {

    private suspend fun getAccessToken(): String {
        return try {
            val clientId = BuildConfig.SPOTIFY_CLIENT_ID
            val clientSecret = BuildConfig.SPOTIFY_CLIENT_SECRET
            val authString = "$clientId:$clientSecret"
            val authHeader = "Basic ${Base64.encodeToString(authString.toByteArray(), Base64.NO_WRAP)}"

            val response = spotifyAuthService.getAccessToken(authHeader)
            "${response.tokenType} ${response.accessToken}"
        } catch (e: Exception) {
            Log.e("SpotifyRepository", "Greška pri dobavljanju tokena", e)
            throw e
        }
    }

    suspend fun searchTracks(query: String): Result<List<DomainTrack>> {
        return try {
            val token = getAccessToken()
            val response = spotifyApiService.searchTracks(token = token, query = query)
            val domainTracks = response.tracks.items.toDomainTrackList()
            Result.success(domainTracks)
        } catch (e: Exception) {
            Log.e("SpotifyRepository", "Greška pri pretrazi pesama", e)
            Result.failure(e)
        }
    }

    suspend fun getTracks(trackIds: List<String>): Result<List<DomainTrack>> {
        if (trackIds.isEmpty()) return Result.success(emptyList())

        return try {
            val token = getAccessToken()
            val ids = trackIds.joinToString(",")
            val response = spotifyApiService.getTracks(token = token, ids = ids)
            val domainTracks = response.tracks.map { it.toDomainTrack() }
            Result.success(domainTracks)
        } catch (e: Exception) {
            Log.e("SpotifyRepository", "Greška pri dobavljanju pesama po ID-u", e)
            Result.failure(e)
        }
    }
}
