/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.app.overview

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.bitpogo.pixalb.album.domain.model.OverviewItem
import io.bitpogo.pixalb.app.components.atomic.Tag
import io.bitpogo.pixalb.app.components.atomic.Thumbnail
import io.bitpogo.pixalb.app.components.atomic.UserIndicator
import io.bitpogo.pixalb.app.overview.OverviewContract.COMPLETE_ITEM_LIST

object OverviewList {
    @Composable
    private fun OverviewListItem(
        item: OverviewItem,
        modifier: Modifier = Modifier,
        onClick: Function1<Long, Unit>
    ) {
        Row(
            modifier = Modifier
                .height(88.dp)
                .clickable(role = Role.Button, onClick = { onClick(item.id) })
                .padding(start = 10.dp, top = 20.dp, bottom = 10.dp)
                .then(modifier)
        ) {
            Column {
                Thumbnail.Thumbnail(
                    url = item.thumbnail,
                    contentDescription = "",
                    modifier = Modifier
                        .height(48.dp)
                        .width(48.dp)
                )
            }

            Column(
                modifier = Modifier
                    .height(64.dp)
                    .padding(start = 16.dp, top = 4.dp, end = 8.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    UserIndicator.UserIndicator(item.userName)
                }

                Row {
                    Tag.Tag(item.tags.joinToString(", "))
                }
            }
        }
    }

    @Composable
    fun OverviewList(
        items: List<OverviewItem>,
        onClick: Function1<Long, Unit>,
        loadNextItems: Function0<Unit>
    ) {
        LazyColumn {
            itemsIndexed(items) { idx, item: OverviewItem ->
                if (idx == items.lastIndex && items.size % COMPLETE_ITEM_LIST == 0) {
                    loadNextItems()
                }

                OverviewListItem(
                    item = item,
                    onClick = onClick
                )
            }
        }
    }
}
