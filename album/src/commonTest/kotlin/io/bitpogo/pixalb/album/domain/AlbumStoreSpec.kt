/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.album.domain

import io.bitpogo.pixalb.album.AlbumContract
import io.bitpogo.pixalb.album.domain.error.PixabayError
import io.bitpogo.pixalb.album.fixture.detailviewItemsFixture
import io.bitpogo.pixalb.album.fixture.overviewItemsFixture
import io.bitpogo.pixalb.album.testScope1
import io.bitpogo.pixalb.album.testScope2
import io.bitpogo.pixalb.store.kmock
import io.bitpogo.util.coroutine.result.Failure
import io.bitpogo.util.coroutine.result.Success
import io.bitpogo.util.coroutine.wrapper.CoroutineWrapperContract.CoroutineScopeDispatcher
import io.bitpogo.util.coroutine.wrapper.CoroutineWrapperContract.SharedFlowWrapper
import io.bitpogo.util.coroutine.wrapper.SharedFlowWrapperMock
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.core.qualifier.named
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import tech.antibytes.kfixture.fixture
import tech.antibytes.kfixture.kotlinFixture
import tech.antibytes.kfixture.listFixture
import tech.antibytes.kmock.MockCommon
import tech.antibytes.kmock.verification.assertProxy
import tech.antibytes.kmock.verification.constraints.any
import tech.antibytes.util.test.coroutine.runBlockingTestWithTimeout
import tech.antibytes.util.test.fulfils
import tech.antibytes.util.test.sameAs
import kotlin.test.BeforeTest
import kotlin.test.Test

@MockCommon(
    RepositoryContract.RemoteRepository::class,
    RepositoryContract.LocalRepository::class,
    CoroutineScopeDispatcher::class,
    SharedFlowWrapper::class,
)
class AlbumStoreSpec {
    private val fixture = kotlinFixture()
    private val remoteRepository: RemoteRepositoryMock = kmock()
    private val localRepository: LocalRepositoryMock = kmock()
    private val overviewFlowWrapper: SharedFlowWrapperMock<AlbumContract.OverviewState> = kmock(
        templateType = SharedFlowWrapper::class
    )

    @BeforeTest
    fun setup() {
        remoteRepository._clearMock()
        localRepository._clearMock()
    }

    @Test
    fun `It fulfils Store`() {
        AlbumStore(koinApplication()) fulfils AlbumContract.Store::class
    }

    @Test
    fun `Given fetchOverview is called with a query and pageId it delgates the call to the local repository and emits its response`() {
        // Given
        val query: String = fixture.fixture()
        val pageId: UShort = fixture.fixture()

        val expectedOverview = fixture.overviewItemsFixture()

        val result = Channel<AlbumContract.OverviewState>()
        val overviewFlow: MutableStateFlow<AlbumContract.OverviewState> = MutableStateFlow(
            AlbumContract.OverviewState.Initial
        )

        val koin = koinApplication {
            modules(
                module {
                    single(named(AlbumContract.KoinIds.OVERVIEW_STORE_IN)) { overviewFlow }
                    single(named(AlbumContract.KoinIds.OVERVIEW_STORE_OUT)) { overviewFlowWrapper }
                    single<RepositoryContract.LocalRepository> { localRepository }
                    single(named(AlbumContract.KoinIds.PRODUCER_SCOPE)) { CoroutineScopeDispatcher { testScope1 } }
                }
            )
        }

        overviewFlow.onEach { state ->
            result.send(state)
        }.launchIn(testScope2)

        localRepository._fetchOverview returns Success(expectedOverview)

        // When
        val album = AlbumStore(koin)

        // Then
        runBlockingTestWithTimeout {
            result.receive() sameAs AlbumContract.OverviewState.Initial
        }

        // When
        album.fetchOverview(query, pageId)

        // Then
        runBlockingTestWithTimeout {
            result.receive() sameAs AlbumContract.OverviewState.Pending

            val success = result.receive()
            success fulfils AlbumContract.OverviewState.Accepted::class
            (success as AlbumContract.OverviewState.Accepted).value sameAs expectedOverview

            assertProxy {
                localRepository._fetchOverview.hasBeenStrictlyCalledWith(query, pageId)
            }
        }
    }

