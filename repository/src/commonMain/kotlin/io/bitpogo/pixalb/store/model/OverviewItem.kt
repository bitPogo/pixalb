/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.store.model

data class OverviewItem(
    val thumbnail: String,
    val userName: String,
    val tags: List<String>
)
