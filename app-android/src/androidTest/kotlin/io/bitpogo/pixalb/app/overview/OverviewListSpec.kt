/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.app.overview

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import io.bitpogo.pixalb.app.overview.OverviewList.OverviewList
import io.bitpogo.pixalb.app.theme.PixabayAlbumTheme
import io.bitpogo.pixalb.fixture.overviewItemsFixture
import org.junit.Rule
import org.junit.Test
import tech.antibytes.kfixture.kotlinFixture
import tech.antibytes.util.test.mustBe

class OverviewListSpec {
    @get:Rule
    val composeTestRule = createComposeRule()
    private val fixture = kotlinFixture()

    @Test
    fun It_propagates_the_correct_item() {
        // Given
        val items = fixture.overviewItemsFixture(7)
        val expectedIdx = 4

        var capturedClick: Long? = null
        val onClick: Function1<Long, Unit> = { id -> capturedClick = id }

        // When
        composeTestRule.setContent {
            PixabayAlbumTheme {
                OverviewList(
                    items = items,
                    onClick = onClick,
                ) { }
            }
        }

        var idx = -3
        composeTestRule
            .onNode(
                SemanticsMatcher("Button Click") { node ->
                    idx++

                    node.config.any { item ->
                        item.value == Role.Button && idx == expectedIdx
                    }
                },
            )
            .performClick()

        // Then
        capturedClick mustBe items[expectedIdx].id
    }

    @Test
    fun It_requests_new_items() {
        // Given
        val items = fixture.overviewItemsFixture(50)

        var capturedCall = false
        val onEnd: Function0<Unit> = { capturedCall = true }

        // When
        composeTestRule.setContent {
            PixabayAlbumTheme {
                OverviewList(
                    items = items,
                    onClick = { },
                    loadNextItems = onEnd,
                )
            }
        }

        var idx = 0
        composeTestRule
            .onNode(
                SemanticsMatcher("Scroll Port") {
                    idx++
                    idx == 2
                },
                useUnmergedTree = true,
            )
            .performScrollToIndex(items.lastIndex)

        // Then
        capturedCall mustBe true
    }

    @Test
    fun It_requests_new_items_for_the_multiple_of_50() {
        // Given
        val items = fixture.overviewItemsFixture(250)

        var capturedCall = false
        val onEnd: Function0<Unit> = { capturedCall = true }

        // When
        composeTestRule.setContent {
            PixabayAlbumTheme {
                OverviewList(
                    items = items,
                    onClick = { },
                    loadNextItems = onEnd,
                )
            }
        }

        var idx = 0
        composeTestRule
            .onNode(
                SemanticsMatcher("Scroll Port") {
                    idx++
                    idx == 2
                },
                useUnmergedTree = true,
            )
            .performScrollToIndex(items.lastIndex)

        // Then
        capturedCall mustBe true
    }

    @Test
    fun It_requests_not_new_items_for_no_multiple_of_50() {
        // Given
        val items = fixture.overviewItemsFixture(253)

        var capturedCall = false
        val onEnd: Function0<Unit> = { capturedCall = true }

        // When
        composeTestRule.setContent {
            PixabayAlbumTheme {
                OverviewList(
                    items = items,
                    onClick = { },
                    loadNextItems = onEnd,
                )
            }
        }

        var idx = 0
        composeTestRule
            .onNode(
                SemanticsMatcher("Scroll Port") {
                    idx++
                    idx == 2
                },
                useUnmergedTree = true,
            )
            .performScrollToIndex(items.lastIndex)

        // Then
        capturedCall mustBe false
    }
}
