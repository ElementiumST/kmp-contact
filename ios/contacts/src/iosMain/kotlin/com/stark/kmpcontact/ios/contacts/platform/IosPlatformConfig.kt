package com.stark.kmpcontact.ios.contacts.platform

import com.stark.kmpcontact.data.network.ServerUrlProvider
import com.stark.kmpcontact.ios.contacts.IosContactsConfig
import platform.Foundation.NSUserDefaults

data class IosAuthConfig(
    val login: String,
    val password: String,
    val rememberMe: Boolean,
)

class IosServerUrlProvider(
    config: IosContactsConfig,
) : ServerUrlProvider {
    override val serverUrl: String = config.serverUrl
}

class IosAuthSessionStore(
    private val userDefaults: NSUserDefaults = NSUserDefaults.standardUserDefaults,
) {
    fun getSessionId(): String? = userDefaults.stringForKey(SESSION_ID_KEY)

    fun saveSessionId(sessionId: String) {
        userDefaults.setObject(sessionId, forKey = SESSION_ID_KEY)
    }

    fun clear() {
        userDefaults.removeObjectForKey(SESSION_ID_KEY)
    }

    private companion object {
        private const val SESSION_ID_KEY = "contacts.session.id"
    }
}
