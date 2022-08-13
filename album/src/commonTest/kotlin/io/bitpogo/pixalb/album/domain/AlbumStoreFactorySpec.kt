/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.album.domain

import io.bitpogo.pixalb.album.AlbumContract
import io.bitpogo.pixalb.album.database.PixabayDataBase
import io.bitpogo.pixalb.album.kmock
import io.bitpogo.pixalb.client.ClientContract
import io.bitpogo.util.coroutine.wrapper.CoroutineWrapperContract
import kotlin.test.Test
import tech.antibytes.kmock.MockCommon
import tech.antibytes.util.test.fulfils

@MockCommon(
    ClientContract.Client::class,
    CoroutineWrapperContract.CoroutineScopeDispatcher::class,
    PixabayDataBase::class
)
class AlbumStoreFactorySpec {
    @Test
    fun `It fulfils AlbumStoreFactory`() {
        AlbumStore fulfils AlbumContract.StoreFactory::class
    }

    @Test
    fun `Given getInstance is called it returns a Store`() {
        // When
        val store = AlbumStore.getInstance(kmock(), kmock(), kmock(), kmock())

        // The
        store fulfils AlbumContract.Store::class
    }
}
