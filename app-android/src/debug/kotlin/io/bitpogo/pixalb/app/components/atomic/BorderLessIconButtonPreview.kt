/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.app.components.atomic

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CarCrash
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.bitpogo.pixalb.app.components.atomic.BorderLessIconButton.BorderLessIconButton

@Preview
@Composable
fun BorderLessIconButtonPreview() {
    BorderLessIconButton(
        icon = Icons.Filled.CarCrash,
        contentDescription = "Car Crash",
    ) { }
}
