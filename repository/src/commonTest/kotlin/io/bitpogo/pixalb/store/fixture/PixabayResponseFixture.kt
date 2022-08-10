/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.store.fixture

import io.bitpogo.pixalb.client.model.PixabayItem
import io.bitpogo.pixalb.client.model.PixabayResponse
import tech.antibytes.kfixture.PublicApi
import tech.antibytes.kfixture.fixture

fun PublicApi.Fixture.pixabayItemFixture(): PixabayItem {
    return PixabayItem(
        id = fixture(),
        user = fixture(),
        tags = fixture(),
        downloads = fixture(),
        likes = fixture(),
        comments = fixture(),
        preview = fixture(),
        large = fixture()
    )
}

fun PublicApi.Fixture.pixabayItemsFixture(
    total: Int? = null,
    size: Int? = null
): PixabayResponse {
    val items: MutableList<PixabayItem> = mutableListOf()
    val amountOfItems: Int = size ?: fixture(1, 10)

    repeat(amountOfItems) {
        items.add(pixabayItemFixture())
    }

    return PixabayResponse(
        total = total ?: fixture(PublicApi.Sign.POSITIVE),
        items = items
    )
}
