/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.client

class LoggerStub : ClientContract.Logger {

    override fun info(message: String) {
        throw RuntimeException(message)
    }

    override fun warn(message: String) {
        throw RuntimeException(message)
    }

    override fun error(exception: Throwable, message: String?) {
        throw RuntimeException(message)
    }

    override fun log(message: String) {
        println(message)
    }
}
