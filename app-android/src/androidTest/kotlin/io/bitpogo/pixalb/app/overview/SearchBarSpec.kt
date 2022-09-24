/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.app.overview

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import io.bitpogo.pixalb.app.overview.SearchBar.SearchBar
import io.bitpogo.pixalb.app.theme.PixabayAlbumTheme
import org.junit.Rule
import org.junit.Test
import tech.antibytes.kfixture.fixture
import tech.antibytes.kfixture.kotlinFixture
import tech.antibytes.util.test.mustBe

class SearchBarSpec {
    @get:Rule
    val composeTestRule = createComposeRule()
    private val fixture = kotlinFixture()

    @Test
    fun It_delegates_value_changes_to_the_given_lambda() {
        // Given
        val oldValue: String = fixture.fixture()
        val newValue: String = fixture.fixture()

        var capturedValue: String? = null
        val onValueChange = { givenValue: String ->
            capturedValue = givenValue
        }
        // When
        composeTestRule.setContent {
            PixabayAlbumTheme {
                SearchBar(
                    value = oldValue,
                    onValueChange = onValueChange,
                    onSearch = {},
                )
            }
        }

        composeTestRule
            .onNodeWithText(oldValue)
            .performTextReplacement(newValue)

        // Then
        capturedValue mustBe newValue
    }

    @Test
    fun Given_the_search_button_is_clicked_it_calls_the_given_lambda() {
        // Given
        var wasCalled = false
        val onSearch = {
            wasCalled = true
        }
        // When
        composeTestRule.setContent {
            PixabayAlbumTheme {
                SearchBar(
                    value = fixture.fixture(),
                    onValueChange = {},
                    onSearch = onSearch,
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Run the query")
            .performClick()

        // Then
        wasCalled mustBe true
    }
}
