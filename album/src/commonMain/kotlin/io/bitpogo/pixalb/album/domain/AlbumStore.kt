/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.album.domain

import io.bitpogo.pixalb.album.AlbumContract
import io.bitpogo.pixalb.album.database.ImageQueries
import io.bitpogo.pixalb.album.di.initKoin
import io.bitpogo.pixalb.album.domain.error.PixabayError
import io.bitpogo.pixalb.client.ClientContract
import io.bitpogo.util.coroutine.wrapper.CoroutineWrapperContract.CoroutineScopeDispatcher
import io.bitpogo.util.coroutine.wrapper.CoroutineWrapperContract.SharedFlowWrapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.KoinApplication
import org.koin.core.qualifier.named

class AlbumStore internal constructor(
    koin: KoinApplication
) : AlbumContract.Store {
    private val dispatcher: CoroutineScopeDispatcher by koin.koin.inject(
        named(AlbumContract.KoinIds.PRODUCER_SCOPE)
    )

    private val localRepository: RepositoryContract.LocalRepository by koin.koin.inject()
    private val remoteRepository: RepositoryContract.RemoteRepository by koin.koin.inject()

    override val overview: SharedFlowWrapper<AlbumContract.OverviewStoreState> by koin.koin.inject(
        named(AlbumContract.KoinIds.OVERVIEW_STORE_OUT)
    )
    private val overviewPropagator: MutableStateFlow<AlbumContract.OverviewStoreState> by koin.koin.inject(
        named(AlbumContract.KoinIds.OVERVIEW_STORE_IN)
    )

    override val detailview: SharedFlowWrapper<AlbumContract.DetailviewStoreState> by koin.koin.inject(
        named(AlbumContract.KoinIds.DETAILVIEW_STORE_OUT)
    )
    private val detailviewPropagator: MutableStateFlow<AlbumContract.DetailviewStoreState> by koin.koin.inject(
        named(AlbumContract.KoinIds.DETAILVIEW_STORE_IN)
    )

    private fun <T> executeEvent(
        propagator: MutableStateFlow<T>,
        event: suspend () -> T
    ) {
        dispatcher.dispatch().launch {
            propagator.update { event() }
        }
    }

    private fun goIntoPendingOverview() {
        overviewPropagator.update { AlbumContract.OverviewStoreState.Pending }
    }

    private suspend fun loadAndStoreMissingEntries(
        query: String,
        pageId: UShort
    ): AlbumContract.OverviewStoreState {
        val imageInfo = remoteRepository.fetch(query, pageId)

        return if (imageInfo.isError()) {
            AlbumContract.OverviewStoreState.Error(imageInfo.error!!)
        } else {
            localRepository.storeImages(
                query = query,
                pageId = pageId,
                imageInfo = imageInfo.unwrap()
            )

            AlbumContract.OverviewStoreState.Accepted(imageInfo.unwrap().overview)
        }
    }

    private suspend fun resolveOverview(
        query: String,
        pageId: UShort
    ): AlbumContract.OverviewStoreState {
        val storedOverview = localRepository.fetchOverview(query, pageId)

        return when (storedOverview.error) {
            null -> {
                AlbumContract.OverviewStoreState.Accepted(storedOverview.unwrap())
            }
            is PixabayError.MissingEntry, is PixabayError.MissingPage -> {
                loadAndStoreMissingEntries(query, pageId)
            }
            else -> {
                AlbumContract.OverviewStoreState.Error(storedOverview.error!!)
            }
        }
    }

    override fun fetchOverview(query: String, pageId: UShort) {
        goIntoPendingOverview()

        executeEvent(overviewPropagator) {
            resolveOverview(query, pageId)
        }
    }

    private fun goIntoPendingDetailview() {
        detailviewPropagator.update { AlbumContract.DetailviewStoreState.Pending }
    }

    private suspend fun wrapDetailedView(imageId: Long): AlbumContract.DetailviewStoreState {
        val result = localRepository.fetchDetailedView(imageId)

        return if (result.isError()) {
            AlbumContract.DetailviewStoreState.Error(result.error!!)
        } else {
            AlbumContract.DetailviewStoreState.Accepted(result.unwrap())
        }
    }

    override fun fetchDetailView(imageId: Long) {
        goIntoPendingDetailview()

        executeEvent(detailviewPropagator) {
            wrapDetailedView(imageId)
        }
    }

    companion object : AlbumContract.StoreFactory {
        override fun getInstance(
            client: ClientContract.Client,
            database: ImageQueries,
            producerScope: CoroutineScopeDispatcher,
            consumerScope: CoroutineScopeDispatcher
        ): AlbumContract.Store {
            return AlbumStore(
                initKoin(
                    client = client,
                    database = database,
                    consumerScope = consumerScope,
                    producerScope = producerScope
                )
            )
        }
    }
}
