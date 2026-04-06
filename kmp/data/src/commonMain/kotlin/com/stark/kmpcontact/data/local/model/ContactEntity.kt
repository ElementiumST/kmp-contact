package com.stark.kmpcontact.data.local.model

data class ContactEntity(
    val id: String,
    val name: String,
    val phone: String?,
    val email: String? = null,
    val interlocutorType: String,
)
