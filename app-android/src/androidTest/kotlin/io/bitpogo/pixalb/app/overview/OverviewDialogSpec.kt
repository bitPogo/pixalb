/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.app.overview

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.bitpogo.pixalb.app.overview.OverviewDialog.OverviewDialog
import io.bitpogo.pixalb.app.theme.PixabayAlbumTheme
import org.junit.Rule
import org.junit.Test
import tech.antibytes.util.test.mustBe

class OverviewDialogSpec {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun Given_dismiss_is_clicked_it_propagates_it() {
        // Given
        var dismissed = false
        var accepted = false

        // When
        composeTestRule.setContent {
            PixabayAlbumTheme {
                OverviewDialog(
                    onAccept = { accepted = true },
                    onDismiss = { dismissed = true },
                )
            }
        }

        composeTestRule
            .onNodeWithText("No thanks")
            .performClick()

        // Then
        dismissed mustBe true
        accepted mustBe false
    }

    @Test
    fun Given_accept_is_clicked_it_propagates_it() {
        // Given
        var dismissed = false
        var accepted = false

        // When
        composeTestRule.setContent {
            PixabayAlbumTheme {
                OverviewDialog(
                    onAccept = { accepted = true },
                    onDismiss = { dismissed = true },
                )
            }
        }

        composeTestRule
            .onNodeWithText("Show me")
            .performClick()

        // Then
        dismissed mustBe false
        accepted mustBe true
    }

    @Test
    fun Given_somewhere_is_clicked_nothing_happen() {
        // Given
        var dismissed = false
        var accepted = false

        // When
        composeTestRule.setContent {
            PixabayAlbumTheme {
                OverviewDialog(
                    onAccept = { accepted = true },
                    onDismiss = { dismissed = true },
                )
            }
        }

        composeTestRule
            .onNodeWithText("You want to see details?")
            .performClick()

        // Then
        dismissed mustBe false
        accepted mustBe false
    }
}
