/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.album.domain

import io.bitpogo.pixalb.album.AlbumContract
import io.bitpogo.pixalb.album.domain.error.PixabayError
import io.bitpogo.pixalb.album.fixture.detailviewItemFixture
import io.bitpogo.pixalb.album.fixture.detailviewItemsFixture
import io.bitpogo.pixalb.album.fixture.overviewItemsFixture
import io.bitpogo.pixalb.album.kmock
import io.bitpogo.pixalb.album.testScope1
import io.bitpogo.pixalb.album.testScope2
import io.bitpogo.util.coroutine.result.Failure
import io.bitpogo.util.coroutine.result.Success
import io.bitpogo.util.coroutine.wrapper.CoroutineWrapperContract.CoroutineScopeDispatcher
import io.bitpogo.util.coroutine.wrapper.CoroutineWrapperContract.SharedFlowWrapper
import io.bitpogo.util.coroutine.wrapper.SharedFlowWrapperMock
import kotlin.js.JsName
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.core.qualifier.named
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import tech.antibytes.kfixture.fixture
import tech.antibytes.kfixture.kotlinFixture
import tech.antibytes.kmock.MockCommon
import tech.antibytes.kmock.verification.assertProxy
import tech.antibytes.kmock.verification.constraints.any
import tech.antibytes.util.test.annotations.IgnoreJs
import tech.antibytes.util.test.coroutine.runBlockingTestWithTimeout
import tech.antibytes.util.test.fulfils
import tech.antibytes.util.test.sameAs

@MockCommon(
    RepositoryContract.RemoteRepository::class,
    RepositoryContract.LocalRepository::class,
    CoroutineScopeDispatcher::class,
    SharedFlowWrapper::class
)
@IgnoreJs // Works but not when run with other tests on Js
class AlbumStoreSpec {
    private val fixture = kotlinFixture()
    private val remoteRepository: RemoteRepositoryMock = kmock()
    private val localRepository: LocalRepositoryMock = kmock()
    private val overviewFlowWrapper: SharedFlowWrapperMock<AlbumContract.OverviewStoreState> = kmock(
        templateType = SharedFlowWrapper::class
    )

    @BeforeTest
    fun setup() {
        remoteRepository._clearMock()
        localRepository._clearMock()
    }

    @Test
    @JsName("fn1")
    fun `It fulfils Store`() {
        AlbumStore(koinApplication()) fulfils AlbumContract.Store::class
    }

    @Test
    @JsName("fn2")
    fun `Given fetchOverview is called with a query and pageId it delegates the call to the local repository and emits its response`() {
        // Given
        val query: String = fixture.fixture()
        val pageId: UShort = fixture.fixture()

        val expectedOverview = fixture.overviewItemsFixture()

        val result = Channel<AlbumContract.OverviewStoreState>()
        val overviewFlow: MutableStateFlow<AlbumContract.OverviewStoreState> = MutableStateFlow(
            AlbumContract.OverviewStoreState.Initial
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
            if (state != AlbumContract.OverviewStoreState.Pending) {
                result.send(state)
            }
        }.launchIn(testScope2)

        localRepository._fetchOverview returns Success(expectedOverview)

        // When
        val album = AlbumStore(koin)

        // Then
        runBlockingTestWithTimeout {
            result.receive() sameAs AlbumContract.OverviewStoreState.Initial
        }

        // When
        album.fetchOverview(query, pageId)

        // Then
        runBlockingTestWithTimeout {
            val success = result.receive()
            success fulfils AlbumContract.OverviewStoreState.Accepted::class
            (success as AlbumContract.OverviewStoreState.Accepted).value sameAs expectedOverview

            assertProxy {
                localRepository._fetchOverview.hasBeenStrictlyCalledWith(query, pageId)
            }
        }
    }

