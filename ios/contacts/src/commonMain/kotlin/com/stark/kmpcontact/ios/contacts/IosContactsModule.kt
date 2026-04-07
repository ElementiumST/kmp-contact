package com.stark.kmpcontact.ios.contacts

import com.stark.kmpcontact.domain.model.Contact
import com.stark.kmpcontact.domain.repository.ContactsRepository
import com.stark.kmpcontact.domain.usecase.GetContactsUseCase

data class IosContactsConfig(
    val serverUrl: String,
    val authLogin: String,
    val authPassword: String,
    val authRememberMe: Boolean = false,
)

data class IosContactItem(
    val id: String,
    val name: String,
    val email: String? = null,
    val phone: String? = null,
    val interlocutorType: String,
    val contactId: String? = null,
    val contactType: String? = null,
    val ownerProfileId: String? = null,
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
    val contactDeleted: Boolean? = null,
    val contactNote: String? = null,
    val contactTagsText: String? = null,
    val profileId: String? = null,
    val profileUserType: String? = null,
    val avatarResourceId: String? = null,
    val additionalContact: String? = null,
    val aboutSelf: String? = null,
    val companyId: String? = null,
    val isGuest: Boolean? = null,
    val profileDeleted: Boolean? = null,
    val customStatusText: String? = null,
    val ldapUserId: String? = null,
    val ldapTargetsText: String? = null,
    val externalDomainId: String? = null,
    val externalDomainName: String? = null,
    val externalDomainHost: String? = null,
)

data class IosContactsPageResult(
    val contacts: List<IosContactItem>,
    val hasNext: Boolean,
    val totalCount: Int,
)

class IosContactsModule internal constructor(
    private val getContactsUseCase: GetContactsUseCase,
    private val contactsRepository: ContactsRepository,
) {
    suspend fun loadContactsPage(page: Int): IosContactsPageResult {
        val pageResult = getContactsUseCase(page)
        return IosContactsPageResult(
            contacts = pageResult.data.map(Contact::toIosContactItem),
            hasNext = pageResult.hasNext,
            totalCount = pageResult.totalCount,
        )
    }

    suspend fun getContact(contactId: String): IosContactItem? {
        return contactsRepository.getContact(contactId)?.toIosContactItem()
    }
}

private fun Contact.toIosContactItem(): IosContactItem {
    val resolvedId = contact?.contactId
        ?: profile?.profileId
        ?: email
        ?: phone
        ?: name

    return IosContactItem(
        id = resolvedId,
        name = name,
        email = email,
        phone = phone,
        interlocutorType = interlocutorType,
        contactId = contact?.contactId,
        contactType = contact?.type,
        ownerProfileId = contact?.ownerProfileId,
        createdAt = contact?.createdAt,
        updatedAt = contact?.updatedAt,
        contactDeleted = contact?.deleted,
        contactNote = contact?.note,
        contactTagsText = contact?.tags?.joinToString(),
        profileId = profile?.profileId,
        profileUserType = profile?.userType,
        avatarResourceId = profile?.avatarResourceId,
        additionalContact = profile?.additionalContact,
        aboutSelf = profile?.aboutSelf,
        companyId = profile?.companyId,
        isGuest = profile?.isGuest,
        profileDeleted = profile?.deleted,
        customStatusText = profile?.customStatus?.statusText,
        ldapUserId = ldapUser?.ldapUserId,
        ldapTargetsText = ldapUser?.targets?.joinToString(),
        externalDomainId = externalInfo?.externalDomainId,
        externalDomainName = externalInfo?.externalDomainName,
        externalDomainHost = externalInfo?.externalDomainHost,
    )
}
