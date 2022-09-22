/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.app.overview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.bitpogo.pixalb.app.overview.SearchBar.SearchBar
import io.bitpogo.pixalb.app.theme.PixabayAlbumTheme

@Preview
@Composable
fun SearchBarPreview() {
    PixabayAlbumTheme {
        SearchBar(
            "A Query",
            {},
            {},
        )
    }
}

@Preview
@Composable
fun SearchBarPreviewWithPlaceholder() {
    PixabayAlbumTheme {
        SearchBar(
            "",
            {},
            {},
        )
    }
}
