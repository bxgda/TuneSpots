package com.bogda.tunespots.data.di

import com.bogda.tunespots.data.repository.CloudinaryStorageRepository
import com.bogda.tunespots.data.repository.StorageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class StorageModule {

    @Binds
    abstract fun bindStorageRepository(
        cloudinaryStorageRepository: CloudinaryStorageRepository
    ): StorageRepository
}
