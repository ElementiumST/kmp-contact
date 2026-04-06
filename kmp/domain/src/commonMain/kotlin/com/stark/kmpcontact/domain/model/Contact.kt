package com.stark.kmpcontact.domain.model

data class Contact(
    val id: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val email: String? = null,
)
