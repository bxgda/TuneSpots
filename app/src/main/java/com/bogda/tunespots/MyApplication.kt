package com.bogda.tunespots

import android.app.Application
import android.content.Intent
import com.bogda.tunespots.data.services.LocationUpdateService
import com.cloudinary.android.MediaManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initCloudinary()
        startLocationService()
    }

    private fun initCloudinary() {
        val config = mapOf(
            "cloud_name" to BuildConfig.CLOUDINARY_CLOUD_NAME,
            "api_key" to BuildConfig.CLOUDINARY_API_KEY,
            "api_secret" to BuildConfig.CLOUDINARY_API_SECRET
        )
        MediaManager.init(this, config)
    }

    private fun startLocationService() {
        Intent(applicationContext, LocationUpdateService::class.java).also {
            startService(it)
        }
    }
}
