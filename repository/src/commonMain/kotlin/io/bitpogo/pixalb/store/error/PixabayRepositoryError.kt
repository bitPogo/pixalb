/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.store.error

sealed class PixabayRepositoryError(
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause) {
    class NoConnection : PixabayRepositoryError()
    class UnsuccessfulRequest(cause: Throwable) : PixabayRepositoryError(cause = cause)
}
