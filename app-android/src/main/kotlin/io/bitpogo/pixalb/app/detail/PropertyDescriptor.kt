/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.app.detail

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

object PropertyDescriptor {
    @Composable
    fun PropertyDescriptor(
        fieldName: String,
        value: String
    ) {
        Row {
            Text("$fieldName: ")
            Text(value)
        }
    }
}
