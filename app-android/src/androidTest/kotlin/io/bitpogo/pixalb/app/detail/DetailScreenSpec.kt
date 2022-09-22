/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.app.detail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.bitpogo.pixalb.app.detail.DetailContract.State
import io.bitpogo.pixalb.app.detail.DetailScreen.DetailScreen
import io.bitpogo.pixalb.app.kmock
import io.bitpogo.pixalb.app.theme.PixabayAlbumTheme
import io.bitpogo.pixalb.fixture.detailviewItemFixture
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import tech.antibytes.kfixture.kotlinFixture
import tech.antibytes.kmock.Mock
import tech.antibytes.kmock.verification.assertProxy

@Mock(
    DetailContract.ViewModel::class,
    DetailContract.Navigator::class,
)
class DetailScreenSpec {
    @get:Rule
    val composeTestRule = createComposeRule()
    private val fixture = kotlinFixture()

    private val details: MutableStateFlow<State> = MutableStateFlow(State.NoResult)

    private val viewModel: ViewModelMock = kmock(relaxUnitFun = true)
    private val navigator: NavigatorMock = kmock(relaxUnitFun = true)

    @Before
    fun setup() {
        viewModel._clearMock()
        navigator._clearMock()

        viewModel._details returns details
    }

    @Test
    fun It_renders_no_results() {
        // Given
        details.update { State.NoResult }

        // When
        composeTestRule.setContent {
            PixabayAlbumTheme {
                DetailScreen(
                    viewModel,
                    navigator,
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Nothing was found!")
            .assertIsDisplayed()
    }

    @Test
    fun It_renders_details() {
        // Given
        val item = fixture.detailviewItemFixture()
        details.update { State.Accepted(item) }

        // When
        composeTestRule.setContent {
            PixabayAlbumTheme {
                DetailScreen(
                    viewModel,
                    navigator,
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText(item.downloads.toString(), substring = true)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(item.comments.toString(), substring = true)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(item.likes.toString(), substring = true)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(item.userName, substring = true)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(item.tags.joinToString(", "), substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun Given_the_cancel_button_is_clicked_it_propagates_its_result() {
        // Given
        details.update { State.NoResult }

        // When
        composeTestRule.setContent {
            PixabayAlbumTheme {
                DetailScreen(
                    viewModel,
                    navigator,
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Back to overview")
            .performClick()

        // Then
        assertProxy {
            navigator._goToOverview.hasBeenCalled()
        }
    }
}
