/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.app.overview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.bitpogo.pixalb.album.domain.model.OverviewItem
import io.bitpogo.pixalb.app.overview.OverviewScreen.OverviewScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Preview
@Composable
fun OverviewScreenWithItemsPreview() {
    val viewModel = object : OverviewContract.ViewModel {
        override val query: StateFlow<String> = MutableStateFlow("")
        override val overview: StateFlow<OverviewContract.State> = MutableStateFlow(
            OverviewContract.State.Accepted(
                listOf(
                    OverviewItem(
                        23,
                        "https://www.pngmart.com/files/4/Android-PNG-Pic.png",
                        "me",
                        listOf("test", "me")
                    )
                )
            )
        )

        override fun setQuery(query: String) {
            TODO("Not yet implemented")
        }

        override fun search() {
            TODO("Not yet implemented")
        }

        override fun nextPage() {
            TODO("Not yet implemented")
        }

        override fun fetchImage(imageId: Long) {
            TODO("Not yet implemented")
        }
    }

    val navigator = object : OverviewContract.Navigator {
        override fun goToDetailView() {
            TODO("Not yet implemented")
        }
    }

    OverviewScreen(viewModel, navigator)
}

@Preview
@Composable
fun OverviewScreenWithNoResultPreview() {
    val viewModel = object : OverviewContract.ViewModel {
        override val query: StateFlow<String> = MutableStateFlow("")
        override val overview: StateFlow<OverviewContract.State> = MutableStateFlow(
            OverviewContract.State.NoResult
        )

        override fun setQuery(query: String) {
            TODO("Not yet implemented")
        }

        override fun search() {
            TODO("Not yet implemented")
        }

        override fun nextPage() {
            TODO("Not yet implemented")
        }

        override fun fetchImage(imageId: Long) {
            TODO("Not yet implemented")
        }
    }

    val navigator = object : OverviewContract.Navigator {
        override fun goToDetailView() {
            TODO("Not yet implemented")
        }
    }

    OverviewScreen(viewModel, navigator)
}

@Preview
@Composable
fun OverviewScreenWithNoConnectionPreview() {
    val viewModel = object : OverviewContract.ViewModel {
        override val query: StateFlow<String> = MutableStateFlow("")
        override val overview: StateFlow<OverviewContract.State> = MutableStateFlow(
            OverviewContract.State.NoConnection
        )

        override fun setQuery(query: String) {
            TODO("Not yet implemented")
        }

        override fun search() {
            TODO("Not yet implemented")
        }

        override fun nextPage() {
            TODO("Not yet implemented")
        }

        override fun fetchImage(imageId: Long) {
            TODO("Not yet implemented")
        }
    }

    val navigator = object : OverviewContract.Navigator {
        override fun goToDetailView() {
            TODO("Not yet implemented")
        }
    }

    OverviewScreen(viewModel, navigator)
}
