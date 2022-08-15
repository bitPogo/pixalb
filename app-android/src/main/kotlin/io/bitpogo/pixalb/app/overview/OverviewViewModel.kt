/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.app.overview

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.bitpogo.pixalb.album.AlbumContract
import io.bitpogo.pixalb.album.AlbumContract.OverviewStoreState
import io.bitpogo.pixalb.album.domain.error.PixabayError
import io.bitpogo.pixalb.album.domain.model.OverviewItem
import io.bitpogo.pixalb.app.overview.OverviewContract.CHAR_CAP
import io.bitpogo.pixalb.app.overview.OverviewContract.MAX_PAGES
import io.bitpogo.pixalb.app.overview.OverviewContract.START_PAGE
import io.bitpogo.pixalb.app.overview.OverviewContract.State
import javax.inject.Inject
import kotlin.math.min
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class OverviewViewModel @Inject constructor(
    private val store: AlbumContract.Store,
    initialQuery: String = ""
) : OverviewContract.ViewModel, ViewModel() {
    private val _query: MutableStateFlow<String> = MutableStateFlow("")
    override val query: StateFlow<String> = _query

    private var merge = false

    private var bufferedQuery: String = ""
    private var page: UShort = START_PAGE

    private val _overview: MutableStateFlow<State> = MutableStateFlow(State.Initial)
    override val overview: StateFlow<State> = _overview

    init {
        store.overview.subscribe { result -> evaluateSearchResult(result) }
        runDefaultQuery(initialQuery)
    }

    private fun runDefaultQuery(query: String) {
        if (query.isNotEmpty()) {
            bufferedQuery = query
            store.fetchOverview(query, page)
        }
    }

    private fun evaluateError(error: OverviewStoreState.Error) {
        if (error.value is PixabayError.NoConnection) {
            _overview.update { State.NoConnection }
        }
    }

    private fun updateOverview(newValue: List<OverviewItem>) {
        val value = if (!merge) {
            newValue
        } else {
            listOf(_overview.value.value, newValue).flatten()
        }

        _overview.update { State.Accepted(value) }
    }

    private fun propagateResponse(success: OverviewStoreState.Accepted) {
        if (success.value.isEmpty() && !merge) {
            _overview.update { State.NoResult }
        } else {
            updateOverview(success.value)
        }
    }

    private fun evaluateSearchResult(state: OverviewStoreState) {
        when (state) {
            is OverviewStoreState.Accepted -> propagateResponse(state)
            is OverviewStoreState.Error -> evaluateError(state)
            else -> { /* Do nothing */ }
        }
    }

    override fun setQuery(query: String) {
        if (query.length <= CHAR_CAP) {
            _query.update { query }
        }
    }

    private fun prepareSearch() {
        page = START_PAGE
        bufferedQuery = _query.value
        merge = false
    }

    private fun runSearch() {
        prepareSearch()
        store.fetchOverview(_query.value, page)
    }

    override fun search() {
        if (_query.value.isNotEmpty()) {
            runSearch()
        }
    }

    private fun resolveNextPage() {
        page = min(page + 1u, MAX_PAGES).toUShort()
    }

    override fun nextPage() {
        merge = true
        resolveNextPage()
        store.fetchOverview(bufferedQuery, page)
    }

    override fun fetchImage(imageId: Long) = store.fetchDetailView(imageId)
}
