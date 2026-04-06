package com.stark.kmpcontact.domain.usecase

import com.stark.kmpcontact.domain.model.ContactsPage
import com.stark.kmpcontact.domain.repository.ContactsRepository

class GetContactsUseCase(
    private val contactsRepository: ContactsRepository,
) {
    suspend operator fun invoke(page: Int): ContactsPage = contactsRepository.getContacts(page = page)
}
