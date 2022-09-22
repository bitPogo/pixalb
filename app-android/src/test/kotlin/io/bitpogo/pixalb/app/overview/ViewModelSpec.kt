/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.app.overview

import androidx.lifecycle.ViewModel
import io.bitpogo.pixalb.album.AlbumContract
import io.bitpogo.pixalb.album.AlbumContract.OverviewStoreState
import io.bitpogo.pixalb.album.StoreMock
import io.bitpogo.pixalb.album.domain.error.PixabayError
import io.bitpogo.pixalb.album.domain.model.OverviewItem
import io.bitpogo.pixalb.app.kmock
import io.bitpogo.pixalb.app.overview.OverviewContract.State
import io.bitpogo.pixalb.fixture.overviewItemsFixture
import io.bitpogo.util.coroutine.wrapper.CoroutineWrapperContract
import io.bitpogo.util.coroutine.wrapper.SharedFlowWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScheduler
import org.junit.Before
import org.junit.Test
import tech.antibytes.kfixture.fixture
import tech.antibytes.kfixture.kotlinFixture
import tech.antibytes.kmock.Mock
import tech.antibytes.kmock.verification.assertProxy
import tech.antibytes.util.test.coroutine.runBlockingTestWithTimeout
import tech.antibytes.util.test.fulfils
import tech.antibytes.util.test.mustBe

