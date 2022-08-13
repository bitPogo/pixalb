/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.app.components.atomic

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.bitpogo.pixalb.app.R

object UserIndicator {
    @Composable
    fun UserIndicator(value: String) {
        Row {
            Text("${stringResource(R.string.username_indicator)} $value")
        }
    }
}
