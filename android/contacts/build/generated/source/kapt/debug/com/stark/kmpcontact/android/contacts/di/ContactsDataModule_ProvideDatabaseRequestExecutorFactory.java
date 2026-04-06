package com.stark.kmpcontact.android.contacts.di;

import com.stark.kmpcontact.android.contacts.data.local.ContactsSQLiteOpenHelper;
import com.stark.kmpcontact.data.database.DatabaseRequestExecutor;
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
public final class ContactsDataModule_ProvideDatabaseRequestExecutorFactory implements Factory<DatabaseRequestExecutor> {
  private final Provider<ContactsSQLiteOpenHelper> contactsSQLiteOpenHelperProvider;

  public ContactsDataModule_ProvideDatabaseRequestExecutorFactory(
      Provider<ContactsSQLiteOpenHelper> contactsSQLiteOpenHelperProvider) {
    this.contactsSQLiteOpenHelperProvider = contactsSQLiteOpenHelperProvider;
  }

  @Override
  public DatabaseRequestExecutor get() {
    return provideDatabaseRequestExecutor(contactsSQLiteOpenHelperProvider.get());
  }

  public static ContactsDataModule_ProvideDatabaseRequestExecutorFactory create(
      Provider<ContactsSQLiteOpenHelper> contactsSQLiteOpenHelperProvider) {
    return new ContactsDataModule_ProvideDatabaseRequestExecutorFactory(contactsSQLiteOpenHelperProvider);
  }

  public static DatabaseRequestExecutor provideDatabaseRequestExecutor(
      ContactsSQLiteOpenHelper contactsSQLiteOpenHelper) {
    return Preconditions.checkNotNullFromProvides(ContactsDataModule.INSTANCE.provideDatabaseRequestExecutor(contactsSQLiteOpenHelper));
  }
}
