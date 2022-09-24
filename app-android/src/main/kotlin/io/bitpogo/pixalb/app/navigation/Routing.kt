/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.bitpogo.pixalb.app.AppContract
import io.bitpogo.pixalb.app.detail.DetailScreen.DetailScreen
import io.bitpogo.pixalb.app.overview.OverviewScreen.OverviewScreen

object Routing {
    @Composable
    fun Routing(
        start: String = AppContract.Routes.OVERVIEW.name,
    ) {
        val controller = rememberNavController()
        val router by remember { mutableStateOf(Navigator(controller)) }

        NavHost(navController = controller, startDestination = AppContract.Routes.OVERVIEW.name) {
            composable(route = AppContract.Routes.OVERVIEW.name) {
                OverviewScreen(navigator = router)
            }

            composable(route = AppContract.Routes.DETAILVIEW.name) {
                DetailScreen(navigator = router)
            }
        }
    }
}