    @Test
    @JsName("fn3")
    fun `Given fetchOverview is called with a query and pageId it delegates the call to the local repository and emits its errors`() {
        // Given
        val query: String = fixture.fixture()
        val pageId: UShort = fixture.fixture()

        val expectedError = PixabayError.EntryCap

        val result = Channel<AlbumContract.OverviewStoreState>()
        val overviewFlow: MutableStateFlow<AlbumContract.OverviewStoreState> = MutableStateFlow(
            AlbumContract.OverviewStoreState.Initial
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
            if (state != AlbumContract.OverviewStoreState.Pending) {
                result.send(state)
            }
        }.launchIn(testScope2)

        localRepository._fetchOverview returns Failure(expectedError)

        // When
        val album = AlbumStore(koin)

        // Then
        runBlockingTestWithTimeout {
            result.receive() sameAs AlbumContract.OverviewStoreState.Initial
        }

        // When
        album.fetchOverview(query, pageId)

        // Then
        runBlockingTestWithTimeout {
            val error = result.receive()
            error fulfils AlbumContract.OverviewStoreState.Error::class
            (error as AlbumContract.OverviewStoreState.Error).value sameAs expectedError

            assertProxy {
                localRepository._fetchOverview.hasBeenStrictlyCalledWith(query, pageId)
            }
        }
    }

    @Test
    @JsName("fn4")
    fun `Given fetchOverview is called with a query and pageId it delegates the call to the local repository and emits a MissingEntry it uses the remote repository while emitting its errors`() {
        // Given
        val query: String = fixture.fixture()
        val pageId: UShort = fixture.fixture()

        val expectedError = PixabayError.NoConnection

        val result = Channel<AlbumContract.OverviewStoreState>()
        val overviewFlow: MutableStateFlow<AlbumContract.OverviewStoreState> = MutableStateFlow(
            AlbumContract.OverviewStoreState.Initial
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
            if (state != AlbumContract.OverviewStoreState.Pending) {
                result.send(state)
            }
        }.launchIn(testScope2)

        localRepository._fetchOverview returns Failure(PixabayError.MissingEntry)
        remoteRepository._fetch returns Failure(expectedError)

        // When
        val album = AlbumStore(koin)

        // Then
        runBlockingTestWithTimeout {
            result.receive() sameAs AlbumContract.OverviewStoreState.Initial
        }

        // When
        album.fetchOverview(query, pageId)

        // Then
        runBlockingTestWithTimeout {
            val error = result.receive()
            error fulfils AlbumContract.OverviewStoreState.Error::class
            (error as AlbumContract.OverviewStoreState.Error).value sameAs expectedError

            assertProxy {
                localRepository._fetchOverview.hasBeenStrictlyCalledWith(query, pageId)
                remoteRepository._fetch.hasBeenStrictlyCalledWith(query, pageId)
            }
        }
    }

    @Test
    @JsName("fn5")
    fun `Given fetchOverview is called with a query and pageId it delegates the call to the local repository and emits a MissingEntry it uses the remote repository and store while ignoring store errors`() {
        // Given
        val query: String = fixture.fixture()
        val pageId: UShort = fixture.fixture()

        val expectedError = PixabayError.UnsuccessfulDatabaseAccess(RuntimeException())

        val result = Channel<AlbumContract.OverviewStoreState>()
        val overviewFlow: MutableStateFlow<AlbumContract.OverviewStoreState> = MutableStateFlow(
            AlbumContract.OverviewStoreState.Initial
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
            if (state != AlbumContract.OverviewStoreState.Pending) {
                result.send(state)
            }
        }.launchIn(testScope2)

        localRepository._fetchOverview returns Failure(PixabayError.MissingEntry)
        remoteRepository._fetch returns Success(
            RepositoryContract.RemoteRepositoryResponse(
                totalAmountOfItems = fixture.fixture(),
                overview = fixture.overviewItemsFixture(),
                detailedView = fixture.detailviewItemsFixture()
            )
        )
        localRepository._storeImages returns Failure(expectedError)

        // When
        val album = AlbumStore(koin)

        // Then
        runBlockingTestWithTimeout {
            result.receive() sameAs AlbumContract.OverviewStoreState.Initial
        }

        // When
        album.fetchOverview(query, pageId)

        // Then
        runBlockingTestWithTimeout {
            val success = result.receive()
            success fulfils AlbumContract.OverviewStoreState.Accepted::class

            assertProxy {
                localRepository._fetchOverview.hasBeenStrictlyCalledWith(query, pageId)
                remoteRepository._fetch.hasBeenStrictlyCalledWith(query, pageId)
                localRepository._storeImages.hasBeenStrictlyCalledWith(query, pageId, any())
            }
        }
    }

