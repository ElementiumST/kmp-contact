package com.stark.kmpcontact.data.remote.dto

data class ContactDto(
    val name: String,
    val email: String? = null,
    val phone: String? = null,
    val interlocutorType: String,
    val contact: ContactDetailsDto? = null,
    val profile: ProfileDto? = null,
    val ldapUser: LdapUserDto? = null,
    val externalInfo: ExternalInfoDto? = null,
)

data class ContactDetailsDto(
    val contactId: String,
    val type: String,
    val ownerProfileId: String? = null,
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
    val deleted: Boolean = false,
    val note: String? = null,
    val tags: List<String> = emptyList(),
)

data class ProfileDto(
    val profileId: String,
    val userType: String,
    val avatarResourceId: String? = null,
    val additionalContact: String? = null,
    val aboutSelf: String? = null,
    val companyId: String? = null,
    val isGuest: Boolean = false,
    val deleted: Boolean = false,
    val customStatus: CustomStatusDto? = null,
)

data class CustomStatusDto(
    val statusText: String? = null,
)

data class LdapUserDto(
    val ldapUserId: String,
    val targets: List<String> = emptyList(),
)

data class ExternalInfoDto(
    val externalDomainId: String? = null,
    val externalDomainName: String? = null,
    val externalDomainHost: String? = null,
)
