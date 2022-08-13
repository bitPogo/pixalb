/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.app.components.atomic

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.bitpogo.pixalb.app.components.atomic.HeroImage.HeroImage

@Preview
@Composable
fun HeroImagePreview() {
    HeroImage("https://www.pngmart.com/files/4/Android-PNG-Pic.png")
}
