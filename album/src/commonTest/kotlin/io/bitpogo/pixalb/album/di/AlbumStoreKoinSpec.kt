/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.album.di

import io.bitpogo.pixalb.album.AlbumContract
import io.bitpogo.pixalb.album.database.PixabayDataBase
import io.bitpogo.pixalb.album.domain.RepositoryContract
import io.bitpogo.pixalb.album.testScope1
import io.bitpogo.pixalb.client.ClientContract
import io.bitpogo.pixalb.store.kmock
import io.bitpogo.util.coroutine.wrapper.CoroutineWrapperContract
import io.bitpogo.util.coroutine.wrapper.CoroutineWrapperContract.CoroutineScopeDispatcher
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.qualifier.named
import tech.antibytes.kmock.MockCommon
import tech.antibytes.util.test.isNot
import tech.antibytes.util.test.sameAs

@MockCommon(
    ClientContract.Client::class,
    CoroutineScopeDispatcher::class,
    PixabayDataBase::class
)
class AlbumStoreKoinSpec {
    @Test
    fun `Given initKoin is called with a Client, a Database, Consumer and Producer Scope Dispatcher it contains a RemoteRepository`() {
        // When
        val koin = initKoin(
            kmock(),
            kmock(),
            kmock(),
            kmock()
        )

        // Then
        koin.koin.get<RepositoryContract.RemoteRepository>() isNot null
    }

    @Test
    fun `Given initKoin is called with a Client, a Database, Consumer and Producer Scope Dispatcher it contains a LocalRepository`() {
        // When
        val koin = initKoin(
            kmock(),
            kmock(),
            kmock(),
            kmock()
        )

        // Then
        koin.koin.get<RepositoryContract.LocalRepository>() isNot null
    }

    @Test
    fun `Given initKoin is called with a Client, a Database, Consumer and Producer Scope Dispatcher it contains a MutableStateFlow for Overview`() {
        // When
        val koin = initKoin(
            kmock(),
            kmock(),
            kmock(),
            kmock()
        )

        // Then
        koin.koin.get<MutableStateFlow<AlbumContract.OverviewState>>(
            named(AlbumContract.KoinIds.OVERVIEW_STORE_IN)
        ) isNot null
    }

    @Test
    fun `Given initKoin is called with a Client, a Database, Consumer and Producer Scope Dispatcher it contains a SharedFlowWrapper for Overview`() {
        // When
        val koin = initKoin(
            kmock(),
            kmock(),
            kmock(),
            { testScope1 }
        )

        // Then
        val flow = koin.koin.get<MutableStateFlow<AlbumContract.OverviewState>>(
            named(AlbumContract.KoinIds.OVERVIEW_STORE_IN)
        )

        val wrapper = koin.koin.get<CoroutineWrapperContract.SharedFlowWrapper<AlbumContract.OverviewState>>(
            named(AlbumContract.KoinIds.OVERVIEW_STORE_OUT)
        )

        wrapper.wrappedFlow sameAs flow
    }

    @Test
    fun `Given initKoin is called with a Client, a Database, Consumer and Producer Scope Dispatcher it contains a MutableStateFlow for Detailview`() {
        // When
        val koin = initKoin(
            kmock(),
            kmock(),
            kmock(),
            kmock()
        )

        // Then
        koin.koin.get<MutableStateFlow<AlbumContract.DetailviewState>>(named(AlbumContract.KoinIds.DETAILVIEW_STORE_IN)) isNot null
    }

    @Test
    fun `Given initKoin is called with a Client, a Database, Consumer and Producer Scope Dispatcher it contains a SharedFlowWrapper for Detailview`() {
        // When
        val koin = initKoin(
            kmock(),
            kmock(),
            kmock(),
            { testScope1 }
        )

        // Then
        val flow = koin.koin.get<MutableStateFlow<AlbumContract.DetailviewState>>(
            named(AlbumContract.KoinIds.DETAILVIEW_STORE_IN)
        )

        val wrapper = koin.koin.get<CoroutineWrapperContract.SharedFlowWrapper<AlbumContract.DetailviewState>>(
            named(AlbumContract.KoinIds.DETAILVIEW_STORE_OUT)
        )

        wrapper.wrappedFlow sameAs flow
    }
}
