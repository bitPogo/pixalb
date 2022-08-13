/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.app.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.bitpogo.pixalb.album.domain.model.DetailViewItem
import io.bitpogo.pixalb.app.R
import io.bitpogo.pixalb.app.components.atomic.BorderLessIconButton.BorderLessIconButton
import io.bitpogo.pixalb.app.components.atomic.HeroImage.HeroImage
import io.bitpogo.pixalb.app.components.molecule.NoResult.NoResult
import io.bitpogo.pixalb.app.detail.PropertyDescriptor.PropertyDescriptor
import io.bitpogo.pixalb.app.theme.BrightGray

object DetailScreen {
    @Composable
    private fun RenderHeroImage(url: String) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            HeroImage(url)

            Divider(
                thickness = 1.dp,
                color = BrightGray,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(0.85f)
            )
        }
    }

    @Composable
    private fun RenderProperties(details: DetailViewItem) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            PropertyDescriptor(
                fieldName = stringResource(id = R.string.detailview_author),
                value = details.userName
            )
            PropertyDescriptor(
                fieldName = stringResource(id = R.string.detailview_tag),
                value = details.tags.joinToString(", ")
            )
            PropertyDescriptor(
                fieldName = stringResource(id = R.string.detailview_like),
                value = details.likes.toString()
            )
            PropertyDescriptor(
                fieldName = stringResource(id = R.string.detailview_comment),
                value = details.comments.toString()
            )
            PropertyDescriptor(
                fieldName = stringResource(id = R.string.detailview_download),
                value = details.downloads.toString()
            )
        }
    }

    @Composable
    private fun RenderSteckBrief(details: DetailViewItem) {
        Column {
            RenderHeroImage(url = details.imageUrl)
            RenderProperties(details)
        }
    }

    @Composable
    private fun SelectContent(details: State<DetailContract.State>) {
        if (details.value is DetailContract.State.Accepted) {
            RenderSteckBrief(details = (details.value as DetailContract.State.Accepted).value)
        } else {
            NoResult()
        }
    }

    @Composable
    private fun RenderNavBar(
        navigator: DetailContract.Navigator
    ) {
        Row {
            BorderLessIconButton(
                icon = Icons.Outlined.Cancel,
                contentDescription = stringResource(id = R.string.detailview_cancel)
            ) { navigator.goToOverview() }
        }
    }

    @Composable
    fun DetailScreen(
        viewModel: DetailContract.ViewModel = hiltViewModel<DetailviewViewModel>(),
        navigator: DetailContract.Navigator
    ) {
        val details = viewModel.details.collectAsState()
        val scrollState = rememberScrollState()

        Card(
            shape = RoundedCornerShape(5.dp),
            border = BorderStroke(0.dp, Color.Transparent),
            elevation = 8.dp,
            modifier = Modifier.verticalScroll(scrollState)
        ) {
            SelectContent(details = details)
            RenderNavBar(navigator)
        }
    }
}
