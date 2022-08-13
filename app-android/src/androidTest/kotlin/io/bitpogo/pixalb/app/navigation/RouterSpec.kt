/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.app.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.bitpogo.pixalb.app.AppContract
import io.bitpogo.pixalb.app.HiltTestActivity
import io.bitpogo.pixalb.app.kmock
import io.bitpogo.pixalb.app.navigation.Routing.Routing
import io.bitpogo.pixalb.app.overview.OverviewContract
import io.bitpogo.pixalb.app.theme.PixabayAlbumTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import tech.antibytes.kmock.Mock

@Mock(
    OverviewContract.ViewModel::class
)
@HiltAndroidTest
class RouterSpec {
    @get:Rule(order = 1)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 2)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    @BindValue
    val overviewViewModel: OverviewContract.ViewModel = kmock()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun It_routes_to_Overview() {
        // When
        composeTestRule.setContent {
            PixabayAlbumTheme {
                Routing(AppContract.Routes.OVERVIEW.name)
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Your query")
            .assertIsDisplayed()
    }

    @Test
    fun It_routes_to_Detailview() {
        // When
        composeTestRule.setContent {
            PixabayAlbumTheme {
                Routing(AppContract.Routes.DETAILVIEW.name)
            }
        }

        // Then
        composeTestRule
            .onNodeWithContentDescription("Back to overview")
            .assertDoesNotExist()
    }
}
