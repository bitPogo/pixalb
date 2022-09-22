/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.app.navigation

import androidx.navigation.NavController
import io.bitpogo.pixalb.app.AppContract
import io.bitpogo.pixalb.app.detail.DetailContract
import io.bitpogo.pixalb.app.overview.OverviewContract

class Navigator(
    private val router: NavController,
) : DetailContract.Navigator, OverviewContract.Navigator {
    override fun goToOverview() = router.navigate(AppContract.Routes.OVERVIEW.name)

    override fun goToDetailView() = router.navigate(AppContract.Routes.DETAILVIEW.name)
}
