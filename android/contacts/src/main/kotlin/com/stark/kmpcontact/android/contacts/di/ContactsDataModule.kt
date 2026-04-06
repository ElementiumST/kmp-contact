package com.stark.kmpcontact.android.contacts.di

import android.content.Context
import com.google.gson.Gson
import com.stark.kmpcontact.android.contacts.BuildConfig
import com.stark.kmpcontact.android.contacts.data.local.ContactsSQLiteOpenHelper
import com.stark.kmpcontact.android.contacts.data.local.SQLiteDatabaseRequestExecutor
import com.stark.kmpcontact.android.contacts.data.remote.OkHttpNetworkRequestExecutor
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
    fun provideContactsSQLiteOpenHelper(
        @ApplicationContext context: Context,
    ): ContactsSQLiteOpenHelper = ContactsSQLiteOpenHelper(context)

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
    ): NetworkRequestExecutor = OkHttpNetworkRequestExecutor(
        okHttpClient = okHttpClient,
        gson = gson,
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
