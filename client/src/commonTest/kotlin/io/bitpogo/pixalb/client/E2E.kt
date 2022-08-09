/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.client

import io.bitpogo.pixalb.client.test.config.TestConfig
import kotlin.test.Ignore
import kotlin.test.Test
import tech.antibytes.util.test.coroutine.runBlockingTestWithTimeout
import tech.antibytes.util.test.isNot

class E2E {
    @Test
    @Ignore
    fun `It fetches Images`() = runBlockingTestWithTimeout(5000) {
        // Given
        val client = PixabayClient.getInstance(
            apiToken = TestConfig.apiKey,
            logger = LoggerStub(),
            connection = { true }
        )

        // When
        val response = client.fetchImages(
            query = "yellow flower",
            page = 1u
        )

        // Then
        response.value isNot null
        response.value!!.total isNot 0
        response.value!!.items.size isNot 0
    }
}
