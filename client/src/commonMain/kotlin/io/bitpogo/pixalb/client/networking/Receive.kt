/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.client.networking

import io.bitpogo.pixalb.client.error.PixabayClientError
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.statement.HttpStatement

internal suspend inline fun <reified T> HttpStatement.receive(): T {
    return try {
        body()
    } catch (exception: NoTransformationFoundException) {
        throw PixabayClientError.ResponseTransformFailure()
    }
}
