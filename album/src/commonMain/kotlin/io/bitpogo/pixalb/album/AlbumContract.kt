/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.album

import io.bitpogo.pixalb.album.domain.error.PixabayError
import io.bitpogo.pixalb.album.domain.model.DetailedViewItem
import io.bitpogo.pixalb.album.domain.model.OverviewItem
import io.bitpogo.util.coroutine.wrapper.CoroutineWrapperContract.SharedFlowWrapper

object AlbumContract {
    interface Store {
        val overview: SharedFlowWrapper<OverviewItem?, PixabayError>
        val detailedView: SharedFlowWrapper<DetailedViewItem?, PixabayError>

        fun fetchOverview(
            query: String,
            pageId: UInt
        )

        fun fetchDetailedView(imageId: Long)
    }
}