@Mock(
    AlbumContract.Store::class,
)
@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelSpec {
    private val fixture = kotlinFixture()
    private val store: StoreMock = kmock(relaxUnitFun = true)

    private val scheduler = TestCoroutineScheduler()
    private lateinit var storeScope: CoroutineScope

    private val overviewFlow: MutableStateFlow<OverviewStoreState> = MutableStateFlow(OverviewStoreState.Initial)
    private lateinit var overviewState: CoroutineWrapperContract.SharedFlowWrapper<OverviewStoreState>

    @Before
    fun setup() {
        overviewFlow.value = OverviewStoreState.Initial

        store._clearMock()

        storeScope = CoroutineScope(scheduler)
        overviewState = SharedFlowWrapper.getInstance(overviewFlow) { storeScope }
        store._overview returns overviewState
    }

    @Test
    fun `It fulfils ViewModel`() {
        OverviewViewModel(store) fulfils ViewModel::class
    }

    @Test
    fun `It fulfils OverviewViewModel`() {
        OverviewViewModel(store) fulfils OverviewContract.ViewModel::class
    }

    @Test
    fun `Its default search state is a empty String`() {
        OverviewViewModel(store).query.value mustBe ""
    }

    @Test
    fun `Given a setQuery is called it emits the new search state`() {
        // Given
        val query: String = fixture.fixture()
        val result = Channel<String>(
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
            capacity = 1,
        )

        // When
        val viewModel = OverviewViewModel(store)
        CoroutineScope(Dispatchers.Default).launch {
            viewModel.query.collect { state -> result.send(state) }
        }

        scheduler.advanceUntilIdle()

        // Then
        runBlockingTestWithTimeout {
            result.receive() mustBe ""
        }

        // When
        viewModel.setQuery(query)

        // Then
        runBlockingTestWithTimeout {
            result.receive() mustBe query
        }
    }

    @Test
    fun `Given a setQuery is called it ignores the new search state if the char cap is reached`() {
        // Given
        val query: String = fixture.fixture(101)
        val result = Channel<String>(
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
            capacity = 1,
        )
        var ignored = false

        // When
        val viewModel = OverviewViewModel(store)
        CoroutineScope(Dispatchers.Default).launch {
            viewModel.query.collect { state -> result.send(state) }
        }

        scheduler.advanceUntilIdle()

        // Then
        runBlockingTestWithTimeout {
            result.receive() mustBe ""
        }

        // When
        viewModel.setQuery(query)

        // Then
        try {
            runBlockingTestWithTimeout {
                result.receive() mustBe query
            }
        } catch (_: TimeoutCancellationException) {
            ignored = true
        }

        ignored mustBe true
    }

    @Test
    fun `Given a query is set and a search is called it propagates the query with pageId to the store`() {
        // Given
        val query: String = fixture.fixture()

        // When
        val viewModel = OverviewViewModel(store)
        viewModel.setQuery(query)
        viewModel.search()

        // Then
        assertProxy {
            store._fetchOverview.hasBeenStrictlyCalledWith(query, 1.toUShort())
        }
    }

    @Test
    fun `Given a query is set and a search is called it ignores empty queries`() {
        // When
        val viewModel = OverviewViewModel(store)
        viewModel.search()

        // Then
        assertProxy {
            store._fetchOverview.hasNoFurtherInvocations()
        }
    }

    @Test
    fun `Given a query is set, searched and nextPage is called it propagates the query with pageId to the store`() {
        // Given
        val query: String = fixture.fixture()

        // When
        val viewModel = OverviewViewModel(store)
        viewModel.setQuery(query)
        viewModel.search()
        viewModel.nextPage()

        // Then
        assertProxy {
            store._fetchOverview.hasBeenStrictlyCalledWith(query, 1.toUShort())
            store._fetchOverview.hasBeenStrictlyCalledWith(query, 2.toUShort())
        }
    }

    @Test
    fun `Given a query is set, searched and next is called it propagates the query with pageId to the store while respecting the cap`() {
        // Given
        val query: String = fixture.fixture()

        // When
        val viewModel = OverviewViewModel(store)
        viewModel.setQuery(query)
        viewModel.search()
        repeat(9) {
            viewModel.nextPage()
        }
        repeat(10) {
            viewModel.nextPage()
        }

        // Then
        assertProxy {
            (1 until 10).forEach { page ->
                store._fetchOverview.hasBeenStrictlyCalledWith(query, page.toUShort())
            }
            (1 until 10).forEach { _ ->
                store._fetchOverview.hasBeenStrictlyCalledWith(query, 10.toUShort())
            }
        }
    }

    @Test
    fun `Given a query is set, searched and next and be interrupted by setQuery is called it propagates the query to the store with pageId while keeping the query`() {
        // Given
        val query: String = fixture.fixture()

        // When
        val viewModel = OverviewViewModel(store)
        viewModel.setQuery(query)
        viewModel.search()
        viewModel.setQuery(fixture.fixture())
        viewModel.nextPage()

        // Then
        assertProxy {
            store._fetchOverview.hasBeenStrictlyCalledWith(query, 1.toUShort())
            store._fetchOverview.hasBeenStrictlyCalledWith(query, 2.toUShort())
        }
    }

    @Test
    fun `Its default overview state is set to Initial`() {
        OverviewViewModel(store).overview.value mustBe State.Initial
    }

    @Test
    fun `Given a event occurs with Error it does nothing`() {
        // Given
        val result = Channel<State>()
        var ignored = false

        // When
        val viewModel = OverviewViewModel(store)
        CoroutineScope(Dispatchers.Default).launch {
            viewModel.overview.collect { items -> result.send(items) }
        }

        CoroutineScope(Dispatchers.Default).launch {
            overviewFlow.update {
                OverviewStoreState.Error(PixabayError.UnsuccessfulRequest(RuntimeException()))
            }
        }

        scheduler.advanceUntilIdle()

        // Then
        try {
            runBlockingTestWithTimeout {
                result.receive() mustBe State.Initial
                result.receive() mustBe State.Initial
            }
        } catch (_: TimeoutCancellationException) {
            ignored = true
        }

        ignored mustBe true
    }

    @Test
    fun `Given a event occurs with Pending it does nothing`() {
        // Given
        val result = Channel<State>()
        var ignored = false

        // When
        val viewModel = OverviewViewModel(store)
        CoroutineScope(Dispatchers.Default).launch {
            viewModel.overview.collect { items -> result.send(items) }
        }

        CoroutineScope(Dispatchers.Default).launch {
            overviewFlow.update {
                OverviewStoreState.Pending
            }
        }

        scheduler.advanceUntilIdle()

        // Then
        try {
            runBlockingTestWithTimeout {
                result.receive() mustBe State.Initial
                result.receive() mustBe State.Initial
            }
        } catch (_: TimeoutCancellationException) {
            ignored = true
        }

        ignored mustBe true
    }

    @Test
    fun `Given a event occurs with Initial it does nothing`() {
        // Given
        val result = Channel<State>()
        var ignored = false

        // When
        val viewModel = OverviewViewModel(store)
        CoroutineScope(Dispatchers.Default).launch {
            viewModel.overview.collect { items -> result.send(items) }
        }

        CoroutineScope(Dispatchers.Default).launch {
            overviewFlow.update {
                OverviewStoreState.Initial
            }
        }

        scheduler.advanceUntilIdle()

        // Then
        try {
            runBlockingTestWithTimeout {
                result.receive() mustBe State.Initial
                result.receive() mustBe State.Initial
            }
        } catch (_: TimeoutCancellationException) {
            ignored = true
        }

        ignored mustBe true
    }

    @Test
    fun `Given a event occurs with Error and it is the no Connection Error it goes into NoConnection State`() {
        // Given
        val result = Channel<State>()

        // When
        val viewModel = OverviewViewModel(store)

        CoroutineScope(Dispatchers.Default).launch {
            viewModel.overview.collect { items -> result.send(items) }
        }

        CoroutineScope(Dispatchers.Default).launch {
            overviewFlow.update {
                OverviewStoreState.Error(PixabayError.NoConnection)
            }
        }

        scheduler.advanceUntilIdle()

        // Then
        runBlockingTestWithTimeout {
            result.receive() mustBe State.Initial
            result.receive() mustBe State.NoConnection
        }
    }

    @Test
    fun `Given a event occurs with Accepted it emits the Success`() {
        // Given
        val result = Channel<State>()

        val expectedValue = fixture.overviewItemsFixture(2)

        // When
        val viewModel = OverviewViewModel(store)

        CoroutineScope(Dispatchers.Default).launch {
            viewModel.overview.collect { items -> result.send(items) }
        }

        CoroutineScope(Dispatchers.Default).launch {
            overviewFlow.update {
                OverviewStoreState.Accepted(expectedValue)
            }
        }

        scheduler.advanceUntilIdle()

        // Then
        runBlockingTestWithTimeout {
            result.receive() mustBe State.Initial

            val success = result.receive()
            success fulfils State.Accepted::class
            success.value mustBe expectedValue
        }
    }

    @Test
    fun `Given a event occurs with Accepted it emits the NoResult if the response has no values`() {
        // Given
        val result = Channel<State>()
        val expectedValue: List<OverviewItem> = emptyList()

        // When
        val viewModel = OverviewViewModel(store)

        CoroutineScope(Dispatchers.Default).launch {
            viewModel.overview.collect { items -> result.send(items) }
        }

        CoroutineScope(Dispatchers.Default).launch {
            overviewFlow.update {
                OverviewStoreState.Accepted(expectedValue)
            }
        }

        scheduler.advanceUntilIdle()

        // Then
        runBlockingTestWithTimeout {
            result.receive() mustBe State.Initial

            val success = result.receive()
            success fulfils State.NoResult::class
        }
    }

    @Test
    fun `Given the ViewModel is initialized with a default query it runs it`() {
        // Given
        val query: String = fixture.fixture()

        // When
        OverviewViewModel(store, query)

        // Then
        assertProxy {
            store._fetchOverview.hasBeenStrictlyCalledWith(query, 1.toUShort())
        }
    }

    @Test
    fun `Given the ViewModel is initialized with a default query and nextPage is called it propagates the query with pageId to the store`() {
        // Given
        val query: String = fixture.fixture()

        // When
        val viewModel = OverviewViewModel(store, query)
        viewModel.nextPage()

        // Then
        assertProxy {
            store._fetchOverview.hasBeenStrictlyCalledWith(query, 1.toUShort())
            store._fetchOverview.hasBeenStrictlyCalledWith(query, 2.toUShort())
        }
    }

    @Test
    fun `Given a subsequent event occurs with Accepted it emits the Success`() {
        // Given
        val result = Channel<State>()
        val expectedValue1 = fixture.overviewItemsFixture(2)
        val expectedValue2 = fixture.overviewItemsFixture(2)

        // When
        val viewModel = OverviewViewModel(store)

        CoroutineScope(Dispatchers.Default).launch {
            viewModel.overview.collect { items ->
                if (items != State.Initial) {
                    result.send(items)
                }
            }
        }

        CoroutineScope(Dispatchers.Default).launch {
            overviewFlow.update {
                OverviewStoreState.Accepted(expectedValue1)
            }
        }

        scheduler.advanceUntilIdle()

        // Then
        runBlockingTestWithTimeout {
            val success = result.receive()
            success fulfils State.Accepted::class
            success.value mustBe expectedValue1
        }

        // When
        viewModel.nextPage()

        CoroutineScope(Dispatchers.Default).launch {
            overviewFlow.update {
                OverviewStoreState.Accepted(expectedValue2)
            }
        }

        scheduler.advanceUntilIdle()

        // Then
        runBlockingTestWithTimeout {
            val success = result.receive()
            success fulfils State.Accepted::class
            success.value mustBe listOf(expectedValue1, expectedValue2).flatten()
        }
    }

    @Test
    fun `Given a subsequent event occurs with Accepted which has no values it emits the only the values of the current state`() {
        // Given
        val result = Channel<State>()

        val expectedValue1 = fixture.overviewItemsFixture(2)
        val expectedValue2: List<OverviewItem> = emptyList()

        // When
        val viewModel = OverviewViewModel(store)
        scheduler.runCurrent()

        CoroutineScope(Dispatchers.Default).launch {
            viewModel.overview.collect { items ->
                if (items != State.Initial) {
                    result.send(items)
                }
            }
        }

        CoroutineScope(Dispatchers.Default).launch {
            overviewFlow.update {
                OverviewStoreState.Accepted(expectedValue1)
            }
        }

        // Then
        runBlockingTestWithTimeout {
            val success = result.receive()
            success fulfils State.Accepted::class
            success.value mustBe expectedValue1
        }

        // When
        viewModel.nextPage()

        CoroutineScope(Dispatchers.Default).launch {
            overviewFlow.update {
                OverviewStoreState.Accepted(expectedValue2)
            }
        }

        // Then
        runBlockingTestWithTimeout {
            val success = result.receive()
            success fulfils State.Accepted::class
            success.value mustBe listOf(expectedValue1, expectedValue2).flatten()
        }
    }

    @Test
    fun `Given a subsequent event occurs with Accepted which is a new search it emits only the new items`() {
        // Given
        val result = Channel<State>()
        val expectedValue1 = fixture.overviewItemsFixture(2)
        val expectedValue2 = fixture.overviewItemsFixture(2)

        // When
        val viewModel = OverviewViewModel(store)

        CoroutineScope(Dispatchers.Default).launch {
            viewModel.overview.collect { items ->
                if (items != State.Initial) {
                    result.send(items)
                }
            }
        }

        CoroutineScope(Dispatchers.Default).launch {
            overviewFlow.update {
                OverviewStoreState.Accepted(expectedValue1)
            }
        }

        scheduler.advanceUntilIdle()

        // Then
        runBlockingTestWithTimeout {
            val success = result.receive()
            success fulfils State.Accepted::class
            success.value mustBe expectedValue1
        }

        // When
        viewModel.search()

        CoroutineScope(Dispatchers.Default).launch {
            overviewFlow.update {
                OverviewStoreState.Accepted(expectedValue2)
            }
        }

        scheduler.advanceUntilIdle()

        // Then
        runBlockingTestWithTimeout {
            val success = result.receive()
            success fulfils State.Accepted::class
            success.value mustBe expectedValue2
        }
    }

    @Test
    fun `Given fetchImage is called with a ImageId it delegates the call to the store`() {
        // Given
        val imageId: Long = fixture.fixture()

        // When
        OverviewViewModel(store).fetchImage(imageId)

        // Then
        assertProxy {
            store._fetchDetailView.hasBeenStrictlyCalledWith(imageId)
        }
    }
}
