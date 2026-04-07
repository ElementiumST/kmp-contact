package com.stark.kmpcontact.domain.model

data class ContactDraft(
    val name: String,
    val email: String? = null,
    val phone: String? = null,
    val note: String? = null,
    val tags: List<String> = emptyList(),
)
