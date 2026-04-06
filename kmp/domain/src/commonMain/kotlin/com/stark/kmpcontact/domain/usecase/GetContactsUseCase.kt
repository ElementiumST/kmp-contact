package com.stark.kmpcontact.domain.usecase

import com.stark.kmpcontact.domain.model.Contact
import com.stark.kmpcontact.domain.repository.ContactsRepository

class GetContactsUseCase(
    private val contactsRepository: ContactsRepository,
) {
    suspend operator fun invoke(): List<Contact> = contactsRepository.getContacts()
}
