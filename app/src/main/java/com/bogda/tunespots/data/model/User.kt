package com.bogda.tunespots.domain.model

data class User(
    val id: String = "",
    val email: String = "",

    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phoneNumber: String = "",
    val profilePictureUrl: String? = null,

    val points: Long = 0
)
