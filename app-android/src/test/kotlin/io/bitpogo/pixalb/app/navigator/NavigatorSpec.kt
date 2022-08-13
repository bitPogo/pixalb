/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.app.navigator

import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavOptions
import androidx.navigation.Navigator as AndroidNavigator
import io.bitpogo.pixalb.app.detail.DetailContract
import io.bitpogo.pixalb.app.navigation.Navigator
import io.bitpogo.pixalb.app.overview.OverviewContract
import io.bitpogo.pixalb.fixture.MockContext
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import tech.antibytes.util.test.fulfils
import tech.antibytes.util.test.mustBe

@RunWith(RobolectricTestRunner::class)
class NavigatorSpec {
    @Test
    fun `It fulfils DetailContractNavigator`() {
        Navigator(Router()) fulfils DetailContract.Navigator::class
    }

    @Test
    fun `It fulfils OverviewContractNavigator`() {
        Navigator(Router()) fulfils OverviewContract.Navigator::class
    }

    @Test
    fun `Given goToOverview is called it delegates the call to the Router`() {
        // Given
        val router = Router()

        var capturedTarget = ""

        router._navigate = { target, _, _ ->
            capturedTarget = target.uri.toString()
        }

        // When
        Navigator(router).goToOverview()

        // Then
        capturedTarget mustBe "android-app://androidx.navigation/OVERVIEW"
    }

    @Test
    fun `Given goToDetailView is called it delegates the call to the Router`() {
        // Given
        val router = Router()

        var capturedTarget = ""

        router._navigate = { target, _, _ ->
            capturedTarget = target.uri.toString()
        }

        // When
        Navigator(router).goToDetailView()

        // Then
        capturedTarget mustBe "android-app://androidx.navigation/DETAILVIEW"
    }
}

private class Router(
    var _navigate: Function3<NavDeepLinkRequest, NavOptions?, AndroidNavigator.Extras?, Unit>? = null
) : NavController(MockContext) {
    override fun navigate(
        request: NavDeepLinkRequest,
        navOptions: NavOptions?,
        navigatorExtras: AndroidNavigator.Extras?
    ) {
        return _navigate?.invoke(request, navOptions, navigatorExtras)
            ?: throw RuntimeException("Missing SideEffect _navigate")
    }
}