    @Test
    @JsName("fn6")
    fun `Given fetchOverview is called with a query and pageId it delegates the call to the local repository and emits a MissingEntry it uses the remote repository while storing its result`() {
        // Given
        val query: String = fixture.fixture()
        val pageId: UShort = fixture.fixture()

        val expectedBundle = RepositoryContract.RemoteRepositoryResponse(
            totalAmountOfItems = fixture.fixture(),
            overview = fixture.overviewItemsFixture(),
            detailedView = fixture.detailviewItemsFixture()
        )

        val result = Channel<AlbumContract.OverviewStoreState>()
        val overviewFlow: MutableStateFlow<AlbumContract.OverviewStoreState> = MutableStateFlow(
            AlbumContract.OverviewStoreState.Initial
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
            if (state != AlbumContract.OverviewStoreState.Pending) {
                result.send(state)
            }
        }.launchIn(testScope2)

        localRepository._fetchOverview returns Failure(PixabayError.MissingEntry)
        localRepository._storeImages returns Success(Unit)
        remoteRepository._fetch returns Success(expectedBundle)

        // When
        val album = AlbumStore(koin)

        // Then
        runBlockingTestWithTimeout {
            result.receive() sameAs AlbumContract.OverviewStoreState.Initial
        }

        // When
        album.fetchOverview(query, pageId)

        // Then
        runBlockingTestWithTimeout {
            val success = result.receive()
            success fulfils AlbumContract.OverviewStoreState.Accepted::class
            (success as AlbumContract.OverviewStoreState.Accepted).value sameAs expectedBundle.overview

            assertProxy {
                localRepository._fetchOverview.hasBeenStrictlyCalledWith(query, pageId)
                remoteRepository._fetch.hasBeenStrictlyCalledWith(query, pageId)
                localRepository._storeImages.hasBeenStrictlyCalledWith(query, pageId, expectedBundle)
            }
        }
    }

    @Test
    @JsName("fn7")
    fun `Given fetchOverview is called with a query and pageId it delegates the call to the local repository and emits a MissingPage it uses the remote repository while emitting its errors`() {
        // Given
        val query: String = fixture.fixture()
        val pageId: UShort = fixture.fixture()

        val expectedError = PixabayError.NoConnection

        val result = Channel<AlbumContract.OverviewStoreState>()
        val overviewFlow: MutableStateFlow<AlbumContract.OverviewStoreState> = MutableStateFlow(
            AlbumContract.OverviewStoreState.Initial
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
            if (state != AlbumContract.OverviewStoreState.Pending) {
                result.send(state)
            }
        }.launchIn(testScope2)

        localRepository._fetchOverview returns Failure(PixabayError.MissingPage)
        remoteRepository._fetch returns Failure(expectedError)

        // When
        val album = AlbumStore(koin)

        // Then
        runBlockingTestWithTimeout {
            result.receive() sameAs AlbumContract.OverviewStoreState.Initial
        }

        // When
        album.fetchOverview(query, pageId)

        // Then
        runBlockingTestWithTimeout {
            val error = result.receive()
            error fulfils AlbumContract.OverviewStoreState.Error::class
            (error as AlbumContract.OverviewStoreState.Error).value sameAs expectedError

            assertProxy {
                localRepository._fetchOverview.hasBeenStrictlyCalledWith(query, pageId)
                remoteRepository._fetch.hasBeenStrictlyCalledWith(query, pageId)
            }
        }
    }

