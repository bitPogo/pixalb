/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.album

import io.bitpogo.pixalb.album.domain.error.PixabayError
import io.bitpogo.pixalb.album.domain.model.DetailViewItem
import io.bitpogo.pixalb.album.domain.model.OverviewItem
import io.bitpogo.util.coroutine.result.State
import io.bitpogo.util.coroutine.wrapper.CoroutineWrapperContract.SharedFlowWrapper

object AlbumContract {
    sealed class OverviewState : State {
        object Initial : OverviewState()
        object Pending : OverviewState()
        class Accepted(val value: List<OverviewItem>) : OverviewState()
        class Error(val value: PixabayError) : OverviewState()
    }

    sealed class DetailViewState : State {
        object Initial : DetailViewState()
        object Pending : DetailViewState()
        class Accepted(val value: DetailViewItem) : DetailViewState()
        class Error(val value: PixabayError) : DetailViewState()
    }

    interface Store {
        val overview: SharedFlowWrapper<OverviewState>
        val detailview: SharedFlowWrapper<DetailViewState>

        fun fetchOverview(
            query: String,
            pageId: UShort
        )

        fun fetchDetailedView(imageId: Long)
    }

    internal enum class KoinIds {
        PRODUCER_SCOPE,
        DETAILVIEW_STORE_IN,
        DETAILVIEW_STORE_OUT,
        OVERVIEW_STORE_IN,
        OVERVIEW_STORE_OUT,
    }
}
