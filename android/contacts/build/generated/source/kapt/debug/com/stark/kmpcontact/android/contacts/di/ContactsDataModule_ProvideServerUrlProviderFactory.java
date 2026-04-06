package com.stark.kmpcontact.android.contacts.di;

import com.stark.kmpcontact.data.network.ServerUrlProvider;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class ContactsDataModule_ProvideServerUrlProviderFactory implements Factory<ServerUrlProvider> {
  @Override
  public ServerUrlProvider get() {
    return provideServerUrlProvider();
  }

  public static ContactsDataModule_ProvideServerUrlProviderFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ServerUrlProvider provideServerUrlProvider() {
    return Preconditions.checkNotNullFromProvides(ContactsDataModule.INSTANCE.provideServerUrlProvider());
  }

  private static final class InstanceHolder {
    private static final ContactsDataModule_ProvideServerUrlProviderFactory INSTANCE = new ContactsDataModule_ProvideServerUrlProviderFactory();
  }
}