    @Test
    @JsName("fn8")
    fun `Given fetchOverview is called with a query and pageId it delegates the call to the local repository and emits a MissingPage it uses the remote repository and store while ignoring store errors`() {
        // Given
        val query: String = fixture.fixture()
        val pageId: UShort = fixture.fixture()

        val expectedError = PixabayError.UnsuccessfulDatabaseAccess(RuntimeException())

        val result = Channel<AlbumContract.OverviewStoreState>()
        val overviewFlow: MutableStateFlow<AlbumContract.OverviewStoreState> = MutableStateFlow(
            AlbumContract.OverviewStoreState.Initial
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
            if (state != AlbumContract.OverviewStoreState.Pending) {
                result.send(state)
            }
        }.launchIn(testScope2)

        localRepository._fetchOverview returns Failure(PixabayError.MissingPage)
        remoteRepository._fetch returns Success(
            RepositoryContract.RemoteRepositoryResponse(
                totalAmountOfItems = fixture.fixture(),
                overview = fixture.overviewItemsFixture(),
                detailedView = fixture.detailviewItemsFixture()
            )
        )
        localRepository._storeImages returns Failure(expectedError)

        // When
        val album = AlbumStore(koin)

        // Then
        runBlockingTestWithTimeout {
            result.receive() sameAs AlbumContract.OverviewStoreState.Initial
        }

        // When
        album.fetchOverview(query, pageId)

        // Then
        runBlockingTestWithTimeout {
            val success = result.receive()
            success fulfils AlbumContract.OverviewStoreState.Accepted::class

            assertProxy {
                localRepository._fetchOverview.hasBeenStrictlyCalledWith(query, pageId)
                remoteRepository._fetch.hasBeenStrictlyCalledWith(query, pageId)
                localRepository._storeImages.hasBeenStrictlyCalledWith(query, pageId, any())
            }
        }
    }

    @Test
    @JsName("fn9")
    fun `Given fetchOverview is called with a query and pageId it delegates the call to the local repository and emits a MissingPage it uses the remote repository while storing its result`() {
        // Given
        val query: String = fixture.fixture()
        val pageId: UShort = fixture.fixture()

        val expectedBundle = RepositoryContract.RemoteRepositoryResponse(
            totalAmountOfItems = fixture.fixture(),
            overview = fixture.overviewItemsFixture(),
            detailedView = fixture.detailviewItemsFixture()
        )

        val result = Channel<AlbumContract.OverviewStoreState>()
        val overviewFlow: MutableStateFlow<AlbumContract.OverviewStoreState> = MutableStateFlow(
            AlbumContract.OverviewStoreState.Initial
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
            if (state != AlbumContract.OverviewStoreState.Pending) {
                result.send(state)
            }
        }.launchIn(testScope2)

        localRepository._fetchOverview returns Failure(PixabayError.MissingPage)
        localRepository._storeImages returns Success(Unit)
        remoteRepository._fetch returns Success(expectedBundle)

        // When
        val album = AlbumStore(koin)

        // Then
        runBlockingTestWithTimeout {
            result.receive() sameAs AlbumContract.OverviewStoreState.Initial
        }

        // When
        album.fetchOverview(query, pageId)

        // Then
        runBlockingTestWithTimeout {
            val success = result.receive()
            success fulfils AlbumContract.OverviewStoreState.Accepted::class
            (success as AlbumContract.OverviewStoreState.Accepted).value sameAs expectedBundle.overview

            assertProxy {
                localRepository._fetchOverview.hasBeenStrictlyCalledWith(query, pageId)
                remoteRepository._fetch.hasBeenStrictlyCalledWith(query, pageId)
                localRepository._storeImages.hasBeenStrictlyCalledWith(query, pageId, expectedBundle)
            }
        }
    }

