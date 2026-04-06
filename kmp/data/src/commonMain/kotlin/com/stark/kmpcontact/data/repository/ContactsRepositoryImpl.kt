package com.stark.kmpcontact.data.repository

import com.stark.kmpcontact.data.database.DatabaseException
import com.stark.kmpcontact.data.database.DatabaseOperation
import com.stark.kmpcontact.data.database.DatabaseRequestExecutor
import com.stark.kmpcontact.data.local.model.ContactEntity
import com.stark.kmpcontact.data.mapper.toDomain
import com.stark.kmpcontact.data.network.HttpMethod
import com.stark.kmpcontact.data.network.NetworkException
import com.stark.kmpcontact.data.network.NetworkRequestExecutor
import com.stark.kmpcontact.data.remote.dto.ContactDto
import com.stark.kmpcontact.data.remote.dto.ContactsResponseDto
import com.stark.kmpcontact.domain.model.Contact
import com.stark.kmpcontact.domain.repository.ContactsRepository

class ContactsRepositoryImpl(
    private val networkRequestExecutor: NetworkRequestExecutor,
    private val databaseRequestExecutor: DatabaseRequestExecutor,
) : ContactsRepository {

    override suspend fun getContacts(): List<Contact> {
        val cachedContacts = databaseRequestExecutor.execute(
            statement = "contacts.selectAll",
            operation = DatabaseOperation.QUERY,
        )
        if (cachedContacts is List<*>) {
            val cachedEntityContacts = cachedContacts.filterIsInstance<ContactEntity>().takeIf { it.isNotEmpty() }
            if (cachedEntityContacts != null) {
                return cachedEntityContacts.map(ContactEntity::toDomain)
            }

            val cachedDomainContacts = cachedContacts.filterIsInstance<Contact>().takeIf { it.isNotEmpty() }
            if (cachedDomainContacts != null) {
                return cachedDomainContacts
            }
        }

        return when (val networkResult = networkRequestExecutor.execute(
            url = "/contacts",
            method = HttpMethod.GET,
        )) {
            is ContactsResponseDto -> networkResult.contacts.map(ContactDto::toDomain)
            is NetworkException -> throw networkResult
            else -> throw NetworkException(
                code = null,
                message = "Unexpected network response for contacts list.",
            )
        }
    }

    override suspend fun getContact(contactId: String): Contact? {
        return when (val databaseResult = databaseRequestExecutor.execute(
            statement = "contacts.findById",
            operation = DatabaseOperation.QUERY,
            arguments = mapOf("contactId" to contactId),
        )) {
            is Contact -> databaseResult
            is ContactEntity -> databaseResult.toDomain()
            is DatabaseException -> throw databaseResult
            null -> null
            else -> throw DatabaseException(
                code = null,
                message = "Unexpected database response for contact lookup.",
            )
        }
    }
}
