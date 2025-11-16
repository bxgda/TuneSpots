package com.bogda.tunespots.data.repository

import android.content.Context
import android.net.Uri
import com.bogda.tunespots.BuildConfig
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface StorageRepository {
    suspend fun uploadProfileImage(imageUri: Uri, userId: String): Result<String>
    suspend fun uploadPlaylistImage(imageUri: Uri, userId: String): Result<String>
}

@Singleton
class CloudinaryStorageRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : StorageRepository {

    init {
        if (MediaManager.get() == null) {
            val config = mapOf(
                "cloud_name" to BuildConfig.CLOUDINARY_CLOUD_NAME,
                "api_key" to BuildConfig.CLOUDINARY_API_KEY,
                "api_secret" to BuildConfig.CLOUDINARY_API_SECRET
            )
            MediaManager.init(context, config)
        }
    }

    override suspend fun uploadProfileImage(imageUri: Uri, userId: String): Result<String> = suspendCoroutine { continuation ->
        // ime fajla - id korisnika i nesto random
        val publicId = "profile_${userId}_${UUID.randomUUID()}"

        MediaManager.get()
            .upload(imageUri)
            .unsigned(BuildConfig.CLOUDINARY_UPLOAD_PRESET)
            .option("folder", "tunespots/profile_images")
            .option("public_id", publicId)
            .callback(object : UploadCallback {
                override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                    val secureUrl = resultData?.get("secure_url") as? String
                    if (secureUrl != null) {
                        continuation.resume(Result.success(secureUrl))
                    } else {
                        continuation.resume(Result.failure(Exception("Cloudinary URL je null.")))
                    }
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    continuation.resume(Result.failure(Exception(error?.description ?: "Nepoznata Cloudinary greška.")))
                }

                override fun onStart(requestId: String?) { }
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) { }
                override fun onReschedule(requestId: String?, error: ErrorInfo?) { }
            })
            .dispatch()
    }

    override suspend fun uploadPlaylistImage(imageUri: Uri, userId: String): Result<String> = suspendCoroutine { continuation ->
        val publicId = "playlist_${userId}_${UUID.randomUUID()}"

        MediaManager.get()
            .upload(imageUri)
            .unsigned(BuildConfig.CLOUDINARY_UPLOAD_PRESET)
            .option("folder", "tunespots/playlist_images")
            .option("public_id", publicId)
            .callback(object : UploadCallback {
                override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                    val secureUrl = resultData?.get("secure_url") as? String
                    if (secureUrl != null) {
                        continuation.resume(Result.success(secureUrl))
                    } else {
                        continuation.resume(Result.failure(Exception("Cloudinary URL je null.")))
                    }
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    continuation.resume(Result.failure(Exception(error?.description ?: "Nepoznata Cloudinary greška.")))
                }

                override fun onStart(requestId: String?) { }
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) { }
                override fun onReschedule(requestId: String?, error: ErrorInfo?) { }
            })
            .dispatch()
    }
}
