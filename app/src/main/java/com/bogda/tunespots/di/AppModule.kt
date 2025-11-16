package com.bogda.tunespots.di

import android.content.Context
import com.bogda.tunespots.data.repository.AuthRepository
import com.bogda.tunespots.data.repository.CloudinaryStorageRepository
import com.bogda.tunespots.data.repository.StorageRepository
import com.bogda.tunespots.data.services.LocationService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideStorageRepository(
        @ApplicationContext context: Context
    ): StorageRepository {
        return CloudinaryStorageRepository(context)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore,
        storageRepository: StorageRepository
    ): AuthRepository {
        return AuthRepository(auth, firestore, storageRepository)
    }

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(@ApplicationContext context: Context): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    @Provides
    @Singleton
    fun provideLocationService(
        @ApplicationContext context: Context,
        client: FusedLocationProviderClient
    ): LocationService {
        return LocationService(context, client)
    }
}
