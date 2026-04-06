package com.stark.kmpcontact.domain.model

data class Contact(
    val name: String,
    val email: String? = null,
    val phone: String? = null,
    val interlocutorType: String,
    val contact: ContactDetails? = null,
    val profile: Profile? = null,
    val ldapUser: LdapUser? = null,
    val externalInfo: ExternalInfo? = null,
)
