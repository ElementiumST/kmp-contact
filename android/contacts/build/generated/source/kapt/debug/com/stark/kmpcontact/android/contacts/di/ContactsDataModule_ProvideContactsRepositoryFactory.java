package com.stark.kmpcontact.android.contacts.di;

import com.stark.kmpcontact.data.database.DatabaseRequestExecutor;
import com.stark.kmpcontact.data.network.NetworkRequestExecutor;
import com.stark.kmpcontact.data.network.ServerUrlProvider;
import com.stark.kmpcontact.domain.repository.ContactsRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class ContactsDataModule_ProvideContactsRepositoryFactory implements Factory<ContactsRepository> {
  private final Provider<ServerUrlProvider> serverUrlProvider;

  private final Provider<NetworkRequestExecutor> networkRequestExecutorProvider;

  private final Provider<DatabaseRequestExecutor> databaseRequestExecutorProvider;

  public ContactsDataModule_ProvideContactsRepositoryFactory(
      Provider<ServerUrlProvider> serverUrlProvider,
      Provider<NetworkRequestExecutor> networkRequestExecutorProvider,
      Provider<DatabaseRequestExecutor> databaseRequestExecutorProvider) {
    this.serverUrlProvider = serverUrlProvider;
    this.networkRequestExecutorProvider = networkRequestExecutorProvider;
    this.databaseRequestExecutorProvider = databaseRequestExecutorProvider;
  }

  @Override
  public ContactsRepository get() {
    return provideContactsRepository(serverUrlProvider.get(), networkRequestExecutorProvider.get(), databaseRequestExecutorProvider.get());
  }

  public static ContactsDataModule_ProvideContactsRepositoryFactory create(
      Provider<ServerUrlProvider> serverUrlProvider,
      Provider<NetworkRequestExecutor> networkRequestExecutorProvider,
      Provider<DatabaseRequestExecutor> databaseRequestExecutorProvider) {
    return new ContactsDataModule_ProvideContactsRepositoryFactory(serverUrlProvider, networkRequestExecutorProvider, databaseRequestExecutorProvider);
  }

  public static ContactsRepository provideContactsRepository(ServerUrlProvider serverUrlProvider,
      NetworkRequestExecutor networkRequestExecutor,
      DatabaseRequestExecutor databaseRequestExecutor) {
    return Preconditions.checkNotNullFromProvides(ContactsDataModule.INSTANCE.provideContactsRepository(serverUrlProvider, networkRequestExecutor, databaseRequestExecutor));
  }
}
