/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.app.components.atomic

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import io.bitpogo.pixalb.app.R
import io.bitpogo.pixalb.app.components.atomic.IconMessage.IconMessage

@Preview
@Composable
fun IconMessagePreview() {
    IconMessage(
        icon = painterResource(id = R.drawable.ic_100tb),
        message = "Test"
    )
}
