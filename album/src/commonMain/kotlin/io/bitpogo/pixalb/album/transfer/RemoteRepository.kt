/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.album.transfer

import io.bitpogo.pixalb.client.ClientContract
import io.bitpogo.pixalb.client.error.PixabayClientError
import io.bitpogo.pixalb.client.model.PixabayResponse
import io.bitpogo.pixalb.album.domain.RepositoryContract
import io.bitpogo.pixalb.album.domain.error.PixabayError
import io.bitpogo.pixalb.album.domain.model.DetailedViewItem
import io.bitpogo.pixalb.album.domain.model.OverviewItem
import io.bitpogo.util.coroutine.result.Failure
import io.bitpogo.util.coroutine.result.ResultContract
import io.bitpogo.util.coroutine.result.Success
import io.bitpogo.pixalb.album.domain.RepositoryContract.RemoteRepositoryResponse
import kotlin.math.ceil

internal class RemoteRepository(
    private val client: ClientContract.Client
) : RepositoryContract.RemoteRepository {
    private fun mapItems(
        response: PixabayResponse
    ): RemoteRepositoryResponse {
        val imageIds: MutableList<Long> = mutableListOf()
        val overview: MutableList<OverviewItem> = mutableListOf()
        val details: MutableList<DetailedViewItem> = mutableListOf()

        response.items.forEach { item ->
            val tags = item.tags.split(", ")

            overview.add(
                OverviewItem(
                    userName = item.user,
                    tags = tags,
                    thumbnail = item.preview
                )
            )

            details.add(
                DetailedViewItem(
                    userName = item.user,
                    imageUrl = item.large,
                    tags = tags,
                    likes = item.likes,
                    downloads = item.downloads,
                    comments = item.comments
                )
            )

            imageIds.add(item.id)
        }

        return RemoteRepositoryResponse(
            totalAmountOfItems = response.total,
            overview = overview,
            detailedView = details,
            imageIds = imageIds
        )
    }

    private fun evaluateResponse(
        response: ResultContract<PixabayResponse, PixabayClientError>
    ): ResultContract<RemoteRepositoryResponse, PixabayError> {
        return when (response.error) {
            is PixabayClientError.NoConnection -> Failure(PixabayError.NoConnection())
            is PixabayClientError -> Failure(PixabayError.UnsuccessfulRequest(response.error!!))
            else -> Success(mapItems(response.unwrap()))
        }
    }

    private fun resolvePageId(
        pageId: UInt
    ): UInt = ceil(pageId.toDouble() / 2).toUInt()

    override suspend fun fetch(
        query: String,
        pageId: UInt
    ): ResultContract<RemoteRepositoryResponse, PixabayError> {
        val response = client.fetchImages(
            query = query,
            page = resolvePageId(pageId)
        )

        return evaluateResponse(response)
    }
}
