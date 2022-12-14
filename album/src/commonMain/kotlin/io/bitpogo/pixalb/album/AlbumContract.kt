/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.album

import io.bitpogo.pixalb.album.database.ImageQueries
import io.bitpogo.pixalb.album.domain.error.PixabayError
import io.bitpogo.pixalb.album.domain.model.DetailViewItem
import io.bitpogo.pixalb.album.domain.model.OverviewItem
import io.bitpogo.pixalb.client.ClientContract
import io.bitpogo.util.coroutine.result.State
import io.bitpogo.util.coroutine.wrapper.CoroutineWrapperContract
import io.bitpogo.util.coroutine.wrapper.CoroutineWrapperContract.SharedFlowWrapper

object AlbumContract {
    sealed class OverviewStoreState : State {
        object Initial : OverviewStoreState()
        object Pending : OverviewStoreState()
        class Accepted(val value: List<OverviewItem>) : OverviewStoreState()
        class Error(val value: PixabayError) : OverviewStoreState()
    }

    sealed class DetailviewStoreState : State {
        object Initial : DetailviewStoreState()
        object Pending : DetailviewStoreState()
        class Accepted(val value: DetailViewItem) : DetailviewStoreState()
        class Error(val value: PixabayError) : DetailviewStoreState()
    }

    interface Store {
        val overview: SharedFlowWrapper<OverviewStoreState>
        val detailview: SharedFlowWrapper<DetailviewStoreState>

        fun fetchOverview(
            query: String,
            pageId: UShort,
        )

        fun fetchDetailView(imageId: Long)
    }

    interface StoreFactory {
        fun getInstance(
            client: ClientContract.Client,
            database: ImageQueries,
            producerScope: CoroutineWrapperContract.CoroutineScopeDispatcher,
            consumerScope: CoroutineWrapperContract.CoroutineScopeDispatcher,
        ): Store
    }

    internal enum class KoinIds {
        PRODUCER_SCOPE,
        DETAILVIEW_STORE_IN,
        DETAILVIEW_STORE_OUT,
        OVERVIEW_STORE_IN,
        OVERVIEW_STORE_OUT,
    }
}
