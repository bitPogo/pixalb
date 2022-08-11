/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.album.domain.error

sealed class PixabayError(
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause) {
    class NoConnection : PixabayError()
    class UnsuccessfulRequest(cause: Throwable) : PixabayError(cause = cause)
    class UnsuccessfulDatabaseAccess(cause: Throwable) : PixabayError(cause = cause)
    class MissingEntry : PixabayError()
    class MissingPage : PixabayError()
    class EntryCap : PixabayError()
}
