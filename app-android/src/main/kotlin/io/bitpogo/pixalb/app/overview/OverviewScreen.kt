/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.app.overview

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.bitpogo.pixalb.app.components.molecule.NoConnection.NoConnection
import io.bitpogo.pixalb.app.components.molecule.NoResult.NoResult
import io.bitpogo.pixalb.app.overview.OverviewContract.State
import io.bitpogo.pixalb.app.overview.OverviewDialog.OverviewDialog
import io.bitpogo.pixalb.app.overview.OverviewList.OverviewList
import io.bitpogo.pixalb.app.overview.SearchBar.SearchBar

object OverviewScreen {
    @Composable
    private fun TopBar(
        query: String,
        onSearch: Function0<Unit>,
        onValueChange: Function1<String, Unit>,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp),
        ) {
            SearchBar(
                value = query,
                onValueChange = onValueChange,
                onSearch = onSearch,
            )
        }
    }

    @Composable
    private fun SelectContent(
        contentState: State,
        nextPage: Function0<Unit>,
        onClick: Function1<Long, Unit>,
    ) {
        when (contentState) {
            is State.Initial -> NoResult()
            is State.NoConnection -> NoConnection()
            is State.NoResult -> NoResult()
            is State.Accepted -> {
                OverviewList(
                    items = contentState.value,
                    onClick = onClick,
                    loadNextItems = nextPage,
                )
            }
        }
    }

    @Composable
    private fun SetOverviewDialog(
        selectImage: Function1<Long, Unit>,
        goTo: MutableState<Long?>,
        navigator: OverviewContract.Navigator,
    ) {
        if (goTo.value != null) {
            OverviewDialog(
                onDismiss = { goTo.value = null },
                onAccept = {
                    selectImage(goTo.value!!)
                    goTo.value = null
                    navigator.goToDetailView()
                },
            )
        }
    }

    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    @Composable
    fun OverviewScreen(
        overviewViewModel: OverviewContract.ViewModel = hiltViewModel<OverviewViewModel>(),
        navigator: OverviewContract.Navigator,
    ) {
        val goTo: MutableState<Long?> = remember { mutableStateOf(null) }
        val query = overviewViewModel.query.collectAsState()
        val overview = overviewViewModel.overview.collectAsState()

        val searchBar = @Composable {
            TopBar(
                query = query.value,
                onSearch = overviewViewModel::search,
                onValueChange = overviewViewModel::setQuery,
            )
        }

        Scaffold(topBar = searchBar) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            ) {
                SelectContent(
                    contentState = overview.value,
                    nextPage = {
                        overviewViewModel.nextPage()
                    },
                ) { imageId ->
                    goTo.value = imageId
                }
            }
        }

        SetOverviewDialog(
            overviewViewModel::fetchImage,
            goTo,
            navigator,
        )
    }
}
