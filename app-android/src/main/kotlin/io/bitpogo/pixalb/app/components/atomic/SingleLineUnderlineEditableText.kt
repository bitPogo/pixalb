/* ktlint-disable filename */
/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.app.components.atomic

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

object SingleLineOutlineEditableText {
    enum class BlockComponent {
        PRIMARY_PROGRESSIVE,
        PRIMARY_DESTRUCTIVE
    }

    @Composable
    fun SingleLineOutlineEditableText(
        label: String,
        value: String,
        onChange: Function1<String, Unit>,
        blockComponent: BlockComponent = BlockComponent.PRIMARY_PROGRESSIVE,
        keyboardOptions: KeyboardOptions = KeyboardOptions.Default
    ) {
        val labelField = @Composable { Text(text = label) }

        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            label = labelField,
            placeholder = labelField,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = blockComponent == BlockComponent.PRIMARY_DESTRUCTIVE,
            keyboardOptions = keyboardOptions
        )
    }
}
