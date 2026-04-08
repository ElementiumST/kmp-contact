package com.stark.kmpcontact.web.contacts.platform

import com.stark.kmpcontact.data.network.ServerUrlProvider

object WebContactsConfig : ServerUrlProvider {
    override val serverUrl: String = "https://alpha.hi-tech.org/api/rest"

    val authConfig: WebAuthConfig = WebAuthConfig(
        login = "mobileuser3@testivcs.su",
        password = "test",
        rememberMe = false,
    )
}
