package com.stark.kmpcontact.domain.repository

import com.stark.kmpcontact.domain.model.Contact
import com.stark.kmpcontact.domain.model.ContactsPage

interface ContactsRepository {
    suspend fun getContacts(page: Int): ContactsPage

    suspend fun getContact(contactId: String): Contact?
}
