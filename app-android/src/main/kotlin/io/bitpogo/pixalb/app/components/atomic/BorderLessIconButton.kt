/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.app.components.atomic

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.bitpogo.pixalb.app.theme.White

object BorderLessIconButton {
    @Composable
    fun BorderLessIconButton(
        icon: ImageVector,
        contentDescription: String,
        onClick: Function0<Unit>,
    ) {
        Button(
            onClick = onClick,
            shape = RoundedCornerShape(15.dp),
            border = BorderStroke(0.dp, Color.Transparent),
            modifier = Modifier.border(0.dp, Color.Transparent).background(Color.Transparent),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
            elevation = null,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.background(White, RoundedCornerShape(15.dp)),
            )
        }
    }
}
