/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.album.transfer

import com.squareup.sqldelight.Query
import com.squareup.sqldelight.TransactionWithReturn
import com.squareup.sqldelight.TransactionWithReturnMock
import io.bitpogo.pixalb.album.database.FetchQueryInfo
import io.bitpogo.pixalb.album.database.Image
import io.bitpogo.pixalb.album.database.ImageQueries
import io.bitpogo.pixalb.album.database.ImageQueriesMock
import io.bitpogo.pixalb.album.domain.RepositoryContract
import io.bitpogo.pixalb.album.domain.error.PixabayError
import io.bitpogo.pixalb.album.domain.model.DetailViewItem
import io.bitpogo.pixalb.album.domain.model.OverviewItem
import io.bitpogo.pixalb.album.kmock
import io.bitpogo.pixalb.album.mock.QueryStub
import io.bitpogo.pixalb.album.mock.SqlCursorStub
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.datetime.Clock
import kotlinx.datetime.ClockMock
import kotlinx.datetime.Instant
import tech.antibytes.kfixture.PublicApi
import tech.antibytes.kfixture.fixture
import tech.antibytes.kfixture.kotlinFixture
import tech.antibytes.kfixture.listFixture
import tech.antibytes.kmock.MockCommon
import tech.antibytes.kmock.verification.Asserter
import tech.antibytes.kmock.verification.assertOrder
import tech.antibytes.kmock.verification.assertProxy
import tech.antibytes.kmock.verification.verify
import tech.antibytes.util.test.coroutine.runBlockingTestWithTimeout
import tech.antibytes.util.test.fulfils
import tech.antibytes.util.test.mustBe
import tech.antibytes.util.test.sameAs

@MockCommon(
    ImageQueries::class,
    Clock::class,
    TransactionWithReturn::class
)
class LocalRepositorySpec {
    private val fixture = kotlinFixture()
    private val asserter = Asserter()
    private val queries: ImageQueriesMock = kmock(relaxUnitFun = true, collector = asserter)

    @BeforeTest
    fun setUp() {
        queries._clearMock()
        asserter.clear()
    }

    private fun PublicApi.Fixture.imageFixture(): Image {
        return Image(
            id = fixture(),
            imageId = fixture(),
            user = fixture(),
            tags = listFixture(),
            downloads = fixture(),
            likes = fixture(),
            comments = fixture(),
            previewUrl = fixture(),
            largeUrl = fixture()
        )
    }

    @Test
    fun `It fulfils LocalRepository`() {
        // Given
        LocalRepository(queries) fulfils RepositoryContract.LocalRepository::class
    }

    @Test
    fun `Given fetchOverview is called it maps any occurring Info error`() = runBlockingTestWithTimeout {
        // Given
        val error = RuntimeException()

        queries._fetchQueryInfoWithStringInstant throws error

        // When
        val result = LocalRepository(queries).fetchOverview(
            fixture.fixture(),
            fixture.fixture()
        )

        // Then
        result.error!! fulfils PixabayError.UnsuccessfulDatabaseAccess::class
        result.error!!.cause sameAs error
    }

    @Test
    fun `Given fetchOverview is called it maps any occurring fetch error`() = runBlockingTestWithTimeout {
        // Given
        val next = mutableListOf(true, false)
        val infoQuery: Query<FetchQueryInfo> = QueryStub(
            mapper = {
                FetchQueryInfo(
                    fixture.fixture(),
                    fixture.fixture(1, 100),
                    fixture.fixture(1, 100)
                )
            },
            execute = { SqlCursorStub { next.removeFirst() } }
        )
        val error = RuntimeException()

        queries._fetchQueryInfoWithStringInstant returns infoQuery
        queries._fetchImagesWithStringLong throws error

        // When
        val result = LocalRepository(queries).fetchOverview(
            fixture.fixture(),
            1.toUShort()
        )

        // Then
        result.error!! fulfils PixabayError.UnsuccessfulDatabaseAccess::class
        result.error!!.cause sameAs error
    }

    @Test
    fun `Given fetchOverview is called returns a missing entry error`() = runBlockingTestWithTimeout {
        // Given
        val infoQuery: Query<FetchQueryInfo> = QueryStub(
            mapper = {
                FetchQueryInfo(
                    fixture.fixture(),
                    fixture.fixture(1, 100),
                    fixture.fixture(1, 100)
                )
            },
            execute = { SqlCursorStub { false } }
        )

        queries._fetchQueryInfoWithStringInstant returns infoQuery

        // When
        val result = LocalRepository(queries).fetchOverview(
            fixture.fixture(),
            1.toUShort()
        )

        // Then
        result.error!! fulfils PixabayError.MissingEntry::class
    }

