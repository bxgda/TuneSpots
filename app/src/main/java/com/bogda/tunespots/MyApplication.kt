package com.bogda.tunespots

import android.app.Application
import com.cloudinary.android.MediaManager // <-- 1. Dodaj ovaj import
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {

    // 2. Dodaj celu ovu 'onCreate' metodu
    override fun onCreate() {
        super.onCreate()
        initCloudinary()
    }

    // 3. Dodaj ovu privatnu funkciju
    private fun initCloudinary() {
        // Kreiramo mapu sa konfiguracionim podacima iz BuildConfig-a
        val config = mapOf(
            "cloud_name" to BuildConfig.CLOUDINARY_CLOUD_NAME,
            "api_key" to BuildConfig.CLOUDINARY_API_KEY,
            "api_secret" to BuildConfig.CLOUDINARY_API_SECRET
        )
        // Pozivamo 'init' metodu koja je nedostajala
        MediaManager.init(this, config)
    }
}
