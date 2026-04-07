package com.stark.kmpcontact.ios.contacts

import com.stark.kmpcontact.data.repository.ContactsRepositoryImpl
import com.stark.kmpcontact.domain.usecase.GetContactsUseCase
import com.stark.kmpcontact.ios.contacts.platform.IosAuthConfig
import com.stark.kmpcontact.ios.contacts.platform.IosAuthSessionStore
import com.stark.kmpcontact.ios.contacts.platform.IosDatabaseRequestExecutor
import com.stark.kmpcontact.ios.contacts.platform.IosNetworkRequestExecutor
import com.stark.kmpcontact.ios.contacts.platform.IosServerUrlProvider

object IosContactsModuleFactory {
    fun create(
        config: IosContactsConfig,
    ): IosContactsModule {
        val serverUrlProvider = IosServerUrlProvider(config = config)
        val authSessionStore = IosAuthSessionStore()
        val authConfig = IosAuthConfig(
            login = config.authLogin,
            password = config.authPassword,
            rememberMe = config.authRememberMe,
        )
        val networkRequestExecutor = IosNetworkRequestExecutor(
            serverUrlProvider = serverUrlProvider,
            authSessionStore = authSessionStore,
            authConfig = authConfig,
        )
        val databaseRequestExecutor = IosDatabaseRequestExecutor()
        val contactsRepository = ContactsRepositoryImpl(
            serverUrlProvider = serverUrlProvider,
            networkRequestExecutor = networkRequestExecutor,
            databaseRequestExecutor = databaseRequestExecutor,
        )

        return IosContactsModule(
            getContactsUseCase = GetContactsUseCase(contactsRepository = contactsRepository),
            contactsRepository = contactsRepository,
        )
    }
}

fun createIosContactsModule(
    config: IosContactsConfig,
): IosContactsModule = IosContactsModuleFactory.create(config)