    @Test
    fun `Given fetchOverview is called returns a missing page error`() = runBlockingTestWithTimeout {
        // Given
        val next = mutableListOf(true, false)
        val infoQuery: Query<FetchQueryInfo> = QueryStub(
            mapper = {
                FetchQueryInfo(
                    fixture.fixture(),
                    fixture.fixture(1, 50),
                    fixture.fixture(100, 200)
                )
            },
            execute = { SqlCursorStub { next.removeFirst() } }
        )

        queries._fetchQueryInfoWithStringInstant returns infoQuery

        // When
        val result = LocalRepository(queries).fetchOverview(
            fixture.fixture(),
            2.toUShort()
        )

        // Then
        result.error!! fulfils PixabayError.MissingPage::class
    }

    @Test
    fun `Given fetchOverview is called returns a entry cap error`() = runBlockingTestWithTimeout {
        // Given
        val next = mutableListOf(true, false)
        val total: Int = fixture.fixture(50, 200)
        val infoQuery: Query<FetchQueryInfo> = QueryStub(
            mapper = {
                FetchQueryInfo(
                    fixture.fixture(),
                    fixture.fixture(1, 50),
                    total
                )
            },
            execute = { SqlCursorStub { next.removeFirst() } }
        )

        queries._fetchQueryInfoWithStringInstant returns infoQuery

        // When
        val result = LocalRepository(queries).fetchOverview(
            fixture.fixture(),
            3.toUShort()
        )

        // Then
        result.error!! fulfils PixabayError.EntryCap::class
    }

    @Test
    fun `Given fetchOverview is called returns an Overview`() = runBlockingTestWithTimeout {
        // Given
        val clock: ClockMock = kmock()
        val query: String = fixture.fixture()
        val pageId: UShort = fixture.fixture(1.toUShort(), 6.toUShort())
        val inquiry: String = fixture.fixture()

        val next1 = mutableListOf(true, false)
        val next2 = mutableListOf(true, false)
        val overviewItem = fixture.imageFixture()
        val infoQuery: Query<FetchQueryInfo> = QueryStub(
            mapper = {
                FetchQueryInfo(
                    inquiry,
                    500,
                    500
                )
            },
            execute = { SqlCursorStub { next1.removeFirst() } }
        )
        val overview: QueryStub<Image> = QueryStub(
            mapper = { overviewItem },
            execute = { SqlCursorStub { next2.removeFirst() } }
        )

        queries._fetchQueryInfoWithStringInstant returns infoQuery
        queries._fetchImagesWithStringLong returns overview
        clock._now returns Instant.DISTANT_FUTURE

        // When
        val result = LocalRepository(queries, clock).fetchOverview(query, pageId)

        // Then
        result.unwrap() mustBe listOf(
            OverviewItem(
                id = overviewItem.imageId,
                thumbnail = overviewItem.previewUrl,
                userName = overviewItem.user,
                tags = overviewItem.tags
            )
        )

        assertProxy {
            queries._fetchQueryInfoWithStringInstant.hasBeenStrictlyCalledWith(
                query,
                Instant.DISTANT_FUTURE
            )
            queries._fetchImagesWithStringLong.hasBeenStrictlyCalledWith(
                inquiry,
                (pageId.toLong() - 1) * 50
            )
        }
    }

    @Test
    fun `Given fetchDetailedView is called it maps any occurring error`() = runBlockingTestWithTimeout {
        // Given
        val error = RuntimeException()

        queries._fetchImageWithLong throws error

        // When
        val result = LocalRepository(queries).fetchDetailedView(
            fixture.fixture()
        )

        // Then
        result.error!! fulfils PixabayError.UnsuccessfulDatabaseAccess::class
        result.error!!.cause sameAs error
    }

    @Test
    fun `Given fetchDetailedView is called it returns a single Image`() = runBlockingTestWithTimeout {
        // Given
        val image = fixture.imageFixture()

        val next = mutableListOf(true, false)
        val query: Query<Image> = QueryStub(
            mapper = { image },
            execute = { SqlCursorStub { next.removeFirst() } }
        )

        queries._fetchImageWithLong returns query

        // When
        val result = LocalRepository(queries).fetchDetailedView(
            fixture.fixture()
        )

        // Then
        result.value mustBe DetailViewItem(
            imageUrl = image.largeUrl,
            userName = image.user,
            tags = image.tags,
            likes = image.likes.toUInt(),
            comments = image.comments.toUInt(),
            downloads = image.downloads.toUInt()
        )
    }

