package com.stark.kmpcontact.android.contacts.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.stark.kmpcontact.domain.model.Contact
import com.stark.kmpcontact.domain.usecase.GetContactsUseCase

class ContactsPagingSource(
    private val getContactsUseCase: GetContactsUseCase,
) : PagingSource<Int, Contact>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Contact> {
        return try {
            val page = params.key ?: 1
            val contactsPage = getContactsUseCase(page = page)

            LoadResult.Page(
                data = contactsPage.data,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (contactsPage.hasNext) page + 1 else null,
            )
        } catch (throwable: Throwable) {
            LoadResult.Error(throwable)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Contact>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val anchorPage = state.closestPageToPosition(anchorPosition) ?: return null

        return anchorPage.prevKey?.plus(1) ?: anchorPage.nextKey?.minus(1)
    }
}
