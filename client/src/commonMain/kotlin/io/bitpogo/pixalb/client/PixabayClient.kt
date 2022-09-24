/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.client

import io.bitpogo.pixalb.client.ClientContract.ENDPOINT
import io.bitpogo.pixalb.client.ClientContract.HOST
import io.bitpogo.pixalb.client.ClientContract.ITEMS_PER_PAGE
import io.bitpogo.pixalb.client.error.PixabayClientError
import io.bitpogo.pixalb.client.model.PixabayResponse
import io.bitpogo.pixalb.client.networking.ClientConfigurator
import io.bitpogo.pixalb.client.networking.HttpErrorMapper
import io.bitpogo.pixalb.client.networking.NetworkingContract
import io.bitpogo.pixalb.client.networking.RequestBuilder
import io.bitpogo.pixalb.client.networking.plugin.LoggingConfigurator
import io.bitpogo.pixalb.client.networking.plugin.ResponseValidatorConfigurator
import io.bitpogo.pixalb.client.networking.plugin.SerializerConfigurator
import io.bitpogo.pixalb.client.networking.receive
import io.bitpogo.pixalb.client.serialization.JsonConfigurator
import io.bitpogo.util.coroutine.result.Failure
import io.bitpogo.util.coroutine.result.ResultContract
import io.bitpogo.util.coroutine.result.Success
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpCallValidator
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logging
import kotlinx.serialization.json.Json

class PixabayClient internal constructor(
    private val token: String,
    private val requestBuilder: NetworkingContract.RequestBuilderFactory,
    private val connectivityManager: ClientContract.ConnectivityManager,
) : ClientContract.Client {
    private suspend fun guardTransaction(
        action: suspend () -> ResultContract<PixabayResponse, PixabayClientError>,
    ): ResultContract<PixabayResponse, PixabayClientError> {
        return if (!connectivityManager.hasConnection()) {
            Failure(PixabayClientError.NoConnection())
        } else {
            action()
        }
    }

    private suspend fun fetchImagesFromApi(
        query: String,
        page: UShort,
    ): PixabayResponse {
        return requestBuilder
            .create()
            .setParameter(
                mapOf(
                    "key" to token,
                    "q" to query,
                    "page" to page.toString(),
                    "per_page" to ITEMS_PER_PAGE,
                ),
            ).prepare(
                path = ENDPOINT,
            ).receive()
    }

    override suspend fun fetchImages(
        query: String,
        page: UShort,
    ): ResultContract<PixabayResponse, PixabayClientError> = guardTransaction {
        try {
            Success(fetchImagesFromApi(query, page))
        } catch (e: PixabayClientError) {
            Failure(e)
        }
    }

    companion object : ClientContract.ClientFactory {
        private fun initPlugins(
            logger: ClientContract.Logger,
        ): Set<NetworkingContract.Plugin<in Any, in Any?>> {
            val jsonConfig = JsonConfigurator()
            Json { jsonConfig.configure(this) }

            @Suppress("UNCHECKED_CAST")
            return setOf(
                NetworkingContract.Plugin(
                    ContentNegotiation,
                    SerializerConfigurator(),
                    jsonConfig,
                ) as NetworkingContract.Plugin<in Any, in Any?>,
                NetworkingContract.Plugin(
                    Logging,
                    LoggingConfigurator(),
                    logger,
                ) as NetworkingContract.Plugin<in Any, in Any?>,
                NetworkingContract.Plugin(
                    HttpCallValidator,
                    ResponseValidatorConfigurator(),
                    HttpErrorMapper(),
                ) as NetworkingContract.Plugin<in Any, in Any?>,
            )
        }

        private fun initRequestBuilder(
            logger: ClientContract.Logger,
        ): NetworkingContract.RequestBuilderFactory {
            return RequestBuilder.Factory(
                client = HttpClient().config {
                    ClientConfigurator.configure(
                        this,
                        initPlugins(logger),
                    )
                },
                host = HOST,
            )
        }

        override fun getInstance(
            apiToken: String,
            logger: ClientContract.Logger,
            connection: ClientContract.ConnectivityManager,
        ): ClientContract.Client {
            return PixabayClient(
                token = apiToken,
                requestBuilder = initRequestBuilder(logger),
                connectivityManager = connection,
            )
        }
    }
}
