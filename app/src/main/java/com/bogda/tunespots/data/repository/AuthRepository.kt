package com.bogda.tunespots.data.repository

import android.net.Uri
import com.bogda.tunespots.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storageRepository: StorageRepository
) {

    suspend fun registerUser(
        email: String,
        pass: String,
        username: String,
        firstName: String,
        lastName: String,
        phoneNumber: String,
        imageUri: Uri?
    ): Result<Unit> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
            val firebaseUser = authResult.user ?: throw Exception("Kreiranje korisnika nije uspelo.")
            val userId = firebaseUser.uid

            var profilePictureUrl: String? = null
            if (imageUri != null) {
                profilePictureUrl = storageRepository.uploadProfileImage(imageUri, userId)
                    .getOrThrow()
            }

            val user = User(
                id = userId,
                email = email,
                username = username,
                firstName = firstName,
                lastName = lastName,
                phoneNumber = phoneNumber,
                profilePictureUrl = profilePictureUrl,
                points = 0
            )

            firestore.collection("users").document(userId).set(user).await()

            Result.success(Unit)
        } catch (e: Exception) {
            auth.currentUser?.delete()?.await()
            Result.failure(e)
        }
    }

    suspend fun loginUser(email: String, pass: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, pass).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logoutUser() {
        auth.signOut()
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
}