    @Test
    fun `Given fetchOverview is called with a query and pageId it delgates the call to the local repository and emits its errors`() {
        // Given
        val query: String = fixture.fixture()
        val pageId: UShort = fixture.fixture()

        val expectedError = PixabayError.EntryCap()

        val result = Channel<AlbumContract.OverviewState>()
        val overviewFlow: MutableStateFlow<AlbumContract.OverviewState> = MutableStateFlow(
            AlbumContract.OverviewState.Initial
        )

        val koin = koinApplication {
            modules(
                module {
                    single(named(AlbumContract.KoinIds.OVERVIEW_STORE_IN)) { overviewFlow }
                    single(named(AlbumContract.KoinIds.OVERVIEW_STORE_OUT)) { overviewFlowWrapper }
                    single<RepositoryContract.LocalRepository> { localRepository }
                    single(named(AlbumContract.KoinIds.PRODUCER_SCOPE)) { CoroutineScopeDispatcher { testScope1 } }
                }
            )
        }

        overviewFlow.onEach { state ->
            delay(400)
            result.send(state)
        }.launchIn(testScope2)

        localRepository._fetchOverview returns Failure(expectedError)

        // When
        val album = AlbumStore(koin)

        // Then
        runBlockingTestWithTimeout {
            result.receive() sameAs AlbumContract.OverviewState.Initial
        }

        // When
        album.fetchOverview(query, pageId)

        // Then
        runBlockingTestWithTimeout {
            result.receive() sameAs AlbumContract.OverviewState.Pending

            val error = result.receive()
            error fulfils AlbumContract.OverviewState.Error::class
            (error as AlbumContract.OverviewState.Error).value sameAs expectedError

            assertProxy {
                localRepository._fetchOverview.hasBeenStrictlyCalledWith(query, pageId)
            }
        }
    }

    @Test
    fun `Given fetchOverview is called with a query and pageId it delgates the call to the local repository and emits a MissingPage it uses the remote repository while emtting its errors`() {
        // Given
        val query: String = fixture.fixture()
        val pageId: UShort = fixture.fixture()

        val expectedError = PixabayError.NoConnection()

        val result = Channel<AlbumContract.OverviewState>()
        val overviewFlow: MutableStateFlow<AlbumContract.OverviewState> = MutableStateFlow(
            AlbumContract.OverviewState.Initial
        )

        val koin = koinApplication {
            modules(
                module {
                    single(named(AlbumContract.KoinIds.OVERVIEW_STORE_IN)) { overviewFlow }
                    single(named(AlbumContract.KoinIds.OVERVIEW_STORE_OUT)) { overviewFlowWrapper }
                    single<RepositoryContract.RemoteRepository> { remoteRepository }
                    single<RepositoryContract.LocalRepository> { localRepository }
                    single(named(AlbumContract.KoinIds.PRODUCER_SCOPE)) { CoroutineScopeDispatcher { testScope1 } }
                }
            )
        }

        overviewFlow.onEach { state ->
            result.send(state)
        }.launchIn(testScope2)

        localRepository._fetchOverview returns Failure(PixabayError.MissingEntry())
        remoteRepository._fetch returns Failure(expectedError)

        // When
        val album = AlbumStore(koin)

        // Then
        runBlockingTestWithTimeout {
            result.receive() sameAs AlbumContract.OverviewState.Initial
        }

        // When
        album.fetchOverview(query, pageId)

        // Then
        runBlockingTestWithTimeout {
            result.receive() sameAs AlbumContract.OverviewState.Pending

            val error = result.receive()
            error fulfils AlbumContract.OverviewState.Error::class
            (error as AlbumContract.OverviewState.Error).value sameAs expectedError

            assertProxy {
                localRepository._fetchOverview.hasBeenStrictlyCalledWith(query, pageId)
                remoteRepository._fetch.hasBeenStrictlyCalledWith(query, pageId)
            }
        }
    }

