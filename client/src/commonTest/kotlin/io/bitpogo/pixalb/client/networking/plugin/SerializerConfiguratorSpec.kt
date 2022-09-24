/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.client.networking.plugin

import io.bitpogo.pixalb.client.kmock
import io.bitpogo.pixalb.client.serialization.JsonConfiguratorContract
import io.bitpogo.pixalb.client.serialization.JsonConfiguratorContractMock
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import kotlin.js.JsName
import kotlin.test.Test
import kotlinx.serialization.json.JsonBuilder
import tech.antibytes.kmock.MockCommon
import tech.antibytes.util.test.fulfils
import tech.antibytes.util.test.mustBe

@MockCommon(
    JsonConfiguratorContract::class,
)
class SerializerConfiguratorSpec {
    @Test
    @JsName("fn0")
    fun `It fulfils SerializerConfigurator`() {
        SerializerConfigurator() fulfils KtorPluginsContract.SerializerConfigurator::class
    }

    @Test
    @JsName("fn1")
    fun `Given configure is called with a JsonFeatureConfig it just runs while configuring the serializer`() {
        // Given
        val pluginConfig = ContentNegotiation.Config()
        val jsonConfigurator: JsonConfiguratorContractMock = kmock()

        var capturedBuilder: JsonBuilder? = null
        jsonConfigurator._configure run { delegatedBuilder ->
            capturedBuilder = delegatedBuilder
            delegatedBuilder
        }

        // When
        val result = SerializerConfigurator().configure(pluginConfig, jsonConfigurator)

        // Then
        result mustBe Unit
        capturedBuilder!! fulfils JsonBuilder::class
    }
}
