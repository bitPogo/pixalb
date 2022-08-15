/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.app.detail

import io.bitpogo.pixalb.album.domain.model.DetailViewItem
import kotlinx.coroutines.flow.StateFlow

object DetailContract {
    sealed class State {
        object NoResult : State()
        class Accepted(val value: DetailViewItem) : State()
    }

    interface ViewModel {
        val details: StateFlow<State>
    }

    interface Navigator {
        fun goToOverview()
    }
}
