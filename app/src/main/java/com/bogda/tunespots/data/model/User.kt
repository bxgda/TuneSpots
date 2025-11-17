package com.bogda.tunespots.domain.model

import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    val id: String = "",
    val email: String = "",

    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phoneNumber: String = "",
    val profilePictureUrl: String? = null,

    val points: Long = 0,

    // Polja za notifikacije i lokaciju
    val fcmToken: String? = null,
    val lastLocation: GeoPoint? = null
)
