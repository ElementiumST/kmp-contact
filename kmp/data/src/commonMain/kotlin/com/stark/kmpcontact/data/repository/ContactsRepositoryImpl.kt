package com.stark.kmpcontact.data.repository

import com.stark.kmpcontact.data.database.DatabaseException
import com.stark.kmpcontact.data.database.DatabaseOperation
import com.stark.kmpcontact.data.database.DatabaseRequestExecutor
import com.stark.kmpcontact.data.local.model.ContactEntity
import com.stark.kmpcontact.data.mapper.toDomain
import com.stark.kmpcontact.data.network.HttpMethod
import com.stark.kmpcontact.data.network.NetworkException
import com.stark.kmpcontact.data.network.NetworkRequestExecutor
import com.stark.kmpcontact.data.network.ServerUrlProvider
import com.stark.kmpcontact.data.remote.dto.ContactsResponseDto
import com.stark.kmpcontact.domain.model.Contact
import com.stark.kmpcontact.domain.model.ContactsPage
import com.stark.kmpcontact.domain.repository.ContactsRepository

class ContactsRepositoryImpl(
    private val serverUrlProvider: ServerUrlProvider,
    private val networkRequestExecutor: NetworkRequestExecutor,
    private val databaseRequestExecutor: DatabaseRequestExecutor,
) : ContactsRepository {

    override suspend fun getContacts(page: Int): ContactsPage {
        val networkResult = networkRequestExecutor.execute(
            url = "${serverUrlProvider.serverUrl.trimEnd('/')}/contacts?page=$page",
            method = HttpMethod.GET,
            responseClass = ContactsResponseDto::class,
        )

        networkResult.data.forEach { contactDto ->
            databaseRequestExecutor.execute(
                statement = "contacts.upsert",
                operation = DatabaseOperation.INSERT,
                arguments = mapOf(
                    "id" to (contactDto.contact?.contactId ?: contactDto.name),
                    "name" to contactDto.name,
                    "phone" to contactDto.phone,
                    "email" to contactDto.email,
                    "interlocutorType" to contactDto.interlocutorType,
                ),
            )
        }

        return ContactsPage(
            data = networkResult.data.map { it.toDomain() },
            hasNext = networkResult.hasNext,
            totalCount = networkResult.totalCount,
        )
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
