/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.store.transfer

import io.bitpogo.pixalb.client.ClientContract
import io.bitpogo.pixalb.client.ClientMock
import io.bitpogo.pixalb.client.error.PixabayClientError
import io.bitpogo.pixalb.album.domain.RepositoryContract
import io.bitpogo.pixalb.album.domain.error.PixabayError
import io.bitpogo.pixalb.store.fixture.StringAlphaGenerator
import io.bitpogo.pixalb.store.fixture.pixabayItemsFixture
import io.bitpogo.pixalb.store.kmock
import io.bitpogo.pixalb.album.domain.model.DetailedViewItem
import io.bitpogo.pixalb.album.domain.model.OverviewItem
import io.bitpogo.pixalb.album.transfer.RemoteRepository
import io.bitpogo.util.coroutine.result.Failure
import io.bitpogo.util.coroutine.result.Success
import kotlin.math.ceil
import kotlin.test.Test
import tech.antibytes.kfixture.fixture
import tech.antibytes.kfixture.kotlinFixture
import tech.antibytes.kfixture.listFixture
import tech.antibytes.kfixture.qualifier.qualifiedBy
import tech.antibytes.kmock.MockCommon
import tech.antibytes.kmock.verification.assertProxy
import tech.antibytes.util.test.coroutine.runBlockingTestWithTimeout
import tech.antibytes.util.test.fulfils
import tech.antibytes.util.test.mustBe

@MockCommon(
    ClientContract.Client::class
)
class RemoteRepositorySpec {
    private val ascii = qualifiedBy("ascii")
    private val fixture = kotlinFixture {
        addGenerator(
            String::class,
            StringAlphaGenerator,
            ascii
        )
    }
    private val client: ClientMock = kmock()

    @Test
    fun `It fulfils RemoteRepository`() {
        RemoteRepository(client) fulfils RepositoryContract.RemoteRepository::class
    }

    @Test
    fun `Given fetch is called with a pageId and a query it delegates the query and returns its mapped result`() = runBlockingTestWithTimeout(2000) {
        // Given
        val pageId: UInt = fixture.fixture(1u, 6u)
        val query: String = fixture.fixture()
        val response = fixture.pixabayItemsFixture(size = 1) { fixture.fixture(ascii) }

        client._fetchImages returns Success(response)

        // When
        val (total, overview, details, imageIds) = RemoteRepository(client).fetch(query, pageId).unwrap()

        // Then
        total mustBe response.total
        overview.size mustBe 1
        overview.first() mustBe OverviewItem(
            userName = response.items.first().user,
            thumbnail = response.items.first().preview,
            tags = listOf(response.items.first().tags)
        )
        details.size mustBe 1
        details.first() mustBe DetailedViewItem(
            userName = response.items.first().user,
            imageUrl = response.items.first().large,
            tags = listOf(response.items.first().tags),
            likes = response.items.first().likes,
            downloads = response.items.first().downloads,
            comments = response.items.first().comments
        )
        imageIds.size mustBe 1
        imageIds.first() mustBe response.items.first().id

        assertProxy {
            client._fetchImages.hasBeenStrictlyCalledWith(query, ceil(pageId.toDouble() / 2).toUInt())
        }
    }

    @Test
    fun `Given fetch is called with a pageId and a query it delegates the query and returns its mapped result with tags`() = runBlockingTestWithTimeout(2000) {
        // Given
        val pageId: UInt = fixture.fixture(1u, 6u)
        val query: String = fixture.fixture()
        val tags: List<String> = fixture.listFixture(size = 3) { fixture.fixture(ascii) }
        val response = fixture.pixabayItemsFixture(size = 1) { tags.joinToString(", ") }

        client._fetchImages returns Success(response)

        // When
        val (total, overview, details, imageIds) = RemoteRepository(client).fetch(query, pageId).unwrap()

        // Then
        total mustBe response.total
        overview.size mustBe 1
        overview.first() mustBe OverviewItem(
            userName = response.items.first().user,
            thumbnail = response.items.first().preview,
            tags = tags
        )
        details.size mustBe 1
        details.first() mustBe DetailedViewItem(
            userName = response.items.first().user,
            imageUrl = response.items.first().large,
            tags = tags,
            likes = response.items.first().likes,
            downloads = response.items.first().downloads,
            comments = response.items.first().comments
        )
        imageIds.size mustBe 1
        imageIds.first() mustBe response.items.first().id

        assertProxy {
            client._fetchImages.hasBeenStrictlyCalledWith(query, ceil(pageId.toDouble() / 2).toUInt())
        }
    }

    @Test
    fun `Given fetch is called with a pageId and a query it delegates the query and returns its mapped error`() = runBlockingTestWithTimeout(2000) {
        // Given
        val pageId: UInt = fixture.fixture(1u, 6u)
        val query: String = fixture.fixture()
        val error = PixabayClientError.RequestError(400)

        client._fetchImages returns Failure(error)

        // When
        val result = RemoteRepository(client).fetch(query, pageId).error!!

        // Then
        result fulfils PixabayError.UnsuccessfulRequest::class
        result.cause mustBe error

        assertProxy {
            client._fetchImages.hasBeenStrictlyCalledWith(query, ceil(pageId.toDouble() / 2).toUInt())
        }
    }

    @Test
    fun `Given fetch is called with a pageId and a query it delegates the query and returns its mapped no connection error`() = runBlockingTestWithTimeout(2000) {
        // Given
        val pageId: UInt = fixture.fixture(1u, 6u)
        val query: String = fixture.fixture()
        val error = PixabayClientError.NoConnection()

        client._fetchImages returns Failure(error)

        // When
        val result = RemoteRepository(client).fetch(query, pageId).error!!

        // Then
        result fulfils PixabayError.NoConnection::class

        assertProxy {
            client._fetchImages.hasBeenStrictlyCalledWith(query, ceil(pageId.toDouble() / 2).toUInt())
        }
    }
}
