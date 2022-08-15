/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.app.overview

import io.bitpogo.pixalb.album.domain.model.OverviewItem
import kotlinx.coroutines.flow.StateFlow

object OverviewContract {
    sealed class State(val value: List<OverviewItem>) {
        object Initial : State(emptyList())
        object NoConnection : State(emptyList())
        object NoResult : State(emptyList())
        class Accepted(value: List<OverviewItem>) : State(value)
    }

    interface ViewModel {
        val query: StateFlow<String>
        val overview: StateFlow<State>

        fun setQuery(query: String)
        fun search()
        fun nextPage()
        fun fetchImage(imageId: Long)
    }

    interface Navigator {
        fun goToDetailView()
    }

    const val COMPLETE_ITEM_LIST = 50
    const val CHAR_CAP = 100
    const val MAX_PAGES = 10u
    const val START_PAGE: UShort = 1u
}