    @Test
    fun `Given fetchOverview is called with a query and pageId it delgates the call to the local repository and emits a MissingPage it uses the remote repository and store while ignoring store errors`() {
        // Given
        val query: String = fixture.fixture()
        val pageId: UShort = fixture.fixture()

        val expectedError = PixabayError.UnsuccessfulDatabaseAccess(RuntimeException())

        val result = Channel<AlbumContract.OverviewState>()
        val overviewFlow: MutableStateFlow<AlbumContract.OverviewState> = MutableStateFlow(
            AlbumContract.OverviewState.Initial
        )

        val koin = koinApplication {
            modules(
                module {
                    single(named(AlbumContract.KoinIds.OVERVIEW_STORE_IN)) { overviewFlow }
                    single(named(AlbumContract.KoinIds.OVERVIEW_STORE_OUT)) { overviewFlowWrapper }
                    single<RepositoryContract.RemoteRepository> { remoteRepository }
                    single<RepositoryContract.LocalRepository> { localRepository }
                    single(named(AlbumContract.KoinIds.PRODUCER_SCOPE)) { CoroutineScopeDispatcher { testScope1 } }
                }
            )
        }

        overviewFlow.onEach { state ->
            result.send(state)
        }.launchIn(testScope2)

        localRepository._fetchOverview returns Failure(PixabayError.MissingEntry())
        remoteRepository._fetch returns Success(
            RepositoryContract.RemoteRepositoryResponse(
                totalAmountOfItems = fixture.fixture(),
                overview = fixture.overviewItemsFixture(),
                detailedView = fixture.detailviewItemsFixture(),
                imageIds = fixture.listFixture()
            )
        )
        localRepository._storeImages returns Failure(expectedError)

        // When
        val album = AlbumStore(koin)

        // Then
        runBlockingTestWithTimeout {
            result.receive() sameAs AlbumContract.OverviewState.Initial
        }

        // When
        album.fetchOverview(query, pageId)

        // Then
        runBlockingTestWithTimeout {
            result.receive() sameAs AlbumContract.OverviewState.Pending

            val success = result.receive()
            success fulfils AlbumContract.OverviewState.Accepted::class

            assertProxy {
                localRepository._fetchOverview.hasBeenStrictlyCalledWith(query, pageId)
                remoteRepository._fetch.hasBeenStrictlyCalledWith(query, pageId)
                localRepository._storeImages.hasBeenStrictlyCalledWith(query, pageId, any())
            }
        }
    }

    @Test
    fun `Given fetchOverview is called with a query and pageId it delgates the call to the local repository and emits a MissingPage it uses the remote repository while storing its result`() {
        // Given
        val query: String = fixture.fixture()
        val pageId: UShort = fixture.fixture()

        val expectedBundle = RepositoryContract.RemoteRepositoryResponse(
            totalAmountOfItems = fixture.fixture(),
            overview = fixture.overviewItemsFixture(),
            detailedView = fixture.detailviewItemsFixture(),
            imageIds = fixture.listFixture()
        )

        val result = Channel<AlbumContract.OverviewState>()
        val overviewFlow: MutableStateFlow<AlbumContract.OverviewState> = MutableStateFlow(
            AlbumContract.OverviewState.Initial
        )

        val koin = koinApplication {
            modules(
                module {
                    single(named(AlbumContract.KoinIds.OVERVIEW_STORE_IN)) { overviewFlow }
                    single(named(AlbumContract.KoinIds.OVERVIEW_STORE_OUT)) { overviewFlowWrapper }
                    single<RepositoryContract.RemoteRepository> { remoteRepository }
                    single<RepositoryContract.LocalRepository> { localRepository }
                    single(named(AlbumContract.KoinIds.PRODUCER_SCOPE)) { CoroutineScopeDispatcher { testScope1 } }
                }
            )
        }

        overviewFlow.onEach { state ->
            result.send(state)
        }.launchIn(testScope2)

        localRepository._fetchOverview returns Failure(PixabayError.MissingEntry())
        localRepository._storeImages returns Success(Unit)
        remoteRepository._fetch returns Success(expectedBundle)

        // When
        val album = AlbumStore(koin)

        // Then
        runBlockingTestWithTimeout {
            result.receive() sameAs AlbumContract.OverviewState.Initial
        }

        // When
        album.fetchOverview(query, pageId)

        // Then
        runBlockingTestWithTimeout {
            result.receive() sameAs AlbumContract.OverviewState.Pending

            val success = result.receive()
            success fulfils AlbumContract.OverviewState.Accepted::class
            (success as AlbumContract.OverviewState.Accepted).value sameAs expectedBundle.overview

            assertProxy {
                localRepository._fetchOverview.hasBeenStrictlyCalledWith(query, pageId)
                remoteRepository._fetch.hasBeenStrictlyCalledWith(query, pageId)
                localRepository._storeImages.hasBeenStrictlyCalledWith(query, pageId, expectedBundle)
            }
        }
    }

}
