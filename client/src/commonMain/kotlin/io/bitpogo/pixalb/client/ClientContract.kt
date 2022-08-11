package io.bitpogo.pixalb.client

import io.bitpogo.pixalb.client.error.PixabayClientError
import io.bitpogo.pixalb.client.model.PixabayResponse
import io.bitpogo.util.coroutine.result.ResultContract

object ClientContract {
    fun interface ConnectivityManager {
        fun hasConnection(): Boolean
    }

    interface Logger : io.ktor.client.plugins.logging.Logger {
        fun info(message: String)
        fun warn(message: String)
        fun error(exception: Throwable, message: String?)
    }

    interface Client {
        suspend fun fetchImages(
            query: String,
            page: UShort
        ): ResultContract<PixabayResponse, PixabayClientError>
    }

    interface ClientFactory {
        fun getInstance(
            apiToken: String,
            logger: Logger,
            connection: ConnectivityManager
        ): Client
    }

    internal val ENDPOINT = listOf("api/")
    internal const val HOST = "pixabay.com"
    internal const val ITEMS_PER_PAGE = "200"
}
