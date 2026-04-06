package com.stark.kmpcontact.domain.model

data class LdapUser(
    val ldapUserId: String,
    val targets: List<String> = emptyList(),
)
