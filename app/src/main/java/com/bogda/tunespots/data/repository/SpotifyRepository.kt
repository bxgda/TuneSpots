package com.bogda.tunespots.data.repository

import android.util.Base64
import android.util.Log // <-- Dodajemo Log za debagovanje
import com.bogda.tunespots.BuildConfig
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
    // Nema potrebe da čuvamo token ovde, uvek ćemo ga tražiti ponovo
    // U produkcijskoj aplikaciji bi ovde čuvali i proveravali vreme isteka tokena

    private suspend fun getAccessToken(): String {
        return try {
            val clientId = BuildConfig.SPOTIFY_CLIENT_ID
            val clientSecret = BuildConfig.SPOTIFY_CLIENT_SECRET
            val authString = "$clientId:$clientSecret"
            // Koristi NO_WRAP da se ne dodaju novi redovi
            val authHeader = "Basic ${Base64.encodeToString(authString.toByteArray(), Base64.NO_WRAP)}"

            val response = spotifyAuthService.getAccessToken(authHeader)
            // Vraćamo token u formatu "Bearer TOKEN_VREDNOST"
            "${response.tokenType} ${response.accessToken}"
        } catch (e: Exception) {
            // Logujemo grešku da bismo je videli u Logcat-u
            Log.e("SpotifyRepository", "Greška pri dobavljanju tokena", e)
            throw e // Ponovo baci izuzetak da bi pozivalac znao da je došlo do greške
        }
    }

    suspend fun searchTracks(query: String): Result<List<DomainTrack>> {
        return try {
            // 1. UVEK prvo dobavi svež token
            val token = getAccessToken()

            Log.d("SpotifyRepository", "Pretraga sa tokenom: $token i upitom: $query")

            // 2. Pozovi API sa ispravnim tokenom
            val response = spotifyApiService.searchTracks(token = token, query = query)

            // 3. Mapiraj mrežni model u domenski model
            val domainTracks = response.tracks.items.toDomainTrackList()
            Log.d("SpotifyRepository", "Pronađeno ${domainTracks.size} pesama")

            // 4. Vrati uspešan rezultat sa mapiranim podacima
            Result.success(domainTracks)

        } catch (e: Exception) {
            // Logujemo grešku da bismo je videli u Logcat-u
            Log.e("SpotifyRepository", "Greška pri pretrazi pesama", e)
            Result.failure(e)
        }
    }
}
