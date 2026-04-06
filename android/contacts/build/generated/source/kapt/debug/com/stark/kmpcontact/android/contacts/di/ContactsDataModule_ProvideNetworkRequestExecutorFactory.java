package com.stark.kmpcontact.android.contacts.di;

import com.google.gson.Gson;
import com.stark.kmpcontact.data.network.NetworkRequestExecutor;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import okhttp3.OkHttpClient;

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
public final class ContactsDataModule_ProvideNetworkRequestExecutorFactory implements Factory<NetworkRequestExecutor> {
  private final Provider<OkHttpClient> okHttpClientProvider;

  private final Provider<Gson> gsonProvider;

  public ContactsDataModule_ProvideNetworkRequestExecutorFactory(
      Provider<OkHttpClient> okHttpClientProvider, Provider<Gson> gsonProvider) {
    this.okHttpClientProvider = okHttpClientProvider;
    this.gsonProvider = gsonProvider;
  }

  @Override
  public NetworkRequestExecutor get() {
    return provideNetworkRequestExecutor(okHttpClientProvider.get(), gsonProvider.get());
  }

  public static ContactsDataModule_ProvideNetworkRequestExecutorFactory create(
      Provider<OkHttpClient> okHttpClientProvider, Provider<Gson> gsonProvider) {
    return new ContactsDataModule_ProvideNetworkRequestExecutorFactory(okHttpClientProvider, gsonProvider);
  }

  public static NetworkRequestExecutor provideNetworkRequestExecutor(OkHttpClient okHttpClient,
      Gson gson) {
    return Preconditions.checkNotNullFromProvides(ContactsDataModule.INSTANCE.provideNetworkRequestExecutor(okHttpClient, gson));
  }
}
