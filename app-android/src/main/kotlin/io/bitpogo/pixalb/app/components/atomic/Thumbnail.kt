/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.app.components.atomic

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Scale
import io.bitpogo.pixalb.app.R

object Thumbnail {
    @Composable
    fun Thumbnail(
        url: String,
        contentDescription: String?,
        modifier: Modifier
    ) {
        Box(
            modifier = modifier
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(url)
                    .crossfade(true)
                    .size(100, 100)
                    .precision(Precision.EXACT)
                    .scale(Scale.FILL)
                    .error(R.mipmap.placeholder_foreground)
                    .build(),
                contentDescription = contentDescription,
                placeholder = painterResource(R.mipmap.placeholder_foreground),
                contentScale = ContentScale.Crop,
                modifier = modifier.clip(CircleShape)
            )
        }
    }
}
