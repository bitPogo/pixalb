/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.app.detail

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.bitpogo.pixalb.album.AlbumContract.DetailviewStoreState
import io.bitpogo.pixalb.album.AlbumContract.Store
import io.bitpogo.pixalb.app.detail.DetailContract.State
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class DetailviewViewModel @Inject constructor(
    private val store: Store,
) : DetailContract.ViewModel, ViewModel() {
    private val _image: MutableStateFlow<State> = MutableStateFlow(State.NoResult)
    override val details: StateFlow<State> = _image

    init {
        store.detailview.subscribe { state -> evaluateState(state) }
    }

    private fun evaluateState(state: DetailviewStoreState) {
        if (state is DetailviewStoreState.Accepted) {
            _image.update { State.Accepted(state.value) }
        } else {
            _image.update { State.NoResult }
        }
    }
}
