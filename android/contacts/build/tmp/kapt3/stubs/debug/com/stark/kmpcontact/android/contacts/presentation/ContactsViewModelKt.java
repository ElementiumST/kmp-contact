package com.stark.kmpcontact.android.contacts.presentation;

import androidx.lifecycle.ViewModel;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import com.stark.kmpcontact.android.contacts.paging.ContactsPagingSource;
import com.stark.kmpcontact.domain.model.Contact;
import com.stark.kmpcontact.domain.usecase.GetContactsUseCase;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.Flow;
import kotlinx.coroutines.flow.StateFlow;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000\b\n\u0000\n\u0002\u0010\b\n\u0000\"\u000e\u0010\u0000\u001a\u00020\u0001X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0002"}, d2 = {"CONTACTS_PAGE_SIZE", "", "contacts_debug"})
public final class ContactsViewModelKt {
    private static final int CONTACTS_PAGE_SIZE = 20;
}