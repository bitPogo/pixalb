/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.fixture

import io.bitpogo.pixalb.album.domain.model.DetailViewItem
import tech.antibytes.kfixture.PublicApi
import tech.antibytes.kfixture.fixture
import tech.antibytes.kfixture.listFixture

fun PublicApi.Fixture.detailviewItemFixture(): DetailViewItem {
    return DetailViewItem(
        userName = fixture(),
        tags = listFixture(),
        downloads = fixture(),
        likes = fixture(),
        comments = fixture(),
        imageUrl = fixture()
    )
}

fun PublicApi.Fixture.detailviewItemsFixture(
    size: Int? = null
): List<DetailViewItem> {
    val items: MutableList<DetailViewItem> = mutableListOf()
    val amountOfItems: Int = size ?: fixture(1, 10)

    repeat(amountOfItems) {
        items.add(detailviewItemFixture())
    }

    return items
}
