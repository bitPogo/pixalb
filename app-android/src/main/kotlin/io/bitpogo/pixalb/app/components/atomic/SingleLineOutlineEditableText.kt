/* ktlint-disable filename */
/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.app.components.atomic

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

object SingleLineUnderlineEditableText {
    enum class BlockComponent {
        PRIMARY_PROGRESSIVE,
        PRIMARY_DESTRUCTIVE
    }

    @Composable
    fun SingleLineUnderlineEditableText(
        label: String,
        value: String,
        onChange: Function1<String, Unit>,
        blockComponent: BlockComponent = BlockComponent.PRIMARY_PROGRESSIVE,
        @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
        keyboardOptions: KeyboardOptions = KeyboardOptions.Default
    ) {
        val labelField = @Composable { Text(text = label) }

        TextField(
            value = value,
            onValueChange = onChange,
            label = labelField,
            placeholder = labelField,
            singleLine = true,
            isError = blockComponent == BlockComponent.PRIMARY_DESTRUCTIVE,
            modifier = modifier.fillMaxWidth(),
            keyboardOptions = keyboardOptions
        )
    }
}
