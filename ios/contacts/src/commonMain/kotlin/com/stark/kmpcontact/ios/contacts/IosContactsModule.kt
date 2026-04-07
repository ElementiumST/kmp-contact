package com.stark.kmpcontact.ios.contacts

import com.stark.kmpcontact.domain.usecase.GetContactsUseCase
import com.stark.kmpcontact.support.paging.ContactsPaginator
import kotlinx.coroutines.CoroutineScope

object IosContactsModule {
    fun createContactsPaginator(
        scope: CoroutineScope,
        getContactsUseCase: GetContactsUseCase,
    ): ContactsPaginator = ContactsPaginator(
        scope = scope,
        getContactsUseCase = getContactsUseCase,
    )
}
