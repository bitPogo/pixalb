/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.app.overview

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.bitpogo.pixalb.app.R
import io.bitpogo.pixalb.app.components.atomic.TopSearchBar.TopSearchBar
import io.bitpogo.pixalb.app.theme.DarkWhite
import io.bitpogo.pixalb.app.theme.DeepBlack
import io.bitpogo.pixalb.app.theme.LightBrightGray
import io.bitpogo.pixalb.app.theme.LightDarkGray

object SearchBar {
    @Composable
    fun SearchBar(
        value: String,
        onValueChange: (String) -> Unit,
        onSearch: () -> Unit
    ) {
        val colours = TextFieldDefaults.outlinedTextFieldColors(
            textColor = DeepBlack,
            disabledTextColor = LightDarkGray,
            cursorColor = DeepBlack,
            placeholderColor = LightDarkGray,
            backgroundColor = DarkWhite,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent,
            errorBorderColor = Color.Transparent,
            focusedLabelColor = Color.Transparent,
            unfocusedLabelColor = Color.Transparent,
            disabledLabelColor = Color.Transparent,
            errorLabelColor = Color.Transparent
        )

        TopSearchBar(
            value = value,
            placeholder = stringResource(R.string.overview_search_placeholder),
            onValueChange = onValueChange,
            leadingIcon = @Composable {
                IconButton(onClick = onSearch) {
                    Icon(
                        Icons.Default.Search,
                        stringResource(R.string.overview_search_run)
                    )
                }
            },
            textFieldColours = colours,
            textFieldShape = RoundedCornerShape(2.dp),
            textFieldModifier = {
                this
                    .padding(
                        start = 8.dp,
                        end = 8.dp,
                        top = 8.dp,
                        bottom = 8.dp
                    )
                    .border(
                        1.dp,
                        LightBrightGray
                    )
            }
        )
    }
}
