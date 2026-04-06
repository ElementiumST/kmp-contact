package com.stark.kmpcontact.domain.model

data class Profile(
    val profileId: String,
    val userType: String,
    val avatarResourceId: String? = null,
    val additionalContact: String? = null,
    val aboutSelf: String? = null,
    val companyId: String? = null,
    val isGuest: Boolean = false,
    val deleted: Boolean = false,
    val customStatus: CustomStatus? = null,
)

data class CustomStatus(
    val statusText: String? = null,
)