    @Test
    fun `Given storeImage is called it maps any occuring errors from addQuery`() {
        // Given
        val transaction: TransactionWithReturnMock<Any?> = kmock(
            templateType = TransactionWithReturn::class
        )
        val error = RuntimeException()
        val image = fixture.imageFixture()
        val detailViewItem = DetailViewItem(
            imageUrl = image.largeUrl,
            userName = image.user,
            tags = image.tags,
            likes = image.likes.toUInt(),
            comments = image.comments.toUInt(),
            downloads = image.downloads.toUInt()
        )
        val overviewItem = OverviewItem(
            id = image.imageId,
            thumbnail = image.previewUrl,
            userName = image.user,
            tags = image.tags
        )

        queries._addQuery throws error
        queries._transactionWithResult run { _, action ->
            action(transaction)
        }

        // When
        val result = LocalRepository(queries).storeImages(
            query = fixture.fixture(),
            pageId = fixture.fixture(1.toUShort(), 4.toUShort()),
            imageInfo = RepositoryContract.RemoteRepositoryResponse(
                overview = listOf(overviewItem),
                detailedView = listOf(detailViewItem),
                totalAmountOfItems = fixture.fixture()
            )
        )

        // Then
        result.error!! fulfils PixabayError.UnsuccessfulDatabaseAccess::class
        result.error!!.cause sameAs error
    }

    @Test
    fun `Given storeImage is called it maps any occuring errors from updatePageIndex`() {
        // Given
        val transaction: TransactionWithReturnMock<Any?> = kmock(
            templateType = TransactionWithReturn::class
        )
        val error = RuntimeException()
        val image = fixture.imageFixture()
        val detailViewItem = DetailViewItem(
            imageUrl = image.largeUrl,
            userName = image.user,
            tags = image.tags,
            likes = image.likes.toUInt(),
            comments = image.comments.toUInt(),
            downloads = image.downloads.toUInt()
        )
        val overviewItem = OverviewItem(
            id = image.imageId,
            thumbnail = image.previewUrl,
            userName = image.user,
            tags = image.tags
        )

        queries._updatePageIndex throws error
        queries._transactionWithResult run { _, action ->
            action(transaction)
        }

        // When
        val result = LocalRepository(queries).storeImages(
            query = fixture.fixture(),
            pageId = fixture.fixture(5.toUShort(), 10.toUShort()),
            imageInfo = RepositoryContract.RemoteRepositoryResponse(
                overview = listOf(overviewItem),
                detailedView = listOf(detailViewItem),
                totalAmountOfItems = fixture.fixture()
            )
        )

        // Then
        result.error!! fulfils PixabayError.UnsuccessfulDatabaseAccess::class
        result.error!!.cause sameAs error
    }

    @Test
    fun `Given storeImage is called it maps any occuring errors from addImageQuery`() {
        // Given
        val transaction: TransactionWithReturnMock<Any?> = kmock(
            templateType = TransactionWithReturn::class
        )
        val error = RuntimeException()
        val image = fixture.imageFixture()
        val detailViewItem = DetailViewItem(
            imageUrl = image.largeUrl,
            userName = image.user,
            tags = image.tags,
            likes = image.likes.toUInt(),
            comments = image.comments.toUInt(),
            downloads = image.downloads.toUInt()
        )
        val overviewItem = OverviewItem(
            id = image.imageId,
            thumbnail = image.previewUrl,
            userName = image.user,
            tags = image.tags
        )

        queries._addImageQuery throws error
        queries._transactionWithResult run { _, action ->
            action(transaction)
        }

        // When
        val result = LocalRepository(queries).storeImages(
            query = fixture.fixture(),
            pageId = fixture.fixture(1.toUShort(), 4.toUShort()),
            imageInfo = RepositoryContract.RemoteRepositoryResponse(
                overview = listOf(overviewItem),
                detailedView = listOf(detailViewItem),
                totalAmountOfItems = fixture.fixture()
            )
        )

        // Then
        result.error!! fulfils PixabayError.UnsuccessfulDatabaseAccess::class
        result.error!!.cause sameAs error
    }

