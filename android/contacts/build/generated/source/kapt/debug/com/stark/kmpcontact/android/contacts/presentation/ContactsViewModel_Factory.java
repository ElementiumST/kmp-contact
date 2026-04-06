package com.stark.kmpcontact.android.contacts.presentation;

import com.stark.kmpcontact.domain.usecase.GetContactsUseCase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation"
})
public final class ContactsViewModel_Factory implements Factory<ContactsViewModel> {
  private final Provider<GetContactsUseCase> getContactsUseCaseProvider;

  public ContactsViewModel_Factory(Provider<GetContactsUseCase> getContactsUseCaseProvider) {
    this.getContactsUseCaseProvider = getContactsUseCaseProvider;
  }

  @Override
  public ContactsViewModel get() {
    return newInstance(getContactsUseCaseProvider.get());
  }

  public static ContactsViewModel_Factory create(
      Provider<GetContactsUseCase> getContactsUseCaseProvider) {
    return new ContactsViewModel_Factory(getContactsUseCaseProvider);
  }

  public static ContactsViewModel newInstance(GetContactsUseCase getContactsUseCase) {
    return new ContactsViewModel(getContactsUseCase);
  }
}
