/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.app.components.atomic

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CarCrash
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import io.bitpogo.pixalb.app.components.atomic.BorderLessIconButton.BorderLessIconButton
import io.bitpogo.pixalb.app.theme.PixabayAlbumTheme
import org.junit.Rule
import org.junit.Test
import tech.antibytes.util.test.mustBe

class BorderlessButtonSpec {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun Given_the_button_is_clicked_it_propagtes_the_click_event() {
        // Given
        var wasClick = false

        // When
        composeTestRule.setContent {
            PixabayAlbumTheme {
                BorderLessIconButton(
                    icon = Icons.Default.CarCrash,
                    contentDescription = "Test"
                ) { wasClick = true }
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Test")
            .performClick()

        // Then
        wasClick mustBe true
    }
}
