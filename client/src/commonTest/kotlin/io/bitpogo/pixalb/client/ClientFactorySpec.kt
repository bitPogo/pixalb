/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.client

import kotlin.js.JsName
import kotlin.test.Test
import tech.antibytes.kfixture.fixture
import tech.antibytes.kfixture.kotlinFixture
import tech.antibytes.kmock.MockCommon
import tech.antibytes.util.test.fulfils

@MockCommon(
    ClientContract.ConnectivityManager::class,
    ClientContract.Logger::class
)
class ClientFactorySpec {
    private val fixture = kotlinFixture()

    @Test
    @JsName("fn0")
    fun `It fulfils ClientFactory`() {
        PixabayClient fulfils ClientContract.ClientFactory::class
    }

    @Test
    @JsName("fn1")
    fun `Given getInstance is called it creates a PixabayClient`() {
        PixabayClient.getInstance(
            fixture.fixture(),
            kmock(),
            kmock()
        ) fulfils ClientContract.Client::class
    }
}