    @Test
    fun `Given storeImage is called it maps any occuring errors from addImage`() {
        // Given
        val transaction: TransactionWithReturnMock<Any?> = kmock(
            templateType = TransactionWithReturn::class
        )
        val error = RuntimeException()
        val image = fixture.imageFixture()
        val detailViewItem = DetailViewItem(
            imageUrl = image.largeUrl,
            userName = image.user,
            tags = image.tags,
            likes = image.likes.toUInt(),
            comments = image.comments.toUInt(),
            downloads = image.downloads.toUInt()
        )
        val overviewItem = OverviewItem(
            id = image.imageId,
            thumbnail = image.previewUrl,
            userName = image.user,
            tags = image.tags
        )

        queries._addImage throws error
        queries._transactionWithResult run { _, action ->
            action(transaction)
        }

        // When
        val result = LocalRepository(queries).storeImages(
            query = fixture.fixture(),
            pageId = fixture.fixture(1.toUShort(), 4.toUShort()),
            imageInfo = RepositoryContract.RemoteRepositoryResponse(
                overview = listOf(overviewItem),
                detailedView = listOf(detailViewItem),
                totalAmountOfItems = fixture.fixture()
            )
        )

        // Then
        result.error!! fulfils PixabayError.UnsuccessfulDatabaseAccess::class
        result.error!!.cause sameAs error
    }

    @Test
    fun `Given storeImage is called it just runs`() {
        // Given
        val transaction: TransactionWithReturnMock<Any?> = kmock(
            templateType = TransactionWithReturn::class
        )

        val query: String = fixture.fixture()
        val pageId: UShort = fixture.fixture(1.toUShort(), 4.toUShort())
        val total: Int = fixture.fixture()
        val now: Long = fixture.fixture(PublicApi.Sign.POSITIVE)
        val tomorrow = now + 86400000

        val image1 = fixture.imageFixture()
        val image2 = fixture.imageFixture()
        val detailViewItem1 = DetailViewItem(
            imageUrl = image1.largeUrl,
            userName = image1.user,
            tags = image1.tags,
            likes = image1.likes.toUInt(),
            comments = image1.comments.toUInt(),
            downloads = image1.downloads.toUInt()
        )
        val detailViewItem2 = DetailViewItem(
            imageUrl = image2.largeUrl,
            userName = image2.user,
            tags = image2.tags,
            likes = image2.likes.toUInt(),
            comments = image2.comments.toUInt(),
            downloads = image2.downloads.toUInt()
        )
        val overviewItem1 = OverviewItem(
            id = image1.imageId,
            thumbnail = image1.previewUrl,
            userName = image1.user,
            tags = image1.tags
        )
        val overviewItem2 = OverviewItem(
            id = image2.imageId,
            thumbnail = image2.previewUrl,
            userName = image2.user,
            tags = image2.tags
        )

        queries._transactionWithResult run { _, action ->
            action(transaction)
        }

        val clock: ClockMock = kmock()

        clock._now returns Instant.fromEpochMilliseconds(now)
        // When
        LocalRepository(queries, clock).storeImages(
            query = query,
            pageId = pageId,
            imageInfo = RepositoryContract.RemoteRepositoryResponse(
                overview = listOf(overviewItem1, overviewItem2),
                detailedView = listOf(detailViewItem1, detailViewItem2),
                totalAmountOfItems = total
            )
        )

        // Then
        asserter.assertOrder {
            queries._transactionWithResult.hasBeenCalled()
            queries._addQuery.hasBeenStrictlyCalledWith(
                query,
                200,
                total,
                Instant.fromEpochMilliseconds(tomorrow)
            )
            queries._addImageQuery.hasBeenStrictlyCalledWith(query, image1.imageId)
            queries._addImage.hasBeenStrictlyCalledWith(
                image1.imageId, // imageId
                image1.user, // user
                image1.tags, // tags
                image1.downloads, // downloads
                image1.likes, // likes
                image1.comments, // comments
                image1.previewUrl, // previewUrl
                image1.largeUrl // large
            )

            queries._addImageQuery.hasBeenStrictlyCalledWith(query, image2.imageId)
            queries._addImage.hasBeenStrictlyCalledWith(
                image2.imageId, // imageId
                image2.user, // user
                image2.tags, // tags
                image2.downloads, // downloads
                image2.likes, // likes
                image2.comments, // comments
                image2.previewUrl, // previewUrl
                image2.largeUrl // large
            )
        }
    }

