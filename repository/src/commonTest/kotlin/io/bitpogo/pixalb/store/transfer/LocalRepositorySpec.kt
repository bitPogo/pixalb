/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.store.transfer

import com.squareup.sqldelight.Query
import io.bitpogo.pixalb.store.RepositoryContract
import io.bitpogo.pixalb.store.database.FetchQueryInfo
import io.bitpogo.pixalb.store.database.ImageQueries
import io.bitpogo.pixalb.store.database.ImageQueriesMock
import io.bitpogo.pixalb.store.database.Image
import io.bitpogo.pixalb.store.error.PixabayRepositoryError
import io.bitpogo.pixalb.store.kmock
import io.bitpogo.pixalb.store.mock.QueryStub
import io.bitpogo.pixalb.store.mock.SqlCursorStub
import io.bitpogo.pixalb.store.model.DetailedViewItem
import io.bitpogo.pixalb.store.model.OverviewItem
import kotlinx.datetime.Clock
import kotlinx.datetime.ClockMock
import kotlinx.datetime.Instant
import tech.antibytes.kfixture.PublicApi
import tech.antibytes.kfixture.fixture
import tech.antibytes.kfixture.kotlinFixture
import tech.antibytes.kfixture.listFixture
import tech.antibytes.kmock.MockCommon
import tech.antibytes.kmock.verification.assertProxy
import tech.antibytes.util.test.coroutine.runBlockingTestWithTimeout
import tech.antibytes.util.test.fulfils
import tech.antibytes.util.test.mustBe
import tech.antibytes.util.test.sameAs
import kotlin.test.BeforeTest
import kotlin.test.Test

@MockCommon(
    ImageQueries::class,
    Clock::class,
)
class LocalRepositorySpec {
    private val fixture = kotlinFixture()
    private val queries: ImageQueriesMock = kmock()

    @BeforeTest
    fun setUp() {
        queries._clearMock()
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
            largeUrl = fixture(),
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
            fixture.fixture(),
        )

        // Then
        result.error!! fulfils PixabayRepositoryError.UnsuccessfulDatabaseAccess::class
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
            1,
        )

        // Then
        result.error!! fulfils PixabayRepositoryError.UnsuccessfulDatabaseAccess::class
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
            1
        )

        // Then
        result.error!! fulfils PixabayRepositoryError.MissingEntry::class
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
            2,
        )

        // Then
        result.error!! fulfils PixabayRepositoryError.MissingPage::class
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
            3,
        )

        // Then
        result.error!! fulfils PixabayRepositoryError.EntryCap::class
    }

    @Test
    fun `Given fetchOverview is called returns an Overview`() = runBlockingTestWithTimeout {
        // Given
        val clock: ClockMock = kmock()
        val query: String = fixture.fixture()
        val pageId: Int = fixture.fixture(1, 6)
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
                thumbnail = overviewItem.previewUrl,
                userName = overviewItem.user,
                tags = overviewItem.tags,
            )
        )

        assertProxy {
            queries._fetchQueryInfoWithStringInstant.hasBeenStrictlyCalledWith(
                query, Instant.DISTANT_FUTURE
            )
            queries._fetchImagesWithStringLong.hasBeenStrictlyCalledWith(
                inquiry, ((pageId - 1) * 50).toLong()
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
            fixture.fixture(),
        )

        // Then
        result.error!! fulfils PixabayRepositoryError.UnsuccessfulDatabaseAccess::class
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
            fixture.fixture(),
        )

        // Then
        result.value mustBe DetailedViewItem(
            imageUrl = image.largeUrl,
            userName = image.user,
            tags = image.tags,
            likes = image.likes.toUInt(),
            comments = image.comments.toUInt(),
            downloads = image.downloads.toUInt(),
        )
    }
}
