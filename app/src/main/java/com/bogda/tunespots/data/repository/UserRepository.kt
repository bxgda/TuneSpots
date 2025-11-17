package com.bogda.tunespots.data.repository

import com.bogda.tunespots.domain.model.User
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) {

    fun getUser(): Flow<User> = callbackFlow {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            close(Exception("User not logged in."))
            return@callbackFlow
        }

        val userDocRef = firestore.collection("users").document(userId)
        val listener = userDocRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val user = snapshot.toObject(User::class.java)
                if (user != null) {
                    trySend(user)
                } else {
                    close(Exception("Failed to parse user data."))
                }
            } else {
                close(Exception("User data not found."))
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun getUser(userId: String): Result<User> {
        return try {
            val userDoc = firestore.collection("users").document(userId).get().await()
            val user = userDoc.toObject(User::class.java) ?: throw Exception("User data not found.")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUsers(userIds: List<String>): Result<List<User>> = coroutineScope {
        if (userIds.isEmpty()) {
            return@coroutineScope Result.success(emptyList())
        }
        try {
            val deferreds = userIds.map { userId ->
                async {
                    firestore.collection("users").document(userId).get().await()
                }
            }
            val documents = deferreds.awaitAll()
            val users = documents.mapNotNull { it.toObject(User::class.java) }
            Result.success(users)
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

    suspend fun getTopDjs(): Result<List<User>> {
        return try {
            val usersCollection = firestore.collection("users")
                .orderBy("points", Query.Direction.DESCENDING)
                .get()
                .await()
            val users = usersCollection.toObjects(User::class.java)
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
