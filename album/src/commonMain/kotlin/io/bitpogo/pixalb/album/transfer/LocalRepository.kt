/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.album.transfer

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOne
import com.squareup.sqldelight.runtime.coroutines.mapToOneOrNull
import io.bitpogo.pixalb.album.domain.RepositoryContract
import io.bitpogo.pixalb.album.database.FetchQueryInfo
import io.bitpogo.pixalb.album.database.ImageQueries
import io.bitpogo.pixalb.album.database.Image
import io.bitpogo.pixalb.album.domain.error.PixabayError
import io.bitpogo.pixalb.album.domain.model.DetailedViewItem
import io.bitpogo.pixalb.album.domain.model.OverviewItem
import io.bitpogo.util.coroutine.result.Failure
import io.bitpogo.util.coroutine.result.ResultContract
import io.bitpogo.util.coroutine.result.Success
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

internal class LocalRepository(
    private val sqlService: ImageQueries,
    private val clock: Clock = Clock.System
) : RepositoryContract.LocalRepository {
    private suspend fun <T> guardSqlAccess(
        action: suspend () -> ResultContract<T, PixabayError>
    ): ResultContract<T, PixabayError> {
        return try {
            action()
        } catch (e: Throwable) {
            Failure(PixabayError.UnsuccessfulDatabaseAccess(e))
        }
    }

    private fun Int.resolveOffset(): Long = (this.toLong() - 1L) * 50

    private fun toOverview(images: List<Image>): ResultContract<List<OverviewItem>, PixabayError> {
        val overview = images.map { image ->
            OverviewItem(
                thumbnail = image.previewUrl,
                userName = image.user,
                tags = image.tags,
            )
        }

        return Success(overview)
    }

    private suspend fun resolveOverview(
        query: FetchQueryInfo,
        offset: Long,
    ): ResultContract<List<OverviewItem>, PixabayError> {
        return sqlService.fetchImages(
            query = query.inquiry,
            offset = offset,
        ).asFlow()
            .mapToList()
            .map(::toOverview)
            .first()
    }

    private suspend fun resolveOverview(
        pageId: Int,
        query: FetchQueryInfo?,
    ): ResultContract<List<OverviewItem>, PixabayError> {
        val offset = pageId.resolveOffset()
        return when {
            query == null -> Failure(PixabayError.MissingEntry())
            query.totalPages < offset -> Failure(PixabayError.EntryCap())
            query.storedPages < offset -> Failure(PixabayError.MissingPage())
            else -> resolveOverview(query, offset)
        }
    }

    override suspend fun fetchOverview(
        query: String,
        pageId: Int
    ): ResultContract<List<OverviewItem>, PixabayError> = guardSqlAccess {
        val queryInfo = sqlService.fetchQueryInfo(query, clock.now())
            .asFlow()
            .mapToOneOrNull()
            .firstOrNull()

        resolveOverview(
            query = queryInfo,
            pageId = pageId
        )
    }

    private fun Image.toDetailViewItem(): DetailedViewItem {
        return DetailedViewItem(
            imageUrl = largeUrl,
            userName = user,
            tags = tags,
            likes = likes.toUInt(),
            comments = comments.toUInt(),
            downloads = downloads.toUInt(),
        )
    }

    private fun toSuccessfulDetailViewItem(image: Image): ResultContract<DetailedViewItem, PixabayError> {
        return Success(image.toDetailViewItem())
    }

    override suspend fun fetchDetailedView(
        imageId: Long
    ): ResultContract<DetailedViewItem, PixabayError> = guardSqlAccess {
        sqlService.fetchImage(imageId)
            .asFlow()
            .mapToOne()
            .map(::toSuccessfulDetailViewItem)
            .first()
    }
}
