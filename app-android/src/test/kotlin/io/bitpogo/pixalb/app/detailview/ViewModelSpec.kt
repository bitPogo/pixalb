/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.app.detailview

import androidx.lifecycle.ViewModel
import io.bitpogo.pixalb.album.AlbumContract
import io.bitpogo.pixalb.album.AlbumContract.DetailviewStoreState
import io.bitpogo.pixalb.album.StoreMock
import io.bitpogo.pixalb.album.domain.error.PixabayError
import io.bitpogo.pixalb.app.detail.DetailContract
import io.bitpogo.pixalb.app.detail.DetailContract.State
import io.bitpogo.pixalb.app.detail.DetailviewViewModel
import io.bitpogo.pixalb.app.kmock
import io.bitpogo.pixalb.fixture.detailviewItemFixture
import io.bitpogo.util.coroutine.wrapper.SharedFlowWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.junit.Before
import org.junit.Test
import tech.antibytes.kfixture.kotlinFixture
import tech.antibytes.kmock.Mock
import tech.antibytes.util.test.coroutine.runBlockingTestWithTimeout
import tech.antibytes.util.test.fulfils
import tech.antibytes.util.test.sameAs

@Mock(
    AlbumContract.Store::class
)
class ViewModelSpec {
    private val fixture = kotlinFixture()
    private val store: StoreMock = kmock(relaxUnitFun = true)

    private val details: MutableStateFlow<DetailviewStoreState> = MutableStateFlow(DetailviewStoreState.Initial)
    private val detailsState = SharedFlowWrapper.getInstance(details) { CoroutineScope(Dispatchers.Default) }

    @Before
    fun setup() {
        details.value = DetailviewStoreState.Initial

        store._clearMock()

        store._detailview returns detailsState
    }

    @Test
    fun `It fulfils ViewModel`() {
        DetailviewViewModel(store) fulfils ViewModel::class
    }

    @Test
    fun `It fulfils DetailviewViewModel`() {
        DetailviewViewModel(store) fulfils DetailContract.ViewModel::class
    }

    @Test
    fun `Given the store is in its initial it emits no result`() {
        // Given
        details.update { DetailviewStoreState.Initial }
        val result = Channel<State>()

        // When
        val viewModel = DetailviewViewModel(store)

        CoroutineScope(Dispatchers.Default).launch {
            viewModel.details.collect { state -> result.send(state) }
        }

        // Then
        runBlockingTestWithTimeout {
            result.receive() sameAs State.NoResult
        }
    }

    @Test
    fun `Given the store is in Pending it emits no result`() {
        // Given
        details.update { DetailviewStoreState.Pending }
        val result = Channel<State>()

        // When
        val viewModel = DetailviewViewModel(store)

        CoroutineScope(Dispatchers.Default).launch {
            viewModel.details.collect { state -> result.send(state) }
        }

        // Then
        runBlockingTestWithTimeout {
            result.receive() sameAs State.NoResult
        }
    }

    @Test
    fun `Given the store is in Error it emits no result`() {
        // Given
        details.update { DetailviewStoreState.Error(PixabayError.NoConnection) }
        val result = Channel<State>()

        // When
        val viewModel = DetailviewViewModel(store)

        CoroutineScope(Dispatchers.Default).launch {
            viewModel.details.collect { state -> result.send(state) }
        }

        // Then
        runBlockingTestWithTimeout {
            result.receive() sameAs State.NoResult
        }
    }

    @Test
    fun `Given the store is in Accept it emits Accepted`() {
        // Given
        val item = fixture.detailviewItemFixture()

        details.update { DetailviewStoreState.Accepted(item) }
        val result = Channel<State>()

        // When
        val viewModel = DetailviewViewModel(store)

        CoroutineScope(Dispatchers.Default).launch {
            viewModel.details.collect { state -> result.send(state) }
        }

        // Then
        runBlockingTestWithTimeout {
            val accepted = result.receive()
            accepted fulfils State.Accepted::class
            (accepted as State.Accepted).value sameAs item
        }
    }
}
