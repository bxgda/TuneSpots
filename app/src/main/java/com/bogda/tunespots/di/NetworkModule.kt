package com.bogda.tunespots.di

import com.bogda.tunespots.data.remote.SpotifyApiService
import com.bogda.tunespots.data.remote.SpotifyAuthService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    @Named("SpotifyApi")
    fun provideSpotifyApiRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.spotify.com/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("SpotifyAuth")
    fun provideSpotifyAuthRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://accounts.spotify.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideSpotifyApi(@Named("SpotifyApi") retrofit: Retrofit): SpotifyApiService {
        return retrofit.create(SpotifyApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideSpotifyAuthApi(@Named("SpotifyAuth") retrofit: Retrofit): SpotifyAuthService {
        return retrofit.create(SpotifyAuthService::class.java)
    }
}
