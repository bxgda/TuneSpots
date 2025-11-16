package com.bogda.tunespots.data.repository

import com.bogda.tunespots.domain.model.User
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) {

    suspend fun getUser(): Result<User> {
        return try {
            val userId = authRepository.getCurrentUserId() ?: throw Exception("User not logged in.")
            val userDoc = firestore.collection("users").document(userId).get().await()
            val user = userDoc.toObject(User::class.java) ?: throw Exception("User data not found.")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addPoints(userId: String, points: Long): Result<Unit> {
        return try {
            firestore.collection("users").document(userId)
                .update("points", FieldValue.increment(points))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}