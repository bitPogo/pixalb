/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.app.overview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.bitpogo.pixalb.album.domain.model.OverviewItem
import io.bitpogo.pixalb.app.overview.OverviewList.OverviewList
import io.bitpogo.pixalb.app.theme.PixabayAlbumTheme

@Preview
@Composable
fun OverviewListPreview() {
    val items = listOf(
        OverviewItem(
            id = 1,
            thumbnail = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS9ymHYb2GFhrk67z9C--HLdnNSk366hEvBVdytb53SzBgQsfWi6apev5iMXJP0qyHfMs4&usqp=CAU",
            userName = "me",
            tags = listOf("this", "is", "a"),
        ),
        OverviewItem(
            id = 2,
            thumbnail = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS9ymHYb2GFhrk67z9C--HLdnNSk366hEvBVdytb53SzBgQsfWi6apev5iMXJP0qyHfMs4&usqp=CAU",
            userName = "not me",
            tags = listOf("test"),
        ),
        OverviewItem(
            id = 3,
            thumbnail = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS9ymHYb2GFhrk67z9C--HLdnNSk366hEvBVdytb53SzBgQsfWi6apev5iMXJP0qyHfMs4&usqp=CAU",
            userName = "somebody",
            tags = listOf("other"),
        ),
    )

    PixabayAlbumTheme {
        OverviewList(
            items = items,
            loadNextItems = { },
            onClick = { },
        )
    }
}
