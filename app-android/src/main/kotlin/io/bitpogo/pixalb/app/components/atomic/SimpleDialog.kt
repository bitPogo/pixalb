/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.app.components.atomic

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

object SimpleDialog {
    @Composable
    fun SimpleDialog(
        onDismissRequest: Function0<Unit>,
        content: @Composable Function0<Unit>,
    ) {
        Dialog(onDismissRequest = onDismissRequest) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
            ) {
                Box(
                    modifier = Modifier
                        .width(280.dp)
                        .defaultMinSize(minHeight = 110.dp)
                        .padding(start = 28.dp, top = 8.dp, end = 28.dp, bottom = 8.dp),
                ) {
                    content()
                }
            }
        }
    }
}
