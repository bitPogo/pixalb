/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.album.domain.model

data class DetailViewItem(
    val imageUrl: String,
    val userName: String,
    val tags: List<String>,
    val likes: UInt,
    val downloads: UInt,
    val comments: UInt,
)
