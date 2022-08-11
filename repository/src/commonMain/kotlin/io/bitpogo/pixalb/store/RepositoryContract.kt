/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.store

import io.bitpogo.pixalb.store.error.PixabayRepositoryError
import io.bitpogo.pixalb.store.model.DetailedViewItem
import io.bitpogo.pixalb.store.model.OverviewItem
import io.bitpogo.util.coroutine.result.ResultContract

object RepositoryContract {
    internal data class RemoteRepositoryResponse(
        val totalAmountOfItems: Int,
        val overview: List<OverviewItem>,
        val detailedView: List<DetailedViewItem>,
        val imageIds: List<Long>,
    )

    internal interface RemoteRepository {
        suspend fun fetch(
            query: String,
            pageId: UInt
        ): ResultContract<RemoteRepositoryResponse, PixabayRepositoryError>
    }

    internal interface LocalRepository {
        suspend fun fetchOverview(
            query: String,
            pageId: Int
        ): ResultContract<List<OverviewItem>, PixabayRepositoryError>

        suspend fun fetchDetailedView(itemId: Long): ResultContract<DetailedViewItem, PixabayRepositoryError>
    }
}
