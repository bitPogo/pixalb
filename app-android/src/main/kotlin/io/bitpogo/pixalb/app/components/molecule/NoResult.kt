/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.app.components.molecule

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import io.bitpogo.pixalb.app.R
import io.bitpogo.pixalb.app.components.atomic.IconMessage.IconMessage

object NoResult {
    @Composable
    fun NoResult() {
        IconMessage(
            icon = painterResource(id = R.mipmap.noresult_foreground),
            message = stringResource(id = R.string.no_result)
        )
    }
}
