/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.album.transfer

import io.bitpogo.pixalb.album.domain.RepositoryContract
import io.bitpogo.pixalb.album.domain.RepositoryContract.RemoteRepositoryResponse
import io.bitpogo.pixalb.album.domain.error.PixabayError
import io.bitpogo.pixalb.album.domain.model.DetailViewItem
import io.bitpogo.pixalb.album.domain.model.OverviewItem
import io.bitpogo.pixalb.client.ClientContract
import io.bitpogo.pixalb.client.error.PixabayClientError
import io.bitpogo.pixalb.client.model.PixabayResponse
import io.bitpogo.util.coroutine.result.Failure
import io.bitpogo.util.coroutine.result.ResultContract
import io.bitpogo.util.coroutine.result.Success

internal class RemoteRepository(
    private val client: ClientContract.Client
) : RepositoryContract.RemoteRepository {
    private fun mapItems(
        response: PixabayResponse
    ): RemoteRepositoryResponse {
        val overview: MutableList<OverviewItem> = mutableListOf()
        val details: MutableList<DetailViewItem> = mutableListOf()

        response.items.forEach { item ->
            val tags = item.tags.split(", ")

            overview.add(
                OverviewItem(
                    id = item.id,
                    userName = item.user,
                    tags = tags,
                    thumbnail = item.preview
                )
            )

            details.add(
                DetailViewItem(
                    userName = item.user,
                    imageUrl = item.large,
                    tags = tags,
                    likes = item.likes,
                    downloads = item.downloads,
                    comments = item.comments
                )
            )
        }

        return RemoteRepositoryResponse(
            totalAmountOfItems = response.total,
            overview = overview,
            detailedView = details
        )
    }

    private fun evaluateResponse(
        response: ResultContract<PixabayResponse, PixabayClientError>
    ): ResultContract<RemoteRepositoryResponse, PixabayError> {
        return when (response.error) {
            is PixabayClientError.NoConnection -> Failure(PixabayError.NoConnection)
            is PixabayClientError -> Failure(PixabayError.UnsuccessfulRequest(response.error!!))
            else -> Success(mapItems(response.unwrap()))
        }
    }

    private fun resolvePageId(
        pageId: UShort
    ): UShort = when {
        pageId <= 4.toUShort() -> 1u
        pageId <= 8.toUShort() -> 2u
        else -> 3u
    }

    override suspend fun fetch(
        query: String,
        pageId: UShort
    ): ResultContract<RemoteRepositoryResponse, PixabayError> {
        val response = client.fetchImages(
            query = query,
            page = resolvePageId(pageId)
        )

        return evaluateResponse(response)
    }
}
