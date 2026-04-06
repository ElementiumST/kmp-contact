package com.stark.kmpcontact.android.contacts.di;

import android.content.Context;
import com.stark.kmpcontact.android.contacts.data.local.ContactsSQLiteOpenHelper;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class ContactsDataModule_ProvideContactsSQLiteOpenHelperFactory implements Factory<ContactsSQLiteOpenHelper> {
  private final Provider<Context> contextProvider;

  public ContactsDataModule_ProvideContactsSQLiteOpenHelperFactory(
      Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public ContactsSQLiteOpenHelper get() {
    return provideContactsSQLiteOpenHelper(contextProvider.get());
  }

  public static ContactsDataModule_ProvideContactsSQLiteOpenHelperFactory create(
      Provider<Context> contextProvider) {
    return new ContactsDataModule_ProvideContactsSQLiteOpenHelperFactory(contextProvider);
  }

  public static ContactsSQLiteOpenHelper provideContactsSQLiteOpenHelper(Context context) {
    return Preconditions.checkNotNullFromProvides(ContactsDataModule.INSTANCE.provideContactsSQLiteOpenHelper(context));
  }
}
