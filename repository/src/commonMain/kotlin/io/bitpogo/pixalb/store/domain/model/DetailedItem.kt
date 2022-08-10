/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.store.domain.model

data class DetailedViewItem(
    val imageUrl: String,
    val userName: String,
    val tags: List<String>,
    val likes: UInt,
    val download: UInt,
    val comments: UInt,
)
