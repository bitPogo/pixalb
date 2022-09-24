/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.app.components.atomic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import io.bitpogo.pixalb.app.R

object HeroImage {
    @Composable
    fun HeroImage(
        url: String,
    ) {
        Row(
            modifier = Modifier
                .height(240.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(url)
                    .crossfade(true)
                    .scale(Scale.FILL)
                    .error(R.mipmap.placeholder_foreground)
                    .build(),
                contentDescription = "",
                placeholder = painterResource(R.mipmap.placeholder_foreground),
                contentScale = ContentScale.Fit,
                modifier = Modifier.height(240.dp),
            )
        }
    }
}
