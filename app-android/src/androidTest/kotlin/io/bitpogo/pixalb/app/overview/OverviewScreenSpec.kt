/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.app.overview

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onParent
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextInput
import io.bitpogo.pixalb.app.kmock
import io.bitpogo.pixalb.app.overview.OverviewScreen.OverviewScreen
import io.bitpogo.pixalb.app.theme.PixabayAlbumTheme
import io.bitpogo.pixalb.fixture.overviewItemsFixture
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import tech.antibytes.kfixture.fixture
import tech.antibytes.kfixture.kotlinFixture
import tech.antibytes.kmock.Mock
import tech.antibytes.kmock.verification.assertProxy
import tech.antibytes.kmock.verification.verify

@Mock(
    OverviewContract.ViewModel::class,
    OverviewContract.Navigator::class,
)
class OverviewScreenSpec {
    @get:Rule
    val composeTestRule = createComposeRule()
    private val fixture = kotlinFixture()

    private val query: MutableStateFlow<String> = MutableStateFlow("")
    private val overview: MutableStateFlow<OverviewContract.State> = MutableStateFlow(OverviewContract.State.Initial)

    private val viewModel: ViewModelMock = kmock(relaxUnitFun = true)
    private val navigator: NavigatorMock = kmock(relaxUnitFun = true)

    @Before
    fun setup() {
        viewModel._clearMock()
        navigator._clearMock()

        query.update { "" }
        overview.update { OverviewContract.State.Initial }

        viewModel._overview returns overview
        viewModel._query returns query
    }

    @Test
    fun It_renders_the_OverviewScreen_without_result() {
        // Given
        overview.value = OverviewContract.State.NoResult

        // When
        composeTestRule.setContent {
            PixabayAlbumTheme {
                OverviewScreen(
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
    fun It_renders_the_OverviewScreen_without_Connection() {
        // Given
        overview.value = OverviewContract.State.NoConnection

        // When
        composeTestRule.setContent {
            PixabayAlbumTheme {
                OverviewScreen(
                    viewModel,
                    navigator,
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("It seems you are unconnected!")
            .assertIsDisplayed()
    }

    @Test
    fun It_renders_the_OverviewScreen_with_items() {
        // Given
        val items = fixture.overviewItemsFixture(50)
        overview.value = OverviewContract.State.Accepted(items)

        // When
        composeTestRule.setContent {
            PixabayAlbumTheme {
                OverviewScreen(
                    viewModel,
                    navigator,
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText(items.first().userName, substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun It_propagates_queries_to_the_viewmodel() {
        // Given
        val query: String = fixture.fixture()

        this.query.value = ""

        // When
        composeTestRule.setContent {
            PixabayAlbumTheme {
                OverviewScreen(
                    viewModel,
                    navigator,
                )
            }
        }

        composeTestRule
            .onNodeWithText("Your query")
            .performTextInput(query)

        // Then
        verify(exactly = 1) {
            viewModel._setQuery.hasBeenCalledWith(query)
        }
    }

    @Test
    fun It_propagates_search_events_to_the_viewmodel() {
        // Given
        this.query.value = ""

        // When
        composeTestRule.setContent {
            PixabayAlbumTheme {
                OverviewScreen(
                    viewModel,
                    navigator,
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Run the query")
            .performClick()

        // Then
        verify(exactly = 1) {
            viewModel._search.hasBeenCalled()
        }
    }

    @Test
    fun It_requests_new_items() {
        // Given
        val items = fixture.overviewItemsFixture(50)
        overview.value = OverviewContract.State.Accepted(items)

        // When
        composeTestRule.setContent {
            PixabayAlbumTheme {
                OverviewScreen(
                    viewModel,
                    navigator,
                )
            }
        }

        composeTestRule
            .onNodeWithText(items.first().userName, substring = true)
            .onParent()
            .performScrollToIndex(items.lastIndex)

        // Then
        verify(exactly = 1) {
            viewModel._nextPage.hasBeenCalled()
        }
    }

    @Test
    fun It_open_and_closes_the_detail_diallog() {
        // Given
        val items = fixture.overviewItemsFixture(50)
        overview.value = OverviewContract.State.Accepted(items)

        // When
        composeTestRule.setContent {
            PixabayAlbumTheme {
                OverviewScreen(
                    viewModel,
                    navigator,
                )
            }
        }

        composeTestRule
            .onNodeWithText(items.first().userName, substring = true)
            .performClick()

        // Then
        composeTestRule
            .onNodeWithText("You want to see details?")
            .assertIsDisplayed()

        // When
        composeTestRule
            .onNodeWithText("No thanks")
            .performClick()

        // Then
        composeTestRule
            .onNodeWithText("You want to see details?")
            .assertDoesNotExist()

        composeTestRule
            .onNodeWithText(items.first().userName, substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun It_open_the_detail_diallog_and_proagates_to_the_detailview() {
        // Given
        val items = fixture.overviewItemsFixture(50)
        overview.value = OverviewContract.State.Accepted(items)

        // When
        composeTestRule.setContent {
            PixabayAlbumTheme {
                OverviewScreen(
                    viewModel,
                    navigator,
                )
            }
        }

        composeTestRule
            .onNodeWithText(items.first().userName, substring = true)
            .performClick()

        // Then
        composeTestRule
            .onNodeWithText("You want to see details?")
            .assertIsDisplayed()

        // When
        composeTestRule
            .onNodeWithText("Show me")
            .performClick()

        // Then
        composeTestRule
            .onNodeWithText("You want to see details?")
            .assertDoesNotExist()

        composeTestRule
            .onNodeWithText(items.first().userName, substring = true)
            .assertIsDisplayed()

        assertProxy {
            navigator._goToDetailView.hasBeenCalledWithVoid()
        }
    }
}
