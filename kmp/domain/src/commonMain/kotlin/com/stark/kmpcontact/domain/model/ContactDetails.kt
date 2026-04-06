package com.stark.kmpcontact.domain.model

data class ContactDetails(
    val contactId: String,
    val type: String,
    val ownerProfileId: String? = null,
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
    val deleted: Boolean = false,
    val note: String? = null,
    val tags: List<String> = emptyList(),
)