    @Test
    fun `Given storeImage it just runs while just updating the Queries if the pageId is less equal then 4`() {
        // Given
        repeat(4) { pageId ->
            val transaction: TransactionWithReturnMock<Any?> = kmock(
                templateType = TransactionWithReturn::class
            )

            val query: String = fixture.fixture()
            val total: Int = fixture.fixture()
            val now: Long = fixture.fixture(PublicApi.Sign.POSITIVE)
            val tomorrow = now + 86400000

            val image1 = fixture.imageFixture()
            val image2 = fixture.imageFixture()
            val detailViewItem1 = DetailViewItem(
                imageUrl = image1.largeUrl,
                userName = image1.user,
                tags = image1.tags,
                likes = image1.likes.toUInt(),
                comments = image1.comments.toUInt(),
                downloads = image1.downloads.toUInt()
            )
            val detailViewItem2 = DetailViewItem(
                imageUrl = image2.largeUrl,
                userName = image2.user,
                tags = image2.tags,
                likes = image2.likes.toUInt(),
                comments = image2.comments.toUInt(),
                downloads = image2.downloads.toUInt()
            )
            val overviewItem1 = OverviewItem(
                id = image1.imageId,
                thumbnail = image1.previewUrl,
                userName = image1.user,
                tags = image1.tags
            )
            val overviewItem2 = OverviewItem(
                id = image2.imageId,
                thumbnail = image2.previewUrl,
                userName = image2.user,
                tags = image2.tags
            )

            queries._transactionWithResult run { _, action ->
                action(transaction)
            }

            val clock: ClockMock = kmock()

            clock._now returns Instant.fromEpochMilliseconds(now)
            // When
            LocalRepository(queries, clock).storeImages(
                query = query,
                pageId = (pageId).toUShort(),
                imageInfo = RepositoryContract.RemoteRepositoryResponse(
                    overview = listOf(overviewItem1, overviewItem2),
                    detailedView = listOf(detailViewItem1, detailViewItem2),
                    totalAmountOfItems = total
                )
            )

            // Then
            verify(atLeast = 1) {
                queries._addQuery.hasBeenStrictlyCalledWith(
                    query,
                    200,
                    total,
                    Instant.fromEpochMilliseconds(tomorrow)
                )
            }
        }
    }

    @Test
    fun `Given storeImage it just runs while just updating the Queries if the pageId is greater then 4`() {
        // Given
        repeat(4) { pageId ->
            val transaction: TransactionWithReturnMock<Any?> = kmock(
                templateType = TransactionWithReturn::class
            )

            val query: String = fixture.fixture()
            val total: Int = fixture.fixture(400, 500)
            val now: Long = fixture.fixture(PublicApi.Sign.POSITIVE)

            val image1 = fixture.imageFixture()
            val image2 = fixture.imageFixture()
            val detailViewItem1 = DetailViewItem(
                imageUrl = image1.largeUrl,
                userName = image1.user,
                tags = image1.tags,
                likes = image1.likes.toUInt(),
                comments = image1.comments.toUInt(),
                downloads = image1.downloads.toUInt()
            )
            val detailViewItem2 = DetailViewItem(
                imageUrl = image2.largeUrl,
                userName = image2.user,
                tags = image2.tags,
                likes = image2.likes.toUInt(),
                comments = image2.comments.toUInt(),
                downloads = image2.downloads.toUInt()
            )
            val overviewItem1 = OverviewItem(
                id = image1.imageId,
                thumbnail = image1.previewUrl,
                userName = image1.user,
                tags = image1.tags
            )
            val overviewItem2 = OverviewItem(
                id = image2.imageId,
                thumbnail = image2.previewUrl,
                userName = image2.user,
                tags = image2.tags
            )

            queries._transactionWithResult run { _, action ->
                action(transaction)
            }

            val clock: ClockMock = kmock()

            clock._now returns Instant.fromEpochMilliseconds(now)
            // When
            LocalRepository(queries, clock).storeImages(
                query = query,
                pageId = (pageId + 5).toUShort(),
                imageInfo = RepositoryContract.RemoteRepositoryResponse(
                    overview = listOf(overviewItem1, overviewItem2),
                    detailedView = listOf(detailViewItem1, detailViewItem2),
                    totalAmountOfItems = total
                )
            )

            // Then
            verify(exactly = 1) {
                queries._updatePageIndex.hasBeenStrictlyCalledWith(400, query)
            }

            queries._clearMock()
        }
    }

