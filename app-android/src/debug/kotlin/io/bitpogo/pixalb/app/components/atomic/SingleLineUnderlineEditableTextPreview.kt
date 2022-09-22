/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.app.components.atomic

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.bitpogo.pixalb.app.components.atomic.SingleLineUnderlineEditableText.SingleLineUnderlineEditableText

@Preview
@Composable
fun SingleLineUnderlineEditableTextWithoutError() {
    SingleLineUnderlineEditableText(
        label = "test",
        value = "myTest",
        {},
    )
}

@Preview
@Composable
fun DefaultSingleLineUnderlineEditableTextWithError() {
    SingleLineUnderlineEditableText(
        label = "test",
        value = "myTest",
        {},
        blockComponent = SingleLineUnderlineEditableText.BlockComponent.PRIMARY_DESTRUCTIVE,
    )
}
