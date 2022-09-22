/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.app.components.atomic

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.bitpogo.pixalb.app.components.atomic.Thumbnail.Thumbnail

@Preview
@Composable
fun ThumbnailPreview() {
    Thumbnail(
        url = "https://www.pngmart.com/files/4/Android-PNG-Pic.png",
        contentDescription = "Test Droid",
        modifier = Modifier.width(75.dp).height(75.dp),
    )
}

@Preview
@Composable
fun ThumbnailErrorPreview() {
    Thumbnail(
        url = "https://de.m.wikipedia.org/wiki/Datei:Google_%22G%22_Logo.svg",
        contentDescription = "Test Droid",
        modifier = Modifier.width(75.dp).height(75.dp),
    )
}
