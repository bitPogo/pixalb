package io.bitpogo.pixalb.client

import io.bitpogo.pixalb.client.error.PixabayClientError
import io.bitpogo.pixalb.client.model.PixabayResponse
import io.bitpogo.util.coroutine.result.ResultContract

object ClientContract {
    fun interface ConnectivityManager {
        fun hasConnection(): Boolean
    }

    interface Logger {
        fun info(message: String)
        fun warn(message: String)
        fun error(exception: Throwable, message: String?)
    }

    interface Client {
        suspend fun fetch(
            query: String,
            page: Int,
        ): ResultContract<PixabayResponse, PixabayClientError>
    }

    interface ClientFactory {
        fun getInstance(
            apiToken: String,
            logger: Logger,
            connection: ConnectivityManager,
        ): Client
    }
}