    @Test
    fun `Given storeImage it just runs while just updating the Queries if the pageId is greater then 8`() {
        // Given
        repeat(2) { pageId ->
            val transaction: TransactionWithReturnMock<Any?> = kmock(
                templateType = TransactionWithReturn::class
            )

            val query: String = fixture.fixture()
            val total = 500
            val now: Long = fixture.fixture(PublicApi.Sign.POSITIVE)

            val image1 = fixture.imageFixture()
            val image2 = fixture.imageFixture()
            val detailViewItem1 = DetailViewItem(
                imageUrl = image1.largeUrl,
                userName = image1.user,
                tags = image1.tags,
                likes = image1.likes.toUInt(),
                comments = image1.comments.toUInt(),
                downloads = image1.downloads.toUInt()
            )
            val detailViewItem2 = DetailViewItem(
                imageUrl = image2.largeUrl,
                userName = image2.user,
                tags = image2.tags,
                likes = image2.likes.toUInt(),
                comments = image2.comments.toUInt(),
                downloads = image2.downloads.toUInt()
            )
            val overviewItem1 = OverviewItem(
                id = image1.imageId,
                thumbnail = image1.previewUrl,
                userName = image1.user,
                tags = image1.tags
            )
            val overviewItem2 = OverviewItem(
                id = image2.imageId,
                thumbnail = image2.previewUrl,
                userName = image2.user,
                tags = image2.tags
            )

            queries._transactionWithResult run { _, action ->
                action(transaction)
            }

            val clock: ClockMock = kmock()

            clock._now returns Instant.fromEpochMilliseconds(now)
            // When
            LocalRepository(queries, clock).storeImages(
                query = query,
                pageId = (pageId + 9).toUShort(),
                imageInfo = RepositoryContract.RemoteRepositoryResponse(
                    overview = listOf(overviewItem1, overviewItem2),
                    detailedView = listOf(detailViewItem1, detailViewItem2),
                    totalAmountOfItems = total
                )
            )

            // Then
            verify(exactly = 1) {
                queries._updatePageIndex.hasBeenStrictlyCalledWith(500, query)
            }

            queries._clearMock()
        }
    }

    @Test
    fun `Given storeImage it just runs while just updating the Queries it does not exceed the actual total`() {
        // Given
        val transaction: TransactionWithReturnMock<Any?> = kmock(
            templateType = TransactionWithReturn::class
        )

        val query: String = fixture.fixture()
        val total: Int = fixture.fixture(200, 400)
        val now: Long = fixture.fixture(PublicApi.Sign.POSITIVE)

        val image1 = fixture.imageFixture()
        val image2 = fixture.imageFixture()
        val detailViewItem1 = DetailViewItem(
            imageUrl = image1.largeUrl,
            userName = image1.user,
            tags = image1.tags,
            likes = image1.likes.toUInt(),
            comments = image1.comments.toUInt(),
            downloads = image1.downloads.toUInt()
        )
        val detailViewItem2 = DetailViewItem(
            imageUrl = image2.largeUrl,
            userName = image2.user,
            tags = image2.tags,
            likes = image2.likes.toUInt(),
            comments = image2.comments.toUInt(),
            downloads = image2.downloads.toUInt()
        )
        val overviewItem1 = OverviewItem(
            id = image1.imageId,
            thumbnail = image1.previewUrl,
            userName = image1.user,
            tags = image1.tags
        )
        val overviewItem2 = OverviewItem(
            id = image2.imageId,
            thumbnail = image2.previewUrl,
            userName = image2.user,
            tags = image2.tags
        )

        queries._transactionWithResult run { _, action ->
            action(transaction)
        }

        val clock: ClockMock = kmock()

        clock._now returns Instant.fromEpochMilliseconds(now)
        // When
        LocalRepository(queries, clock).storeImages(
            query = query,
            pageId = (9).toUShort(),
            imageInfo = RepositoryContract.RemoteRepositoryResponse(
                overview = listOf(overviewItem1, overviewItem2),
                detailedView = listOf(detailViewItem1, detailViewItem2),
                totalAmountOfItems = total
            )
        )

        // Then
        verify(exactly = 1) {
            queries._updatePageIndex.hasBeenStrictlyCalledWith(total, query)
        }

        queries._clearMock()
    }
}
