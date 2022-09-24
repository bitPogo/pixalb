/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.app.di

import android.content.Context
import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.bitpogo.pixalb.album.AlbumContract
import io.bitpogo.pixalb.album.database.PixabayDataBase
import io.bitpogo.pixalb.album.domain.AlbumStore
import io.bitpogo.pixalb.app.AppContract
import io.bitpogo.pixalb.app.AppContract.INITIAL_QUERY
import io.bitpogo.pixalb.app.BuildConfig
import io.bitpogo.pixalb.app.db.DatabaseFactory
import io.bitpogo.pixalb.client.ClientContract
import io.bitpogo.pixalb.client.PixabayClient
import io.bitpogo.pixalb.lib.ConnectivityManager
import io.bitpogo.util.coroutine.wrapper.CoroutineWrapperContract.CoroutineScopeDispatcher
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@Qualifier
annotation class IODispatcher

@Qualifier
annotation class FlowDispatcher

@Module
@InstallIn(SingletonComponent::class)
object DependencyProvider {
    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): PixabayDataBase {
        return DatabaseFactory.create(
            PixabayDataBase.Schema,
            context,
        )
    }

    @Singleton
    @Provides
    fun provideConnectivityManager(
        @ApplicationContext context: Context,
    ): ClientContract.ConnectivityManager = ConnectivityManager(context)

    @Singleton
    @Provides
    fun provideLogger(): ClientContract.Logger {
        return object : ClientContract.Logger {
            override fun info(message: String) {
                Log.d(
                    AppContract.LogTag.CLIENT_INFO.value,
                    message,
                )
            }

            override fun warn(message: String) {
                Log.d(
                    AppContract.LogTag.CLIENT_WARN.value,
                    message,
                )
            }

            override fun error(exception: Throwable, message: String?) {
                Log.d(
                    AppContract.LogTag.CLIENT_ERROR.value,
                    message ?: exception.toString(),
                )
            }

            override fun log(message: String) {
                Log.d(
                    AppContract.LogTag.CLIENT_LOG.value,
                    message,
                )
            }
        }
    }

    @Singleton
    @Provides
    @IODispatcher
    fun provideIODispatcher(): CoroutineScopeDispatcher {
        return CoroutineScopeDispatcher { CoroutineScope(Dispatchers.IO) }
    }

    @Singleton
    @Provides
    @FlowDispatcher
    fun provideStoreDispatcher(): CoroutineScopeDispatcher {
        return CoroutineScopeDispatcher { CoroutineScope(Dispatchers.Default) }
    }

    @Singleton
    @Provides
    fun provideClient(
        logger: ClientContract.Logger,
        connectivityManager: ClientContract.ConnectivityManager,
    ): ClientContract.Client {
        return PixabayClient.getInstance(
            apiToken = BuildConfig.API_KEY,
            logger = logger,
            connection = connectivityManager,
        )
    }

    @Singleton
    @Provides
    fun provideStore(
        client: ClientContract.Client,
        dataBase: PixabayDataBase,
        @IODispatcher ioDispatcher: CoroutineScopeDispatcher,
        @FlowDispatcher flowDispatcher: CoroutineScopeDispatcher,
    ): AlbumContract.Store {
        return AlbumStore.getInstance(
            client = client,
            database = dataBase.imageQueries,
            producerScope = ioDispatcher,
            consumerScope = flowDispatcher,
        )
    }

    @Singleton
    @Provides
    fun provideInitialQuery(): String = INITIAL_QUERY
}
