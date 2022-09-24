/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.app.di

import io.bitpogo.pixalb.album.AlbumContract
import io.bitpogo.pixalb.album.database.ImageQueries
import io.bitpogo.pixalb.album.database.PixabayDataBase
import io.bitpogo.pixalb.album.database.PixabayDataBaseMock
import io.bitpogo.pixalb.app.kmock
import io.bitpogo.pixalb.client.ClientContract
import io.bitpogo.pixalb.fixture.MockContext
import io.bitpogo.util.coroutine.wrapper.CoroutineWrapperContract.CoroutineScopeDispatcher
import org.junit.Test
import tech.antibytes.kmock.Mock
import tech.antibytes.util.test.fulfils
import tech.antibytes.util.test.mustBe

@Mock(
    ClientContract.Logger::class,
    ClientContract.ConnectivityManager::class,
    ClientContract.Client::class,
    PixabayDataBase::class,
    CoroutineScopeDispatcher::class,
    ImageQueries::class,
)
class DependencyProviderSpec {

    @Test
    fun `Given provideDatabase is called with a context it contains a PixabayDatabase`() {
        // Given
        val context = MockContext

        // When
        val actual = DependencyProvider.provideDatabase(context)

        // Then
        actual fulfils PixabayDataBase::class
    }

    @Test
    fun `Given provideConnectivityManager is called with a context is contains an ConnectivityManager`() {
        // Given
        val context = MockContext

        // When
        val actual = DependencyProvider.provideConnectivityManager(context)

        // Then
        actual fulfils ClientContract.ConnectivityManager::class
    }

    @Test
    fun `Given provideLogger is called it creates a Logger`() {
        // When
        val actual = DependencyProvider.provideLogger()

        // Then
        actual fulfils ClientContract.Logger::class
    }

    @Test
    fun `Given provideClient is called with a Logger and ConnectivityManager it contains a Client`() {
        // When
        val actual = DependencyProvider.provideClient(kmock(), kmock())

        // Then
        actual fulfils ClientContract.Client::class
    }

    @Test
    fun `Given provideIODispatcher is called it contains a CoroutineScopeDispatcher`() {
        // When
        val dispatcher = DependencyProvider.provideIODispatcher()

        // Then
        dispatcher.dispatch()
            .toString()
            .contains("Dispatchers.IO") mustBe true
    }

    @Test
    fun `Given provideStoreDispatcher is called it contains a CoroutineScopeDispatcher`() {
        // When
        val dispatcher = DependencyProvider.provideStoreDispatcher()

        // Then
        dispatcher.dispatch()
            .toString()
            .contains("Dispatchers.Default") mustBe true
    }

    @Test
    fun `Given provideInitialQuery is called it contains a String`() {
        // When
        val query = DependencyProvider.provideInitialQuery()

        // Then
        query mustBe "fruits"
    }

    @Test
    fun `Given provideStore is called with a Client and DataBase it contains a Store`() {
        val db: PixabayDataBaseMock = kmock()

        db._imageQueries returns kmock()

        // When
        val actual = DependencyProvider.provideStore(kmock(), db, kmock(), kmock())

        // Then
        actual fulfils AlbumContract.Store::class
    }
}
