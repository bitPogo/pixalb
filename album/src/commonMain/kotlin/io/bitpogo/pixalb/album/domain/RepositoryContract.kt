/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.album.domain

import io.bitpogo.pixalb.album.domain.error.PixabayError
import io.bitpogo.pixalb.album.domain.model.DetailedViewItem
import io.bitpogo.pixalb.album.domain.model.OverviewItem
import io.bitpogo.util.coroutine.result.ResultContract

internal object RepositoryContract {
    data class RemoteRepositoryResponse(
        val totalAmountOfItems: Int,
        val overview: List<OverviewItem>,
        val detailedView: List<DetailedViewItem>,
        val imageIds: List<Long>
    )

    interface RemoteRepository {
        suspend fun fetch(
            query: String,
            pageId: UShort
        ): ResultContract<RemoteRepositoryResponse, PixabayError>
    }

    interface LocalRepository {
        suspend fun fetchOverview(
            query: String,
            pageId: UShort
        ): ResultContract<List<OverviewItem>, PixabayError>

        suspend fun fetchDetailedView(imageId: Long): ResultContract<DetailedViewItem, PixabayError>

        fun storeImages(
            query: String,
            pageId: UShort,
            imageInfo: RemoteRepositoryResponse
        ): ResultContract<Unit, PixabayError>
    }

    const val REMOTE_ITEMS = 200
    const val LOCAL_ITEMS = 50
    const val ITEM_CAP = 500
}
