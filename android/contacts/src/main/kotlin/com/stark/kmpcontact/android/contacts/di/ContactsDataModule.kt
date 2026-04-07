package com.stark.kmpcontact.android.contacts.di

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.stark.kmpcontact.android.contacts.BuildConfig
import com.stark.kmpcontact.android.contacts.data.auth.AuthConfig
import com.stark.kmpcontact.android.contacts.data.auth.AuthSessionStore
import com.stark.kmpcontact.android.contacts.data.local.ContactsSQLiteOpenHelper
import com.stark.kmpcontact.android.contacts.data.local.SQLiteDatabaseRequestExecutor
import com.stark.kmpcontact.android.contacts.data.remote.OkHttpNetworkRequestExecutor
import com.stark.kmpcontact.android.contacts.data.remote.NetworkStatusNotifier
import com.stark.kmpcontact.data.database.DatabaseRequestExecutor
import com.stark.kmpcontact.data.network.NetworkRequestExecutor
import com.stark.kmpcontact.data.network.ServerUrlProvider
import com.stark.kmpcontact.data.repository.ContactsRepositoryImpl
import com.stark.kmpcontact.domain.repository.ContactsRepository
import com.stark.kmpcontact.domain.usecase.GetContactsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ContactsDataModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder().build()

    @Provides
    @Singleton
    fun provideSharedPreferences(
        @ApplicationContext context: Context,
    ): SharedPreferences = context.getSharedPreferences("contacts_auth", Context.MODE_PRIVATE)

    @Provides
    @Singleton
    fun provideContactsSQLiteOpenHelper(
        @ApplicationContext context: Context,
    ): ContactsSQLiteOpenHelper = ContactsSQLiteOpenHelper(context)

    @Provides
    @Singleton
    fun provideAuthSessionStore(
        sharedPreferences: SharedPreferences,
    ): AuthSessionStore = AuthSessionStore(sharedPreferences)

    @Provides
    @Singleton
    fun provideAuthConfig(): AuthConfig = AuthConfig(
        login = BuildConfig.AUTH_LOGIN,
        password = BuildConfig.AUTH_PASSWORD,
        rememberMe = BuildConfig.AUTH_REMEMBER_ME,
    )

    @Provides
    @Singleton
    fun provideNetworkStatusNotifier(): NetworkStatusNotifier = NetworkStatusNotifier()

    @Provides
    @Singleton
    fun provideServerUrlProvider(): ServerUrlProvider = object : ServerUrlProvider {
        override val serverUrl: String = BuildConfig.SERVER_URL
    }

    @Provides
    @Singleton
    fun provideNetworkRequestExecutor(
        okHttpClient: OkHttpClient,
        gson: Gson,
        serverUrlProvider: ServerUrlProvider,
        authSessionStore: AuthSessionStore,
        authConfig: AuthConfig,
        networkStatusNotifier: NetworkStatusNotifier,
    ): NetworkRequestExecutor = OkHttpNetworkRequestExecutor(
        okHttpClient = okHttpClient,
        gson = gson,
        serverUrlProvider = serverUrlProvider,
        authSessionStore = authSessionStore,
        authConfig = authConfig,
        networkStatusNotifier = networkStatusNotifier,
    )

    @Provides
    @Singleton
    fun provideDatabaseRequestExecutor(
        contactsSQLiteOpenHelper: ContactsSQLiteOpenHelper,
    ): DatabaseRequestExecutor = SQLiteDatabaseRequestExecutor(
        contactsSQLiteOpenHelper = contactsSQLiteOpenHelper,
    )

    @Provides
    @Singleton
    fun provideContactsRepository(
        serverUrlProvider: ServerUrlProvider,
        networkRequestExecutor: NetworkRequestExecutor,
        databaseRequestExecutor: DatabaseRequestExecutor,
    ): ContactsRepository = ContactsRepositoryImpl(
        serverUrlProvider = serverUrlProvider,
        networkRequestExecutor = networkRequestExecutor,
        databaseRequestExecutor = databaseRequestExecutor,
    )

    @Provides
    fun provideGetContactsUseCase(
        contactsRepository: ContactsRepository,
    ): GetContactsUseCase = GetContactsUseCase(
        contactsRepository = contactsRepository,
    )
}
