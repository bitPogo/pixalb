/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.album.domain

import io.bitpogo.pixalb.album.AlbumContract
import io.bitpogo.pixalb.album.domain.error.PixabayError
import io.bitpogo.util.coroutine.wrapper.CoroutineWrapperContract.SharedFlowWrapper
import io.bitpogo.util.coroutine.wrapper.CoroutineWrapperContract.CoroutineScopeDispatcher
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

    override val overview: SharedFlowWrapper<AlbumContract.OverviewState> by koin.koin.inject(
        named(AlbumContract.KoinIds.OVERVIEW_STORE_OUT)
    )
    private val overviewPropagator: MutableStateFlow<AlbumContract.OverviewState> by koin.koin.inject(
        named(AlbumContract.KoinIds.OVERVIEW_STORE_IN)
    )

    override val detailview: SharedFlowWrapper<AlbumContract.DetailViewState> by koin.koin.inject(
        named(AlbumContract.KoinIds.DETAILVIEW_STORE_OUT)
    )
    private val detailviewPropagator: MutableStateFlow<AlbumContract.DetailViewState> by koin.koin.inject(
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
        overviewPropagator.update { AlbumContract.OverviewState.Pending }
    }

    private suspend fun loadAndStoreMissingEntries(
        query: String,
        pageId: UShort
    ): AlbumContract.OverviewState {
        val imageInfo = remoteRepository.fetch(query, pageId)

        if (imageInfo.isError()) {
            return AlbumContract.OverviewState.Error(imageInfo.error!!)
        }

        localRepository.storeImages(
            query = query,
            pageId = pageId,
            imageInfo = imageInfo.unwrap()
        )

        return AlbumContract.OverviewState.Accepted(imageInfo.unwrap().overview)
    }

    private suspend fun resolveOverview(
        query: String,
        pageId: UShort
    ): AlbumContract.OverviewState {
        val storedOverview = localRepository.fetchOverview(query, pageId)

        return when (storedOverview.error) {
            null -> AlbumContract.OverviewState.Accepted(storedOverview.unwrap())
            is PixabayError.MissingEntry -> loadAndStoreMissingEntries(query, pageId)
            else -> AlbumContract.OverviewState.Error(storedOverview.error!!)
        }
    }

    override fun fetchOverview(query: String, pageId: UShort) {
        goIntoPendingOverview()

        executeEvent(overviewPropagator) {
            resolveOverview(query, pageId)
        }
    }

    override fun fetchDetailedView(imageId: Long) {
        TODO("Not yet implemented")
    }
}
