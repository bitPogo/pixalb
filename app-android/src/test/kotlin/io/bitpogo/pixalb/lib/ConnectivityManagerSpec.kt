/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.lib

import android.content.Context
import android.net.ConnectivityManager as AndroidConnection
import io.bitpogo.pixalb.client.ClientContract
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import tech.antibytes.kfixture.fixture
import tech.antibytes.kfixture.kotlinFixture
import tech.antibytes.util.test.fulfils
import tech.antibytes.util.test.mustBe

@RunWith(RobolectricTestRunner::class)
class ConnectivityManagerSpec {
    private val fixture = kotlinFixture()

    @Test
    fun `It fulfils ConnectivityManager`() {
        ConnectivityManager(mockk()) fulfils ClientContract.ConnectivityManager::class
    }

    @Test
    fun `Given hasConnection is called, it returns false if activeNetworkInfo is null`() {
        // Given
        val context: Context = mockk()
        val connectivityManager: AndroidConnection = mockk()

        every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager
        every { connectivityManager.activeNetworkInfo } returns null

        // When
        val actual = ConnectivityManager(context).hasConnection()

        // Then
        actual mustBe false
    }

    @Test
    fun `Given hasConnection is called, it returns the value activeNetworkInfo Connection`() {
        // Given
        val expected: Boolean = fixture.fixture()
        val context: Context = mockk()
        val connectivityManager: AndroidConnection = mockk()

        every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager
        every { connectivityManager.activeNetworkInfo!!.isConnected } returns expected

        // When
        val actual = ConnectivityManager(context).hasConnection()

        // Then
        actual mustBe expected
    }
}
