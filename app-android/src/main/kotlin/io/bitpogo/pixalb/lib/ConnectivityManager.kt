/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.lib

import android.content.Context
import android.net.ConnectivityManager
import io.bitpogo.pixalb.client.ClientContract

class ConnectivityManager(
    private val context: Context,
) : ClientContract.ConnectivityManager {
    override fun hasConnection(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        @Suppress("DEPRECATION")
        return connectivityManager.activeNetworkInfo?.isConnected ?: false
    }
}
