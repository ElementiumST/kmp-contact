package com.stark.kmpcontact.data.local.model

data class ContactEntity(
    val id: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val email: String? = null,
)
