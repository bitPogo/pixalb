/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.album.fixture

import io.bitpogo.pixalb.album.domain.model.OverviewItem
import io.bitpogo.pixalb.client.model.PixabayItem
import io.bitpogo.pixalb.client.model.PixabayResponse
import tech.antibytes.kfixture.PublicApi
import tech.antibytes.kfixture.fixture
import tech.antibytes.kfixture.listFixture

fun PublicApi.Fixture.overviewItemFixture(): OverviewItem {
    return OverviewItem(
        thumbnail = fixture(),
        userName = fixture(),
        tags = listFixture()
    )
}

fun PublicApi.Fixture.overviewItemsFixture(
    size: Int? = null,
): List<OverviewItem> {
    val items: MutableList<OverviewItem> = mutableListOf()
    val amountOfItems: Int = size ?: fixture(1, 10)

    repeat(amountOfItems) {
        items.add(overviewItemFixture())
    }

    return items
}