    @Test
    @JsName("fn10")
    fun `Given fetchDetailedView is called with a imageId it delegates the call to the local repository and emits its errors`() {
        // Given
        val imageId: Long = fixture.fixture()

        val expectedError = PixabayError.UnsuccessfulDatabaseAccess(RuntimeException())

        val result = Channel<AlbumContract.DetailviewStoreState>()
        val detailviewFlow: MutableStateFlow<AlbumContract.DetailviewStoreState> = MutableStateFlow(
            AlbumContract.DetailviewStoreState.Initial
        )

        val koin = koinApplication {
            modules(
                module {
                    single(named(AlbumContract.KoinIds.DETAILVIEW_STORE_IN)) { detailviewFlow }
                    single(named(AlbumContract.KoinIds.DETAILVIEW_STORE_OUT)) { overviewFlowWrapper }
                    single<RepositoryContract.LocalRepository> { localRepository }
                    single(named(AlbumContract.KoinIds.PRODUCER_SCOPE)) { CoroutineScopeDispatcher { testScope1 } }
                }
            )
        }

        detailviewFlow.onEach { state ->
            if (state != AlbumContract.DetailviewStoreState.Pending) {
                result.send(state)
            }
        }.launchIn(testScope2)

        localRepository._fetchDetailedView returns Failure(expectedError)

        // When
        val album = AlbumStore(koin)

        // Then
        runBlockingTestWithTimeout {
            result.receive() sameAs AlbumContract.DetailviewStoreState.Initial
        }

        // When
        album.fetchDetailView(imageId)

        // Then
        runBlockingTestWithTimeout {
            val error = result.receive()
            error fulfils AlbumContract.DetailviewStoreState.Error::class
            (error as AlbumContract.DetailviewStoreState.Error).value sameAs expectedError

            assertProxy {
                localRepository._fetchDetailedView.hasBeenStrictlyCalledWith(imageId)
            }
        }
    }

    @Test
    @JsName("fn11")
    fun `Given fetchDetailedView is called with a imageId it delegates the call to the local repository and emits its result`() {
        // Given
        val imageId: Long = fixture.fixture()

        val detailview = fixture.detailviewItemFixture()

        val result = Channel<AlbumContract.DetailviewStoreState>()
        val detailviewFlow: MutableStateFlow<AlbumContract.DetailviewStoreState> = MutableStateFlow(
            AlbumContract.DetailviewStoreState.Initial
        )

        val koin = koinApplication {
            modules(
                module {
                    single(named(AlbumContract.KoinIds.DETAILVIEW_STORE_IN)) { detailviewFlow }
                    single(named(AlbumContract.KoinIds.DETAILVIEW_STORE_OUT)) { overviewFlowWrapper }
                    single<RepositoryContract.LocalRepository> { localRepository }
                    single(named(AlbumContract.KoinIds.PRODUCER_SCOPE)) { CoroutineScopeDispatcher { testScope1 } }
                }
            )
        }

        detailviewFlow.onEach { state ->
            if (state != AlbumContract.DetailviewStoreState.Pending) {
                result.send(state)
            }
        }.launchIn(testScope2)

        localRepository._fetchDetailedView returns Success(detailview)

        // When
        val album = AlbumStore(koin)

        // Then
        runBlockingTestWithTimeout {
            result.receive() sameAs AlbumContract.DetailviewStoreState.Initial
        }

        // When
        album.fetchDetailView(imageId)

        // Then
        runBlockingTestWithTimeout {
            val success = result.receive()
            success fulfils AlbumContract.DetailviewStoreState.Accepted::class
            (success as AlbumContract.DetailviewStoreState.Accepted).value sameAs detailview

            assertProxy {
                localRepository._fetchDetailedView.hasBeenStrictlyCalledWith(imageId)
            }
        }
    }
}
