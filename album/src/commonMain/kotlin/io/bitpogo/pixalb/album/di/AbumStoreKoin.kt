/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.album.di

import io.bitpogo.pixalb.album.AlbumContract
import io.bitpogo.pixalb.album.database.ImageQueries
import io.bitpogo.pixalb.album.domain.RepositoryContract
import io.bitpogo.pixalb.album.transfer.LocalRepository
import io.bitpogo.pixalb.album.transfer.RemoteRepository
import io.bitpogo.pixalb.client.ClientContract
import io.bitpogo.util.coroutine.wrapper.CoroutineWrapperContract.CoroutineScopeDispatcher
import io.bitpogo.util.coroutine.wrapper.SharedFlowWrapper as SharedFlowWrapperFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.Clock
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.koinApplication
import org.koin.dsl.module

private fun resolveAlbumStoreParameterModule(
    producerScope: CoroutineScopeDispatcher,
): Module {
    return module {
        factory(named(AlbumContract.KoinIds.PRODUCER_SCOPE)) { producerScope }
        single<Clock> { Clock.System }
    }
}

private fun resolveRepositories(
    client: ClientContract.Client,
    database: ImageQueries,
): Module {
    return module {
        single<RepositoryContract.RemoteRepository> { RemoteRepository(client) }
        single<RepositoryContract.LocalRepository> { LocalRepository(database, get()) }
    }
}

internal fun resolveAlbumStoreModule(
    consumerScope: CoroutineScopeDispatcher
): Module {
    return module {
        single<MutableStateFlow<AlbumContract.OverviewState>>(named(AlbumContract.KoinIds.OVERVIEW_STORE_IN)) {
            MutableStateFlow(AlbumContract.OverviewState.Initial)
        }

        single<MutableStateFlow<AlbumContract.DetailViewState>>(named(AlbumContract.KoinIds.DETAILVIEW_STORE_IN)) {
            MutableStateFlow(
                AlbumContract.DetailViewState.Initial
            )
        }

        single(named(AlbumContract.KoinIds.OVERVIEW_STORE_OUT)) {
            SharedFlowWrapperFactory.getInstance(
                get<MutableStateFlow<AlbumContract.OverviewState>>(named(AlbumContract.KoinIds.OVERVIEW_STORE_IN)),
                consumerScope
            )
        }

        single(named(AlbumContract.KoinIds.DETAILVIEW_STORE_OUT)) {
            SharedFlowWrapperFactory.getInstance(
                get<MutableStateFlow<AlbumContract.DetailViewState>>(named(AlbumContract.KoinIds.DETAILVIEW_STORE_IN)),
                consumerScope
            )
        }
    }
}

internal fun initKoin(
    client: ClientContract.Client,
    database: ImageQueries,
    producerScope: CoroutineScopeDispatcher,
    consumerScope: CoroutineScopeDispatcher
): KoinApplication {
    return koinApplication {
        modules(
            resolveRepositories(client, database),
            resolveAlbumStoreModule(consumerScope),
            resolveAlbumStoreParameterModule(producerScope)
        )
    }
}
