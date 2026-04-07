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
import com.stark.kmpcontact.data.remote.dto.ContactDto
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
        return try {
            val limit = DEFAULT_PAGE_SIZE
            val offset = (page - 1).coerceAtLeast(0) * limit
            val networkResult = networkRequestExecutor.execute(
                url = "${serverUrlProvider.serverUrl.trimEnd('/')}/contacts?offset=$offset&limit=$limit",
                method = HttpMethod.GET,
                responseClass = ContactsResponseDto::class,
            )
            val uniqueNetworkContacts = networkResult.data.distinctBy { it.stableKey() }

            uniqueNetworkContacts.forEach { contactDto ->
                databaseRequestExecutor.execute(
                    statement = "contacts.upsert",
                    operation = DatabaseOperation.INSERT,
                    arguments = mapOf(
                        "id" to contactDto.stableKey(),
                        "name" to contactDto.name,
                        "phone" to contactDto.phone,
                        "email" to contactDto.email,
                        "interlocutorType" to contactDto.interlocutorType,
                    ),
                )
            }

            ContactsPage(
                data = uniqueNetworkContacts.map { it.toDomain() },
                hasNext = networkResult.hasNext,
                totalCount = uniqueNetworkContacts.size,
            )
        } catch (_: NetworkException) {
            getContactsFromLocalDatabase(page = page)
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

    private suspend fun getContactsFromLocalDatabase(page: Int): ContactsPage {
        val pageSize = DEFAULT_PAGE_SIZE
        val offset = (page - 1).coerceAtLeast(0) * pageSize
        val cachedContacts = databaseRequestExecutor.execute(
            statement = "contacts.selectPage",
            operation = DatabaseOperation.QUERY,
            arguments = mapOf(
                "limit" to pageSize,
                "offset" to offset,
            ),
        )
        val totalCount = databaseRequestExecutor.execute(
            statement = "contacts.count",
            operation = DatabaseOperation.QUERY,
        ) as? Int ?: 0

        val cachedEntities = (cachedContacts as? List<*>)
            .orEmpty()
            .filterIsInstance<ContactEntity>()
            .distinctBy { it.stableKey() }

        if (cachedEntities.isEmpty()) {
            return ContactsPage(
                data = emptyList(),
                hasNext = false,
                totalCount = 0,
            )
        }

        return ContactsPage(
            data = cachedEntities.map(ContactEntity::toDomain),
            hasNext = offset + cachedEntities.size < totalCount,
            totalCount = cachedEntities.size,
        )
    }

    private fun ContactDto.stableKey(): String {
        return contact?.contactId
            ?: profile?.profileId
            ?: email
            ?: phone
            ?: name
    }

    private fun ContactEntity.stableKey(): String {
        return email ?: phone ?: id
    }

    private companion object {
        const val DEFAULT_PAGE_SIZE = 50
    }
}
