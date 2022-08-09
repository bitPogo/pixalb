/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.client.networking.plugin

import io.bitpogo.pixalb.client.ClientContract
import io.bitpogo.pixalb.client.networking.NetworkingContract
import io.bitpogo.pixalb.client.serialization.JsonConfiguratorContract
import io.ktor.client.plugins.HttpCallValidator
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logging

internal interface KtorPluginsContract {
    fun interface ErrorMapper {
        fun mapAndThrow(error: Throwable)
    }

    fun interface LoggingConfigurator : NetworkingContract.PluginConfigurator<Logging.Config, ClientContract.Logger>
    fun interface SerializerConfigurator : NetworkingContract.PluginConfigurator<ContentNegotiation.Config, JsonConfiguratorContract>
    fun interface ResponseValidatorConfigurator : NetworkingContract.PluginConfigurator<HttpCallValidator.Config, ErrorMapper>
}
