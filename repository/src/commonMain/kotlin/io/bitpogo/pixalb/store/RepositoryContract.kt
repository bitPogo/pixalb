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
    interface RemoteRepository {
        suspend fun fetch(
            query: String,
            pageId: UInt
        ): ResultContract<Triple<Int, List<OverviewItem>, List<DetailedViewItem>>, PixabayRepositoryError>
    }

    interface LocalRepository {
        suspend fun fetchOverview(
            query: String,
            pageId: Int
        ): ResultContract<List<OverviewItem>, PixabayRepositoryError>

        suspend fun storeOverview(
            query: String,
            pageId: Int,
            totalItems: Int,
            overview: List<OverviewItem>
        )

        suspend fun fetchDetailedView(itemId: String): ResultContract<OverviewItem, PixabayRepositoryError>

        suspend fun storeDetailedView(
            items: List<DetailedViewItem>
        )
    }
}
