/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.album.integration

import io.bitpogo.pixalb.album.AlbumContract
import io.bitpogo.pixalb.album.database.DatabaseDriver
import io.bitpogo.pixalb.album.database.PixabayDataBase
import io.bitpogo.pixalb.album.domain.AlbumStore
import io.bitpogo.pixalb.album.domain.model.OverviewItem
import io.bitpogo.pixalb.album.fixture.pixabayItemsFixture
import io.bitpogo.pixalb.album.kmock
import io.bitpogo.pixalb.album.testScope1
import io.bitpogo.pixalb.album.testScope2
import io.bitpogo.pixalb.client.ClientContract
import io.bitpogo.pixalb.client.ClientMock
import io.bitpogo.pixalb.client.error.PixabayClientError
import io.bitpogo.util.coroutine.result.Failure
import io.bitpogo.util.coroutine.result.Success
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.channels.Channel
import tech.antibytes.kfixture.fixture
import tech.antibytes.kfixture.kotlinFixture
import tech.antibytes.kmock.MockCommon
import tech.antibytes.util.test.annotations.RobolectricConfig
import tech.antibytes.util.test.annotations.RobolectricTestRunner
import tech.antibytes.util.test.annotations.RunWithRobolectricTestRunner
import tech.antibytes.util.test.coroutine.runBlockingTestWithTimeout
import tech.antibytes.util.test.mustBe

@MockCommon(
    ClientContract.Client::class
)
@RobolectricConfig(manifest = "--none")
@RunWithRobolectricTestRunner(RobolectricTestRunner::class)
class IntegrationTest {
    private val fixture = kotlinFixture()
    private val db = DatabaseDriver()

    @BeforeTest
    fun setUp() {
        db.open(PixabayDataBase.Schema)
    }

    @AfterTest
    fun tearDown() {
        db.close()
    }

    @Test
    fun `It fetches, stores and resolves an overview`() {
        // Given
        val client: ClientMock = kmock()
        val result = Channel<AlbumContract.OverviewStoreState>()
        val query: String = fixture.fixture()
        val pageId: UShort = fixture.fixture(1.toUShort(), 4.toUShort())

        val clientResponse = fixture.pixabayItemsFixture { "tag" }
        val overview = clientResponse.items.map { item ->
            OverviewItem(
                id = item.id,
                thumbnail = item.preview,
                userName = item.user,
                tags = item.tags.split(", ")
            )
        }

        client._fetchImages returns Success(clientResponse)
        // When
        val store = AlbumStore.getInstance(
            client,
            db.dataBase.imageQueries,
            { testScope2 },
            { testScope1 }
        )

        store.overview.subscribeWithSuspendingFunction { state ->
            result.send(state)
        }

        // Then
        runBlockingTestWithTimeout {
            result.receive() mustBe AlbumContract.OverviewStoreState.Initial
        }

        // When
        store.fetchOverview(query, pageId)

        // Then
        runBlockingTestWithTimeout {
            result.receive() mustBe AlbumContract.OverviewStoreState.Pending

            (result.receive() as AlbumContract.OverviewStoreState.Accepted).value mustBe overview
        }
    }

    @Test
    fun `It fetches an overview only once`() {
        // Given
        var firstCall = true
        val client: ClientMock = kmock()
        val result = Channel<AlbumContract.OverviewStoreState>()
        val query: String = fixture.fixture()
        val pageId: UShort = fixture.fixture(1.toUShort(), 4.toUShort())

        val clientResponse = fixture.pixabayItemsFixture { "tag" }
        val overview = clientResponse.items.map { item ->
            OverviewItem(
                id = item.id,
                thumbnail = item.preview,
                userName = item.user,
                tags = item.tags.split(", ")
            )
        }

        client._fetchImages runs { _, _ ->
            if (firstCall) {
                firstCall = false
                Success(clientResponse)
            } else {
                Failure(PixabayClientError.RequestError(23))
            }
        }
        // When
        val store = AlbumStore.getInstance(
            client,
            db.dataBase.imageQueries,
            { testScope2 },
            { testScope1 }
        )

        store.overview.subscribeWithSuspendingFunction { state ->
            result.send(state)
        }

        // Then
        runBlockingTestWithTimeout {
            result.receive() mustBe AlbumContract.OverviewStoreState.Initial
        }

        // When
        store.fetchOverview(query, pageId)

        // Then
        runBlockingTestWithTimeout {
            result.receive() mustBe AlbumContract.OverviewStoreState.Pending

            (result.receive() as AlbumContract.OverviewStoreState.Accepted).value mustBe overview
        }

        // When
        store.fetchOverview(query, pageId)

        // Then
        runBlockingTestWithTimeout {
            result.receive() mustBe AlbumContract.OverviewStoreState.Pending

            (result.receive() as AlbumContract.OverviewStoreState.Accepted).value mustBe overview
        }
    }
}
