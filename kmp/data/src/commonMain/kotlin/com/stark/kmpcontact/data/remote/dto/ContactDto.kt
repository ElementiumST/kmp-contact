package com.stark.kmpcontact.data.remote.dto

data class ContactDto(
    val id: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val email: String? = null,
)
