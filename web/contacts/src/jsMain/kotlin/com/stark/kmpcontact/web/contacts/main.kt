package com.stark.kmpcontact.web.contacts

import com.stark.kmpcontact.data.repository.ContactsRepositoryImpl
import com.stark.kmpcontact.domain.usecase.GetContactsUseCase
import com.stark.kmpcontact.web.contacts.platform.FetchNetworkRequestExecutor
import com.stark.kmpcontact.web.contacts.platform.IndexedDbDatabaseRequestExecutor
import com.stark.kmpcontact.web.contacts.platform.WebContactsConfig
import com.stark.kmpcontact.web.contacts.platform.WebNetworkStatusNotifier
import com.stark.kmpcontact.web.contacts.platform.WebSessionStore
import com.stark.kmpcontact.web.contacts.ui.WebContactsController
import kotlinx.browser.document
import org.w3c.dom.HTMLElement

fun main() {
    val root = document.getElementById("app") as? HTMLElement
        ?: error("Element with id 'app' not found.")

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

    WebContactsController(
        root = root,
        getContactsUseCase = getContactsUseCase,
        networkStatusNotifier = networkStatusNotifier,
    ).start()
}
