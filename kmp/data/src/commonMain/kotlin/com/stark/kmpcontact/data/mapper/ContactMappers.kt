package com.stark.kmpcontact.data.mapper

import com.stark.kmpcontact.data.local.model.ContactEntity
import com.stark.kmpcontact.data.remote.dto.ContactDto
import com.stark.kmpcontact.domain.model.Contact

fun ContactDto.toDomain(): Contact = Contact(
    id = id,
    firstName = firstName,
    lastName = lastName,
    phoneNumber = phoneNumber,
    email = email,
)

fun ContactEntity.toDomain(): Contact = Contact(
    id = id,
    firstName = firstName,
    lastName = lastName,
    phoneNumber = phoneNumber,
    email = email,
)
