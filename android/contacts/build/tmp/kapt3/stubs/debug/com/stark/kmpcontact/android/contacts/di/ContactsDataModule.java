package com.stark.kmpcontact.android.contacts.di;

import android.content.Context;
import com.google.gson.Gson;
import com.stark.kmpcontact.android.contacts.BuildConfig;
import com.stark.kmpcontact.android.contacts.data.local.ContactsSQLiteOpenHelper;
import com.stark.kmpcontact.android.contacts.data.local.SQLiteDatabaseRequestExecutor;
import com.stark.kmpcontact.android.contacts.data.remote.OkHttpNetworkRequestExecutor;
import com.stark.kmpcontact.data.database.DatabaseRequestExecutor;
import com.stark.kmpcontact.data.network.NetworkRequestExecutor;
import com.stark.kmpcontact.data.network.ServerUrlProvider;
import com.stark.kmpcontact.data.repository.ContactsRepositoryImpl;
import com.stark.kmpcontact.domain.repository.ContactsRepository;
import com.stark.kmpcontact.domain.usecase.GetContactsUseCase;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import javax.inject.Singleton;

@dagger.Module()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000J\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J \u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nH\u0007J\u0012\u0010\u000b\u001a\u00020\f2\b\b\u0001\u0010\r\u001a\u00020\u000eH\u0007J\u0010\u0010\u000f\u001a\u00020\n2\u0006\u0010\u0010\u001a\u00020\fH\u0007J\u0010\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u0004H\u0007J\b\u0010\u0014\u001a\u00020\u0015H\u0007J\u0018\u0010\u0016\u001a\u00020\b2\u0006\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\u0015H\u0007J\b\u0010\u001a\u001a\u00020\u0018H\u0007J\b\u0010\u001b\u001a\u00020\u0006H\u0007\u00a8\u0006\u001c"}, d2 = {"Lcom/stark/kmpcontact/android/contacts/di/ContactsDataModule;", "", "()V", "provideContactsRepository", "Lcom/stark/kmpcontact/domain/repository/ContactsRepository;", "serverUrlProvider", "Lcom/stark/kmpcontact/data/network/ServerUrlProvider;", "networkRequestExecutor", "Lcom/stark/kmpcontact/data/network/NetworkRequestExecutor;", "databaseRequestExecutor", "Lcom/stark/kmpcontact/data/database/DatabaseRequestExecutor;", "provideContactsSQLiteOpenHelper", "Lcom/stark/kmpcontact/android/contacts/data/local/ContactsSQLiteOpenHelper;", "context", "Landroid/content/Context;", "provideDatabaseRequestExecutor", "contactsSQLiteOpenHelper", "provideGetContactsUseCase", "Lcom/stark/kmpcontact/domain/usecase/GetContactsUseCase;", "contactsRepository", "provideGson", "Lcom/google/gson/Gson;", "provideNetworkRequestExecutor", "okHttpClient", "Lokhttp3/OkHttpClient;", "gson", "provideOkHttpClient", "provideServerUrlProvider", "contacts_debug"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public final class ContactsDataModule {
    @org.jetbrains.annotations.NotNull()
    public static final com.stark.kmpcontact.android.contacts.di.ContactsDataModule INSTANCE = null;
    
    private ContactsDataModule() {
        super();
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.google.gson.Gson provideGson() {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final okhttp3.OkHttpClient provideOkHttpClient() {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.stark.kmpcontact.android.contacts.data.local.ContactsSQLiteOpenHelper provideContactsSQLiteOpenHelper(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.stark.kmpcontact.data.network.ServerUrlProvider provideServerUrlProvider() {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.stark.kmpcontact.data.network.NetworkRequestExecutor provideNetworkRequestExecutor(@org.jetbrains.annotations.NotNull()
    okhttp3.OkHttpClient okHttpClient, @org.jetbrains.annotations.NotNull()
    com.google.gson.Gson gson) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.stark.kmpcontact.data.database.DatabaseRequestExecutor provideDatabaseRequestExecutor(@org.jetbrains.annotations.NotNull()
    com.stark.kmpcontact.android.contacts.data.local.ContactsSQLiteOpenHelper contactsSQLiteOpenHelper) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.stark.kmpcontact.domain.repository.ContactsRepository provideContactsRepository(@org.jetbrains.annotations.NotNull()
    com.stark.kmpcontact.data.network.ServerUrlProvider serverUrlProvider, @org.jetbrains.annotations.NotNull()
    com.stark.kmpcontact.data.network.NetworkRequestExecutor networkRequestExecutor, @org.jetbrains.annotations.NotNull()
    com.stark.kmpcontact.data.database.DatabaseRequestExecutor databaseRequestExecutor) {
        return null;
    }
    
    @dagger.Provides()
    @org.jetbrains.annotations.NotNull()
    public final com.stark.kmpcontact.domain.usecase.GetContactsUseCase provideGetContactsUseCase(@org.jetbrains.annotations.NotNull()
    com.stark.kmpcontact.domain.repository.ContactsRepository contactsRepository) {
        return null;
    }
}