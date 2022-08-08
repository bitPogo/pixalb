package io.bitpogo.pixalb.client.error

sealed class PixabayClientError(
    message: String?,
    cause: Throwable?
) : Error(message, cause)
