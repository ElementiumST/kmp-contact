package com.stark.kmpcontact.domain.repository

import com.stark.kmpcontact.domain.model.Contact

interface ContactsRepository {
    suspend fun getContacts(): List<Contact>

    suspend fun getContact(contactId: String): Contact?
}
