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
import kotlin.js.JsName
import kotlin.test.Test
import kotlinx.coroutines.channels.Channel
import tech.antibytes.kfixture.fixture
import tech.antibytes.kfixture.kotlinFixture
import tech.antibytes.kmock.MockCommon
import tech.antibytes.util.test.annotations.RobolectricConfig
import tech.antibytes.util.test.annotations.RobolectricTestRunner
import tech.antibytes.util.test.annotations.RunWithRobolectricTestRunner
import tech.antibytes.util.test.coroutine.AsyncTestReturnValue
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

    @Test
    @JsName("fn1")
    fun `It fetches, stores and resolves an overview`(): AsyncTestReturnValue {
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
        runBlockingTestWithTimeout {
            db.open(PixabayDataBase.Schema)
        }

        return runBlockingTestWithTimeout {
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
            result.receive() mustBe AlbumContract.OverviewStoreState.Initial

            // When
            store.fetchOverview(query, pageId)

            // Then
            result.receive() mustBe AlbumContract.OverviewStoreState.Pending

            (result.receive() as AlbumContract.OverviewStoreState.Accepted).value mustBe overview

            db.close()
        }
    }

    @Test
    @JsName("fn2")
    fun `It fetches an overview only once`(): AsyncTestReturnValue {
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
        runBlockingTestWithTimeout {
            db.open(PixabayDataBase.Schema)
        }

        return runBlockingTestWithTimeout {
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
            result.receive() mustBe AlbumContract.OverviewStoreState.Initial

            // When
            store.fetchOverview(query, pageId)

            // Then
            result.receive() mustBe AlbumContract.OverviewStoreState.Pending

            var items = (result.receive() as AlbumContract.OverviewStoreState.Accepted).value
            items.forEachIndexed { idx, overviewItem ->
                overviewItem mustBe overview[idx]
            }

            // When
            store.fetchOverview(query, pageId)

            // Then
            result.receive() mustBe AlbumContract.OverviewStoreState.Pending

            items = (result.receive() as AlbumContract.OverviewStoreState.Accepted).value
            items.forEachIndexed { idx, overviewItem ->
                overviewItem.thumbnail mustBe overview[idx].thumbnail
            }

            db.close()
        }
    }
}
