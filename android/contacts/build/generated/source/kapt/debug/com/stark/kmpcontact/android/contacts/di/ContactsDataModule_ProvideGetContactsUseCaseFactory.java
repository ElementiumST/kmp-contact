package com.stark.kmpcontact.android.contacts.di;

import com.stark.kmpcontact.domain.repository.ContactsRepository;
import com.stark.kmpcontact.domain.usecase.GetContactsUseCase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class ContactsDataModule_ProvideGetContactsUseCaseFactory implements Factory<GetContactsUseCase> {
  private final Provider<ContactsRepository> contactsRepositoryProvider;

  public ContactsDataModule_ProvideGetContactsUseCaseFactory(
      Provider<ContactsRepository> contactsRepositoryProvider) {
    this.contactsRepositoryProvider = contactsRepositoryProvider;
  }

  @Override
  public GetContactsUseCase get() {
    return provideGetContactsUseCase(contactsRepositoryProvider.get());
  }

  public static ContactsDataModule_ProvideGetContactsUseCaseFactory create(
      Provider<ContactsRepository> contactsRepositoryProvider) {
    return new ContactsDataModule_ProvideGetContactsUseCaseFactory(contactsRepositoryProvider);
  }

  public static GetContactsUseCase provideGetContactsUseCase(
      ContactsRepository contactsRepository) {
    return Preconditions.checkNotNullFromProvides(ContactsDataModule.INSTANCE.provideGetContactsUseCase(contactsRepository));
  }
}
