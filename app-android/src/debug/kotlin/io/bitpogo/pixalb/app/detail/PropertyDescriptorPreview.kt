/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.app.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.bitpogo.pixalb.app.detail.PropertyDescriptor.PropertyDescriptor

@Preview
@Composable
fun PropertyDescriptorPreview() {
    PropertyDescriptor(
        "Test",
        "Value",
    )
}
