package io.bitpogo.pixalb.client.error

sealed class PixabayClientError(
    message: String? = null,
    cause: Throwable? = null,
) : Error(message, cause) {
    class NoConnection : PixabayClientError()
    class RequestError(val status: Int) : PixabayClientError()
    class RequestValidationFailure(message: String) : PixabayClientError(message)
    class ResponseTransformFailure : PixabayClientError(message = "Unexpected Response")
}
