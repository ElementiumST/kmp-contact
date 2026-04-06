package com.stark.kmpcontact.domain.model

data class ContactsPage(
    val data: List<Contact>,
    val hasNext: Boolean,
    val totalCount: Int,
)
