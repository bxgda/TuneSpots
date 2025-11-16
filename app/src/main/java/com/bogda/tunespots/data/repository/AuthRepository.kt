package com.bogda.tunespots.data.repository

import android.net.Uri
import com.bogda.tunespots.domain.model.User
import com.google.firebase.auth.FirebaseAuth
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
            // kreiramo korisnika u Firebase Authentication da bismo dobili njegov jedinstveni ID
            val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
            val firebaseUser = authResult.user ?: throw Exception("Kreiranje korisnika nije uspelo.")
            val userId = firebaseUser.uid

            // ako ima slika onda se ona upload-uje na cloudinary
            var profilePictureUrl: String? = null
            if (imageUri != null) {
                profilePictureUrl = storageRepository.uploadProfileImage(imageUri, userId)
                    .getOrThrow()
            }

            // pravimo objekat korisnika sa svim podacima
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

            // cuvamo korisnika u Firestore bazu podataka
            firestore.collection("users").document(userId).set(user).await()

            Result.success(Unit)
        } catch (e: Exception) {
            // ako nesto ne uspe onda se brise taj neuspeli podatak
            auth.currentUser?.delete()?.await()
            Result.failure(e) // Vrati grešku
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
}
