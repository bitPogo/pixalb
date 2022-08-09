/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.client.networking

import io.ktor.client.HttpClientConfig

internal object ClientConfigurator : NetworkingContract.ClientConfigurator {
    override fun configure(
        httpConfig: HttpClientConfig<*>,
        installers: Set<NetworkingContract.Plugin<in Any, in Any?>>?
    ) {
        installers?.forEach { (plugin, configurator, subConfig) ->
            httpConfig.install(plugin) {
                configurator.configure(
                    this,
                    subConfig
                )
            }
        }
    }
}
