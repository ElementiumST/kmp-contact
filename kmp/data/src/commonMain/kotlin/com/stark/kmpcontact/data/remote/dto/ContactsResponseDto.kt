package com.stark.kmpcontact.data.remote.dto

data class ContactsResponseDto(
    val data: List<ContactDto>,
    val hasNext: Boolean,
    val totalCount: Int,
)
