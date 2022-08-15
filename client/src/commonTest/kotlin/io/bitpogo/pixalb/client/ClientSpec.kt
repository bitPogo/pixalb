/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.client

import io.bitpogo.pixalb.client.error.PixabayClientError
import io.bitpogo.pixalb.client.fixture.StringAlphaGenerator
import io.bitpogo.pixalb.client.fixture.pixabayResponseFixture
import io.bitpogo.pixalb.client.model.PixabayResponse
import io.bitpogo.pixalb.client.networking.NetworkingContract
import io.bitpogo.pixalb.client.networking.RequestBuilderFactoryMock
import io.bitpogo.pixalb.client.networking.RequestBuilderMock
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpStatement
import io.ktor.http.HttpStatusCode
import kotlin.js.JsName
import kotlin.test.BeforeTest
import kotlin.test.Test
import tech.antibytes.kfixture.fixture
import tech.antibytes.kfixture.kotlinFixture
import tech.antibytes.kfixture.qualifier.qualifiedBy
import tech.antibytes.kmock.MockCommon
import tech.antibytes.kmock.verification.assertProxy
import tech.antibytes.util.test.coroutine.runBlockingTestWithTimeout
import tech.antibytes.util.test.fulfils
import tech.antibytes.util.test.ktor.KtorMockClientFactory
import tech.antibytes.util.test.sameAs

@MockCommon(
    NetworkingContract.RequestBuilderFactory::class,
    NetworkingContract.RequestBuilder::class,
    ClientContract.ConnectivityManager::class
)
class ClientSpec {
    private val ascii = qualifiedBy("ascii")
    private val fixture = kotlinFixture {
        addGenerator(
            String::class,
            StringAlphaGenerator,
            ascii
        )
    }

    private val requestBuilderFactory: RequestBuilderFactoryMock = kmock()
    private val requestBuilder: RequestBuilderMock = kmock()
    private val connectivityManager: ConnectivityManagerMock = kmock()
    private val ktorDummy = HttpRequestBuilder()

    @BeforeTest
    fun setUp() {
        requestBuilderFactory._clearMock()
        requestBuilder._clearMock()
        connectivityManager._clearMock()
    }

    @Test
    @JsName("fn0")
    fun `It fulfils Client`() {
        PixabayClient(
            fixture.fixture(),
            requestBuilderFactory,
            connectivityManager
        ) fulfils ClientContract.Client::class
    }

    @Test
    @JsName("fn1")
    fun `Given fetchImages is called with a query and a page Index it returns an Error if it has no Connection`() = runBlockingTestWithTimeout {
        // Given
        val query: String = fixture.fixture(ascii)
        val page: UShort = fixture.fixture()

        connectivityManager._hasConnection returns false

        // When
        val response = PixabayClient(
            fixture.fixture(),
            requestBuilderFactory,
            connectivityManager
        ).fetchImages(query, page)

        // Then
        response.error!! fulfils PixabayClientError.NoConnection::class
    }

    @Test
    @JsName("fn2")
    fun `Given fetchImages is called with a query and a page Index it propagates Errors`() = runBlockingTestWithTimeout {
        // Given
        val query: String = fixture.fixture(ascii)
        val page: UShort = fixture.fixture()
        val expected: String = fixture.fixture()
        val responseError = PixabayClientError.RequestError(400)
        val client = KtorMockClientFactory.createSimpleMockClient(
            response = expected,
            error = responseError,
            status = HttpStatusCode.BadRequest
        )

        connectivityManager._hasConnection returns true
        requestBuilderFactory._create returns requestBuilder
        requestBuilder._setParameter returns requestBuilder
        requestBuilder._prepare returns HttpStatement(ktorDummy, client)

        // When
        val response = PixabayClient(
            fixture.fixture(),
            requestBuilderFactory,
            connectivityManager
        ).fetchImages(query, page)

        // Then
        response.error!! sameAs responseError
    }

    @Test
    @JsName("fn3")
    fun `Given fetchImages is called with a page Index it delegates the Call and returns the response`() = runBlockingTestWithTimeout {
        // Given
        val token: String = fixture.fixture()
        val query: String = fixture.fixture(ascii)
        val page: UShort = fixture.fixture()
        val expected: PixabayResponse = fixture.pixabayResponseFixture(4)
        val client = KtorMockClientFactory.createObjectMockClient(listOf(expected)) { scope, _ ->
            return@createObjectMockClient scope.respond(
                content = fixture.fixture<String>()
            )
        }

        connectivityManager._hasConnection returns true
        requestBuilderFactory._create returns requestBuilder
        requestBuilder._setParameter returns requestBuilder
        requestBuilder._prepare returns HttpStatement(ktorDummy, client)

        // When
        val response = PixabayClient(token, requestBuilderFactory, connectivityManager)
            .fetchImages(query, page)
            .unwrap()

        // Then
        response sameAs expected
        assertProxy {
            requestBuilder._setParameter.hasBeenStrictlyCalledWith(
                mapOf(
                    "key" to token,
                    "q" to query,
                    "page" to page.toString(),
                    "per_page" to "200"
                )
            )
        }
    }
}
