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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u0007J\u0006\u0010\u0014\u001a\u00020\u0012R\u0016\u0010\u0005\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00070\n0\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0019\u0010\r\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00070\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010\u00a8\u0006\u0015"}, d2 = {"Lcom/stark/kmpcontact/android/contacts/presentation/ContactsViewModel;", "Landroidx/lifecycle/ViewModel;", "getContactsUseCase", "Lcom/stark/kmpcontact/domain/usecase/GetContactsUseCase;", "(Lcom/stark/kmpcontact/domain/usecase/GetContactsUseCase;)V", "_selectedContact", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/stark/kmpcontact/domain/model/Contact;", "contactsPagingData", "Lkotlinx/coroutines/flow/Flow;", "Landroidx/paging/PagingData;", "getContactsPagingData", "()Lkotlinx/coroutines/flow/Flow;", "selectedContact", "Lkotlinx/coroutines/flow/StateFlow;", "getSelectedContact", "()Lkotlinx/coroutines/flow/StateFlow;", "onContactClick", "", "contact", "onContactInfoBack", "contacts_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class ContactsViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.Flow<androidx.paging.PagingData<com.stark.kmpcontact.domain.model.Contact>> contactsPagingData = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.stark.kmpcontact.domain.model.Contact> _selectedContact = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.stark.kmpcontact.domain.model.Contact> selectedContact = null;
    
    @javax.inject.Inject()
    public ContactsViewModel(@org.jetbrains.annotations.NotNull()
    com.stark.kmpcontact.domain.usecase.GetContactsUseCase getContactsUseCase) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<androidx.paging.PagingData<com.stark.kmpcontact.domain.model.Contact>> getContactsPagingData() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.stark.kmpcontact.domain.model.Contact> getSelectedContact() {
        return null;
    }
    
    public final void onContactClick(@org.jetbrains.annotations.NotNull()
    com.stark.kmpcontact.domain.model.Contact contact) {
    }
    
    public final void onContactInfoBack() {
    }
}