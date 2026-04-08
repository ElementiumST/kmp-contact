package com.stark.kmpcontact.web.contacts

import com.stark.kmpcontact.data.repository.ContactsRepositoryImpl
import com.stark.kmpcontact.domain.usecase.GetContactsUseCase
import com.stark.kmpcontact.web.contacts.platform.FetchNetworkRequestExecutor
import com.stark.kmpcontact.web.contacts.platform.IndexedDbDatabaseRequestExecutor
import com.stark.kmpcontact.web.contacts.platform.WebContactsConfig
import com.stark.kmpcontact.web.contacts.platform.WebNetworkStatusNotifier
import com.stark.kmpcontact.web.contacts.platform.WebSessionStore
import kotlinx.browser.window

fun main() {
    val networkStatusNotifier = WebNetworkStatusNotifier()
    val repository = ContactsRepositoryImpl(
        serverUrlProvider = WebContactsConfig,
        networkRequestExecutor = FetchNetworkRequestExecutor(
            serverUrlProvider = WebContactsConfig,
            sessionStore = WebSessionStore(),
            authConfig = WebContactsConfig.authConfig,
            networkStatusNotifier = networkStatusNotifier,
        ),
        databaseRequestExecutor = IndexedDbDatabaseRequestExecutor(),
    )
    val getContactsUseCase = GetContactsUseCase(repository)
    val bridge = ContactsBridge(
        getContactsUseCase = getContactsUseCase,
        networkStatusNotifier = networkStatusNotifier,
    )

    window.asDynamic().kmpContactsBridge = createBridgeApi(bridge)
}

private fun createBridgeApi(bridge: ContactsBridge): dynamic {
    val api = js("({})")

    api.subscribe = { listener: (dynamic) -> Unit ->
        bridge.subscribe(listener)
    }
    api.retryLoading = {
        bridge.retryLoading()
    }
    api.loadNextPage = {
        bridge.loadNextPage()
    }
    api.selectContact = { stableKey: String ->
        bridge.selectContact(stableKey)
    }
    api.backToList = {
        bridge.backToList()
    }
    api.dispose = {
        bridge.dispose()
    }

    return api
}
