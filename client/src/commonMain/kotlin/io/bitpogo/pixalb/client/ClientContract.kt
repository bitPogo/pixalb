package io.bitpogo.pixalb.client

object ClientContract {
    fun interface ConnectivityManager {
        fun hasConnection(): Boolean
    }

    interface Logger {
        fun info(message: String)
        fun warn(message: String)
        fun error(exception: Throwable, message: String?)
    }

    interface Client

    interface ClientFactory {
        fun getInstance(
            apiToken: String,
            logger: Logger,
            connection: ConnectivityManager,
        ): Client
    }
}
