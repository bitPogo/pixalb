/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.client.networking

import io.bitpogo.pixalb.client.error.PixabayClientError
import io.bitpogo.pixalb.client.networking.plugin.KtorPluginsContract
import io.ktor.client.plugins.ResponseException

internal class HttpErrorMapper : KtorPluginsContract.ErrorMapper {
    private fun wrapError(error: Throwable): Throwable {
        return if (error is ResponseException) {
            PixabayClientError.RequestError(error.response.status.value)
        } else {
            error
        }
    }

    override fun mapAndThrow(error: Throwable) {
        throw wrapError(error)
    }
}
