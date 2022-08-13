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
import io.bitpogo.pixalb.album.database.FetchQueryInfo
import io.bitpogo.pixalb.album.database.Image
import io.bitpogo.pixalb.album.database.ImageQueries
import io.bitpogo.pixalb.album.domain.RepositoryContract
import io.bitpogo.pixalb.album.domain.RepositoryContract.ITEM_CAP
import io.bitpogo.pixalb.album.domain.RepositoryContract.LOCAL_ITEMS
import io.bitpogo.pixalb.album.domain.RepositoryContract.REMOTE_ITEMS
import io.bitpogo.pixalb.album.domain.error.PixabayError
import io.bitpogo.pixalb.album.domain.model.DetailViewItem
import io.bitpogo.pixalb.album.domain.model.OverviewItem
import io.bitpogo.util.coroutine.result.Failure
import io.bitpogo.util.coroutine.result.ResultContract
import io.bitpogo.util.coroutine.result.Success
import kotlin.math.min
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus

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

    private fun UShort.resolveOffset(): Long = (this.toLong() - 1L) * LOCAL_ITEMS

    private fun toOverview(images: List<Image>): ResultContract<List<OverviewItem>, PixabayError> {
        val overview = images.map { image ->
            OverviewItem(
                id = image.imageId,
                thumbnail = image.previewUrl,
                userName = image.user,
                tags = image.tags
            )
        }

        return Success(overview)
    }

    private suspend fun resolveOverview(
        query: FetchQueryInfo,
        offset: Long
    ): ResultContract<List<OverviewItem>, PixabayError> {
        return sqlService.fetchImages(
            query = query.inquiry,
            offset = offset
        ).asFlow()
            .mapToList()
            .map(::toOverview)
            .first()
    }

    private suspend fun resolveOverview(
        pageId: UShort,
        query: FetchQueryInfo?
    ): ResultContract<List<OverviewItem>, PixabayError> {
        val offset = pageId.resolveOffset()

        return when {
            query == null -> Failure(PixabayError.MissingEntry)
            query.totalPages < offset -> Failure(PixabayError.EntryCap)
            query.storedPages < offset -> Failure(PixabayError.MissingPage)
            else -> resolveOverview(query, offset)
        }
    }

    override suspend fun fetchOverview(
        query: String,
        pageId: UShort
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

    private fun Image.toDetailViewItem(): DetailViewItem {
        return DetailViewItem(
            imageUrl = largeUrl,
            userName = user,
            tags = tags,
            likes = likes.toUInt(),
            comments = comments.toUInt(),
            downloads = downloads.toUInt()
        )
    }

    private fun toSuccessfulDetailViewItem(
        image: Image
    ): ResultContract<DetailViewItem, PixabayError> = Success(image.toDetailViewItem())

    override suspend fun fetchDetailedView(
        imageId: Long
    ): ResultContract<DetailViewItem, PixabayError> = guardSqlAccess {
        sqlService.fetchImage(imageId)
            .asFlow()
            .mapToOne()
            .map(::toSuccessfulDetailViewItem)
            .first()
    }

    private fun Instant.toDayAfter(): Instant {
        return this.plus(
            1,
            DateTimeUnit.DAY,
            TimeZone.currentSystemDefault()
        )
    }

    private fun <T> guardSqlTransaction(
        action: () -> ResultContract<T, PixabayError>
    ): ResultContract<T, PixabayError> {
        return try {
            sqlService.transactionWithResult {
                action()
            }
        } catch (e: Throwable) {
            Failure(PixabayError.UnsuccessfulDatabaseAccess(e))
        }
    }

    private fun storeImageEntries(
        query: String,
        imageInfo: RepositoryContract.RemoteRepositoryResponse
    ) {
        imageInfo.overview.forEachIndexed { idx, overview ->
            val image = imageInfo.detailedView[idx]

            sqlService.addImageQuery(query, overview.id)
            sqlService.addImage(
                imageId = overview.id,
                user = image.userName,
                tags = image.tags,
                largeUrl = image.imageUrl,
                previewUrl = overview.thumbnail,
                comments = image.comments.toInt(),
                likes = image.likes.toInt(),
                downloads = image.downloads.toInt()
            )
        }
    }

    private fun useUpdate(pageId: UShort) = pageId >= 5u

    private fun storeQuery(
        query: String,
        pageId: UShort,
        imageInfo: RepositoryContract.RemoteRepositoryResponse
    ) {
        if (useUpdate(pageId)) {
            sqlService.updatePageIndex(
                query = query,
                pageIndex = min(imageInfo.totalAmountOfItems, paging.getOrElse(pageId) { ITEM_CAP })
            )
        } else {
            sqlService.addQuery(
                inquiry = query,
                storedPages = REMOTE_ITEMS,
                totalPages = imageInfo.totalAmountOfItems,
                expiryDate = clock.now().toDayAfter()
            )
        }
    }

    override fun storeImages(
        query: String,
        pageId: UShort,
        imageInfo: RepositoryContract.RemoteRepositoryResponse
    ): ResultContract<Unit, PixabayError> = guardSqlTransaction {
        storeQuery(query, pageId, imageInfo)
        storeImageEntries(query, imageInfo)

        Success(Unit)
    }

    private companion object {
        private val paging = mapOf(
            5.toUShort() to REMOTE_ITEMS * 2,
            6.toUShort() to REMOTE_ITEMS * 2,
            7.toUShort() to REMOTE_ITEMS * 2,
            8.toUShort() to REMOTE_ITEMS * 2
        )
    }
}
