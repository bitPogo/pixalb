/* ktlint-disable filename */
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
        imageUrl = fixture(),
    )
}
