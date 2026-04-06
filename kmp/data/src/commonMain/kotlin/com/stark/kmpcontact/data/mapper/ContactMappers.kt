package com.stark.kmpcontact.data.mapper

import com.stark.kmpcontact.data.local.model.ContactEntity
import com.stark.kmpcontact.data.remote.dto.ContactDto
import com.stark.kmpcontact.data.remote.dto.ContactDetailsDto
import com.stark.kmpcontact.data.remote.dto.CustomStatusDto
import com.stark.kmpcontact.data.remote.dto.ExternalInfoDto
import com.stark.kmpcontact.data.remote.dto.LdapUserDto
import com.stark.kmpcontact.data.remote.dto.ProfileDto
import com.stark.kmpcontact.domain.model.Contact
import com.stark.kmpcontact.domain.model.ContactDetails
import com.stark.kmpcontact.domain.model.CustomStatus
import com.stark.kmpcontact.domain.model.ExternalInfo
import com.stark.kmpcontact.domain.model.LdapUser
import com.stark.kmpcontact.domain.model.Profile

fun ContactDto.toDomain(): Contact = Contact(
    name = name,
    email = email,
    phone = phone,
    interlocutorType = interlocutorType,
    contact = contact?.toDomain(),
    profile = profile?.toDomain(),
    ldapUser = ldapUser?.toDomain(),
    externalInfo = externalInfo?.toDomain(),
)

fun ContactEntity.toDomain(): Contact = Contact(
    name = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" "),
    email = email,
    phone = phoneNumber,
    interlocutorType = "UNKNOWN",
)

fun ContactDetailsDto.toDomain(): ContactDetails = ContactDetails(
    contactId = contactId,
    type = type,
    ownerProfileId = ownerProfileId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deleted = deleted,
    note = note,
    tags = tags,
)

fun ProfileDto.toDomain(): Profile = Profile(
    profileId = profileId,
    userType = userType,
    avatarResourceId = avatarResourceId,
    additionalContact = additionalContact,
    aboutSelf = aboutSelf,
    companyId = companyId,
    isGuest = isGuest,
    deleted = deleted,
    customStatus = customStatus?.toDomain(),
)

fun CustomStatusDto.toDomain(): CustomStatus = CustomStatus(
    statusText = statusText,
)

fun LdapUserDto.toDomain(): LdapUser = LdapUser(
    ldapUserId = ldapUserId,
    targets = targets,
)

fun ExternalInfoDto.toDomain(): ExternalInfo = ExternalInfo(
    externalDomainId = externalDomainId,
    externalDomainName = externalDomainName,
    externalDomainHost = externalDomainHost,
)
