package com.stark.kmpcontact.support.paging

import com.stark.kmpcontact.domain.model.Contact
import com.stark.kmpcontact.domain.usecase.GetContactsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val FIRST_PAGE = 1

data class PagingError(
    val message: String,
)

data class PagingState<T>(
    val items: List<T> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasNext: Boolean = true,
    val nextPage: Int = FIRST_PAGE,
    val error: PagingError? = null,
)

data class PagingChunk<T>(
    val items: List<T>,
    val hasNext: Boolean,
)

class PageNumberPaginator<T, K>(
    private val scope: CoroutineScope,
    private val firstPage: Int = FIRST_PAGE,
    private val loadPage: suspend (page: Int) -> PagingChunk<T>,
    private val keySelector: ((T) -> K)? = null,
) {
    private val _state = MutableStateFlow(PagingState<T>(nextPage = firstPage))
    val state: StateFlow<PagingState<T>> = _state.asStateFlow()

    private var loadJob: Job? = null
    private var lastFailedRequest: FailedRequest? = null

    fun refresh() {
        requestPage(page = firstPage, replace = true)
    }

    fun loadNextPage() {
        val currentState = _state.value
        if (loadJob?.isActive == true || !currentState.hasNext) return
        requestPage(page = currentState.nextPage, replace = false)
    }

    fun retry() {
        val failedRequest = lastFailedRequest ?: return
        if (loadJob?.isActive == true) return
        requestPage(page = failedRequest.page, replace = failedRequest.replace)
    }

    private fun requestPage(
        page: Int,
        replace: Boolean,
    ) {
        if (loadJob?.isActive == true) return

        loadJob = scope.launch {
            val previousState = _state.value

            _state.value = if (replace) {
                previousState.copy(
                    isLoading = true,
                    isLoadingMore = false,
                    error = null,
                    hasNext = true,
                    nextPage = firstPage,
                )
            } else {
                previousState.copy(
                    isLoading = false,
                    isLoadingMore = true,
                    error = null,
                )
            }

            try {
                val chunk = loadPage(page)
                val mergedItems = if (replace) {
                    deduplicate(chunk.items)
                } else {
                    deduplicate(previousState.items + chunk.items)
                }

                _state.value = PagingState(
                    items = mergedItems,
                    isLoading = false,
                    isLoadingMore = false,
                    hasNext = chunk.hasNext,
                    nextPage = if (chunk.hasNext) page + 1 else page,
                    error = null,
                )
                lastFailedRequest = null
            } catch (throwable: Throwable) {
                _state.value = previousState.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    error = PagingError(
                        message = throwable.message ?: defaultErrorMessage(replace = replace),
                    ),
                )
                lastFailedRequest = FailedRequest(page = page, replace = replace)
            }
        }
    }

    private fun deduplicate(items: List<T>): List<T> {
        val selector = keySelector ?: return items
        return items.distinctBy(selector)
    }

    private fun defaultErrorMessage(replace: Boolean): String {
        return if (replace) {
            "Failed to load items."
        } else {
            "Failed to load next page."
        }
    }

    private data class FailedRequest(
        val page: Int,
        val replace: Boolean,
    )
}

class ContactsPaginator(
    scope: CoroutineScope,
    getContactsUseCase: GetContactsUseCase,
) {
    private val delegate = PageNumberPaginator(
        scope = scope,
        loadPage = { page ->
            val contactsPage = getContactsUseCase(page = page)
            PagingChunk(
                items = contactsPage.data,
                hasNext = contactsPage.hasNext,
            )
        },
        keySelector = Contact::stableKey,
    )

    val state: StateFlow<PagingState<Contact>> = delegate.state

    fun refresh() {
        delegate.refresh()
    }

    fun loadNextPage() {
        delegate.loadNextPage()
    }

    fun retry() {
        delegate.retry()
    }
}

private fun Contact.stableKey(): String {
    return contact?.contactId
        ?: profile?.profileId
        ?: email
        ?: phone
        ?: name
}
