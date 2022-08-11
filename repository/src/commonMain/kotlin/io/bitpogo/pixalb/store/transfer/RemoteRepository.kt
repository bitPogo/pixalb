/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.store.transfer

import io.bitpogo.pixalb.client.ClientContract
import io.bitpogo.pixalb.client.error.PixabayClientError
import io.bitpogo.pixalb.client.model.PixabayResponse
import io.bitpogo.pixalb.store.RepositoryContract
import io.bitpogo.pixalb.store.error.PixabayRepositoryError
import io.bitpogo.pixalb.store.model.DetailedViewItem
import io.bitpogo.pixalb.store.model.OverviewItem
import io.bitpogo.util.coroutine.result.Failure
import io.bitpogo.util.coroutine.result.ResultContract
import io.bitpogo.util.coroutine.result.Success
import kotlin.math.ceil

class RemoteRepository(
    private val client: ClientContract.Client
) : RepositoryContract.RemoteRepository {
    private fun mapItems(
        response: PixabayResponse
    ): Triple<Int, List<OverviewItem>, List<DetailedViewItem>> {
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
        }

        return Triple(response.total, overview, details)
    }

    private fun evaluateResponse(
        response: ResultContract<PixabayResponse, PixabayClientError>
    ): ResultContract<Triple<Int, List<OverviewItem>, List<DetailedViewItem>>, PixabayRepositoryError> {
        return when (response.error) {
            is PixabayClientError.NoConnection -> Failure(PixabayRepositoryError.NoConnection())
            is PixabayClientError -> Failure(PixabayRepositoryError.UnsuccessfulRequest(response.error!!))
            else -> Success(mapItems(response.unwrap()))
        }
    }

    override suspend fun fetch(
        query: String,
        pageId: UInt
    ): ResultContract<Triple<Int, List<OverviewItem>, List<DetailedViewItem>>, PixabayRepositoryError> {
        val response = client.fetchImages(
            query = query,
            page = ceil(pageId.toDouble() / 2).toUInt()
        )

        return evaluateResponse(response)
    }
}
