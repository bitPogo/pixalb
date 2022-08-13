/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.app.overview

import androidx.compose.foundation.layout.width
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.bitpogo.pixalb.app.R

object OverviewDialog {
    @Composable
    fun OverviewDialog(
        onDismiss: Function0<Unit>,
        onAccept: Function0<Unit>
    ) {
        AlertDialog(
            modifier = Modifier.width(226.dp),
            onDismissRequest = onDismiss,
            text = {
                Text(stringResource(R.string.overview_dialog_text))
            },
            confirmButton = {
                Button(onClick = onAccept) {
                    Text(stringResource(R.string.overview_dialog_accept))
                }
            },
            dismissButton = {
                Button(onClick = onDismiss) {
                    Text(stringResource(R.string.overview_dialog_dismiss))
                }
            }
        )
    }
}
