/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.client.networking.plugin

import io.bitpogo.pixalb.client.ClientContract
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging

internal class LoggingConfigurator : KtorPluginsContract.LoggingConfigurator {
    override fun configure(pluginConfiguration: Logging.Config, subConfiguration: ClientContract.Logger) {
        pluginConfiguration.logger = subConfiguration
        pluginConfiguration.level = LogLevel.ALL
    }
}
