/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.album.fixture

import io.bitpogo.pixalb.client.model.PixabayItem
import io.bitpogo.pixalb.client.model.PixabayResponse
import tech.antibytes.kfixture.PublicApi
import tech.antibytes.kfixture.fixture

fun PublicApi.Fixture.pixabayItemFixture(
    tagsGenerator: Function0<String>? = null,
): PixabayItem {
    return PixabayItem(
        id = fixture(),
        user = fixture(),
        tags = tagsGenerator?.invoke() ?: fixture(),
        downloads = fixture(),
        likes = fixture(),
        comments = fixture(),
        preview = fixture(),
        large = fixture(),
    )
}

fun PublicApi.Fixture.pixabayItemsFixture(
    total: Int? = null,
    size: Int? = null,
    tagsGenerator: Function0<String>? = null,
): PixabayResponse {
    val items: MutableList<PixabayItem> = mutableListOf()
    val amountOfItems: Int = size ?: fixture(1, 10)

    repeat(amountOfItems) {
        items.add(pixabayItemFixture(tagsGenerator))
    }

    return PixabayResponse(
        total = total ?: fixture(PublicApi.Sign.POSITIVE),
        items = items,
    )
}
